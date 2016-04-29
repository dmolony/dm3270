package com.bytezone.dm3270.display;

import static com.bytezone.dm3270.database.DatabaseRequest.Command.UPDATE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.assistant.TableDataset;
import com.bytezone.dm3270.database.*;;

// created by FieldManager
// used by ScreenChangeListener (ScreenPacker, TransfersStage, TransferMenu)
// used by DownloadDialog (via TransferMenu)
// used by UploadDialog (via TransferMenu)
public class ScreenWatcher implements Initiator
{
  private static final String[] tsoMenus =
      { "Menu", "List", "Mode", "Functions", "Utilities", "Help" };
  private static final String[] pdsMenus =
      { "Menu", "Functions", "Confirm", "Utilities", "Help" };
  private static final String[] memberMenus =
      { "Menu", "Functions", "Utilities", "Help" };
  private static final String SPLIT_LINE = ".  .  .  .  .  .  .  .  .  .  .  .  .  "
      + ".  .  .  .  .  .  .  .  .  .  .  .  .  .";
  private static final String EXCLUDE_LINE = "-  -  -  -  -  -  -  -  -  -  -  -";
  private static final String segment = "[A-Z@#$][-A-Z0-9@#$]{0,7}";
  private static final Pattern datasetNamePattern =
      Pattern.compile (segment + "(\\." + segment + "){0,21}");
  private static final Pattern memberNamePattern = Pattern.compile (segment);

  private static final String ispfScreen = "ISPF Primary Option Menu";
  private static final String zosScreen = "z/OS Primary Option Menu";
  private static final String ispfShell = "ISPF Command Shell";

  private final FieldManager fieldManager;
  private final ScreenDimensions screenDimensions;
  private final BlockingQueue<DatabaseRequest> queue;

  private final Map<String, TableDataset> siteDatasets = new TreeMap<> ();
  private final List<TableDataset> screenDatasets = new ArrayList<> ();
  private final List<TableDataset> screenMembers = new ArrayList<> ();
  private final List<String> recentDatasetNames = new ArrayList<> ();

  private String datasetsMatching;
  private String datasetsOnVolume;

  private Field tsoCommandField;
  private boolean isTSOCommandScreen;
  private boolean isDatasetList;
  private boolean isMemberList;
  private boolean isSplitScreen;
  private int promptFieldLine;

  private String currentPDS = "";
  private String singleDataset = "";
  private String userid = "";
  private String prefix = "";

  public ScreenWatcher (FieldManager fieldManager, ScreenDimensions screenDimensions,
      BlockingQueue<DatabaseRequest> queue)
  {
    this.fieldManager = fieldManager;
    this.screenDimensions = screenDimensions;
    this.queue = queue;
  }

  public Field getTSOCommandField ()
  {
    return tsoCommandField;
  }

  public boolean isTSOCommandScreen ()
  {
    return isTSOCommandScreen;
  }

  public String getUserid ()
  {
    return userid;
  }

  public String getPrefix ()
  {
    return prefix;
  }

  // called by UploadDialog
  // called by DownloadDialog
  public Optional<TableDataset> getDataset (String datasetName)
  {
    if (siteDatasets.containsKey (datasetName))
      return Optional.of (siteDatasets.get (datasetName));
    return Optional.empty ();
  }

  public List<TableDataset> getDatasets ()
  {
    return screenDatasets;
  }

  public List<TableDataset> getMembers ()
  {
    return screenMembers;
  }

  public String getCurrentPDS ()
  {
    return currentPDS;
  }

  public String getSingleDataset ()
  {
    return singleDataset;
  }

  public List<String> getRecentDatasets ()
  {
    return recentDatasetNames;
  }

  // called by FieldManager after building a new screen
  void check ()
  {
    tsoCommandField = null;
    isTSOCommandScreen = false;
    isDatasetList = false;
    isMemberList = false;
    isSplitScreen = false;
    screenDatasets.clear ();
    screenMembers.clear ();
    //    currentDataset = "";
    //    singleDataset = "";
    promptFieldLine = -1;

    List<Field> screenFields = fieldManager.getFields ();
    if (screenFields.size () <= 2)
      return;

    isSplitScreen = checkSplitScreen ();
    if (isSplitScreen)
      return;

    checkMenu ();

    isTSOCommandScreen = checkTSOCommandScreen (screenFields);
    if (isTSOCommandScreen)
    {

    }
    else if (hasPromptField ())
    {
      if (prefix.isEmpty ())
        checkPrefixScreen (screenFields);       // initial ISPF screen

      isDatasetList = checkDatasetList (screenFields);
      if (isDatasetList)
      {
        //        System.out.println ("Dataset list");
      }
      else
      {
        isMemberList = checkMemberList (screenFields);
        if (isMemberList)
        {
          //          System.out.println ("Member list of " + currentDataset);
        }
        else
          checkSingleDataset (screenFields);
      }
    }
  }

  private void checkMenu ()
  {
    if (true)
      return;

    List<Field> rowFields = fieldManager.getRowFields (0, 1);
    dumpFields (rowFields);

    if (rowFields.size () > 1 && rowFields.size () < 10)
    {
      Field menuField = rowFields.get (0);
      String text = menuField.getText ();
      if (" Menu".equals (text) && menuField.isAlphanumeric () && menuField.isProtected ()
          && menuField.isVisible () && menuField.isIntensified ())
      {
        System.out.println ("Possible menu");
      }
    }
  }

  private boolean checkSplitScreen ()
  {
    return fieldManager.getFields ().parallelStream ()
        .filter (f -> f.isProtected () && f.getDisplayLength () == 79
            && f.getFirstLocation () % screenDimensions.columns == 1
            && SPLIT_LINE.equals (f.getText ()))
        .findAny ().isPresent ();
  }

  private boolean hasPromptField ()
  {
    List<Field> rowFields = fieldManager.getRowFields (1, 3);
    for (int i = 0; i < rowFields.size (); i++)
    {
      Field field = rowFields.get (i);
      String text = field.getText ();

      int column = field.getFirstLocation () % screenDimensions.columns;
      int nextFieldNo = i + 1;

      if (nextFieldNo < rowFields.size () && column == 1
          && ("Command ===>".equals (text) || "Option ===>".equals (text)))
      {
        Field nextField = rowFields.get (nextFieldNo);
        int length = nextField.getDisplayLength ();
        boolean modifiable = nextField.isUnprotected ();
        boolean visible = !nextField.isHidden ();

        if ((length == 66 || length == 48) && visible && modifiable)
        {
          tsoCommandField = nextField;
          promptFieldLine = field.getFirstLocation () / screenDimensions.columns;
          return true;
        }
      }
    }

    tsoCommandField = null;
    return false;
  }

  private void checkPrefixScreen (List<Field> screenFields)
  {
    if (screenFields.size () < 74)
      return;

    Field field = screenFields.get (10);
    String heading = field.getText ();
    if (!ispfScreen.equals (heading) && !zosScreen.equals (heading))
      return;

    if (!fieldManager.textMatches (23, " User ID . :", 457))
      return;

    field = screenFields.get (24);
    if (field.getFirstLocation () != 470)
      return;

    userid = field.getText ().trim ();

    if (!fieldManager.textMatches (72, " TSO prefix:", 1017))
      return;

    field = screenFields.get (73);
    if (field.getFirstLocation () != 1030)
      return;

    prefix = field.getText ().trim ();
  }

  private boolean checkTSOCommandScreen (List<Field> screenFields)
  {
    if (screenFields.size () < 19)
      return false;

    if (!fieldManager.textMatches (10, ispfShell))
      return false;

    int workstationFieldNo = 13;
    String workstationText = "Enter TSO or Workstation commands below:";
    if (!fieldManager.textMatches (workstationFieldNo, workstationText))
      if (!fieldManager.textMatches (++workstationFieldNo, workstationText))
        return false;

    if (!listMatchesArray (fieldManager.getMenus (), tsoMenus))
      return false;

    Field field = screenFields.get (workstationFieldNo + 5);
    if (field.getDisplayLength () != 234)
      return false;

    tsoCommandField = field;
    return true;
  }

  private boolean checkDatasetList (List<Field> screenFields)
  {
    if (screenFields.size () < 21)
      return false;

    List<Field> rowFields = fieldManager.getRowFields (2, 2);
    if (rowFields.size () == 0)
      return false;

    String text = rowFields.get (0).getText ();
    if (!text.startsWith ("DSLIST - Data Sets "))
      return false;

    String locationText = "";
    int pos = text.indexOf ("Row ");
    if (pos > 0)
      locationText = text.substring (19, pos).trim ();
    else
      locationText = text.substring (19).trim ();

    datasetsOnVolume = "";
    datasetsMatching = "";

    if (locationText.startsWith ("on volume "))
      datasetsOnVolume = locationText.substring (10);
    else if (locationText.startsWith ("Matching "))
      datasetsMatching = locationText.substring (9);
    else
    {
      // Could be: Matched in list REFLIST
      System.out.println ("Unexpected text: " + locationText);
      return false;
    }

    rowFields = fieldManager.getRowFields (5, 2);
    if (rowFields.size () < 3)
      return false;

    if (!rowFields.get (0).getText ().startsWith ("Command - Enter"))
      return false;

    int screenType = 0;
    int linesPerDataset = 1;
    int nextLine = 7;

    switch (rowFields.size ())
    {
      case 3:
        String heading = rowFields.get (1).getText ().trim ();
        if (heading.startsWith ("Tracks"))
          screenType = 1;
        else if (heading.startsWith ("Dsorg"))
          screenType = 2;
        break;

      case 4:
        String message = rowFields.get (1).getText ().trim ();
        heading = rowFields.get (2).getText ().trim ();
        if ("Volume".equals (heading) && "Message".equals (message))
          screenType = 3;
        break;

      case 6:
        message = rowFields.get (1).getText ().trim ();
        heading = rowFields.get (2).getText ().trim ();
        if ("Volume".equals (heading) && "Message".equals (message))
        {
          List<Field> rowFields2 = fieldManager.getRowFields (nextLine);
          if (rowFields2.size () == 1)
          {
            String line = rowFields2.get (0).getText ().trim ();
            if (line.equals ("Catalog"))
            {
              screenType = 4;
              linesPerDataset = 3;
              nextLine = 9;
            }
            else if (line.startsWith ("--"))
            {
              screenType = 5;
              linesPerDataset = 2;
              nextLine = 8;
            }
            else
              System.out.println ("Expected 'Catalog' or underscores: " + line);
          }
        }
        break;

      default:
        System.out.printf ("Unexpected number of fields: %d%n", rowFields.size ());
    }

    if (screenType == 0)
    {
      System.out.println ("Screen not recognised");
      dumpFields (rowFields);
      return false;
    }

    while (nextLine < screenDimensions.rows)
    {
      rowFields = fieldManager.getRowFields (nextLine, linesPerDataset);
      if (rowFields.size () <= 1)
        break;

      String lineText = rowFields.get (0).getText ();
      if (lineText.length () < 10)
        break;

      String datasetName = lineText.substring (9).trim ();
      if (datasetName.length () > 44)
      {
        System.out.printf ("Dataset name too long: %s%n", datasetName);
        break;
      }

      if (datasetNamePattern.matcher (datasetName).matches ())
        addDataset (datasetName, screenType, rowFields);
      else
      {
        // check for excluded datasets
        if (!EXCLUDE_LINE.equals (datasetName))
          System.out.printf ("Invalid dataset name: %s%n", datasetName);

        // what about GDGs?
      }

      nextLine += linesPerDataset;
      if (linesPerDataset > 1)
        nextLine++;                           // skip the row of hyphens
    }

    return true;
  }

  private void addDataset (String datasetName, int screenType, List<Field> rowFields)
  {
    TableDataset dataset;
    if (siteDatasets.containsKey (datasetName))
      dataset = siteDatasets.get (datasetName);
    else
    {
      dataset = new TableDataset (datasetName);
      siteDatasets.put (datasetName, dataset);
    }

    screenDatasets.add (dataset);
    Dataset ds = new Dataset (dataset.getDatasetName ());

    switch (screenType)
    {
      case 1:
        if (rowFields.size () == 2)
        {
          setSpace (dataset, rowFields.get (1).getText (), 6, 11, 15);
          ds.setSpace (dataset.getTracks (), dataset.getCylinders (),
                       dataset.getExtents (), dataset.getPercentUsed ());
          ds.setDevice (dataset.getDevice ());
        }
        break;

      case 2:
        if (rowFields.size () == 2)
        {
          setDisposition (dataset, rowFields.get (1).getText (), 5, 11, 18);
          ds.setDisposition (dataset.getDsorg (), dataset.getRecfm (),
                             dataset.getLrecl (), dataset.getBlksize ());
        }
        break;

      case 3:
        if (rowFields.size () == 3)
        {
          dataset.setVolume (rowFields.get (2).getText ().trim ());
          ds.setVolume (dataset.getVolume ());
        }
        break;

      case 4:
        if (rowFields.size () == 7)
        {
          dataset.setVolume (rowFields.get (2).getText ().trim ());
          setSpace (dataset, rowFields.get (3).getText (), 6, 10, 14);
          setDisposition (dataset, rowFields.get (4).getText (), 5, 10, 16);
          setDates (dataset, rowFields.get (5).getText (), ds);

          String catalog = rowFields.get (6).getText ().trim ();
          if (datasetNamePattern.matcher (catalog).matches ())
          {
            dataset.setCatalog (catalog);
            ds.setCatalog (catalog);
          }

          ds.setSpace (dataset.getTracks (), dataset.getCylinders (),
                       dataset.getExtents (), dataset.getPercentUsed ());
          ds.setDisposition (dataset.getDsorg (), dataset.getRecfm (),
                             dataset.getLrecl (), dataset.getBlksize ());
          ds.setVolume (dataset.getVolume ());
          ds.setDevice (dataset.getDevice ());
        }
        break;

      case 5:
        if (rowFields.size () >= 3)
        {
          dataset.setVolume (rowFields.get (2).getText ().trim ());
          if (rowFields.size () >= 6)
          {
            setSpace (dataset, rowFields.get (3).getText (), 6, 10, 14);
            setDisposition (dataset, rowFields.get (4).getText (), 5, 10, 16);
            setDates (dataset, rowFields.get (5).getText (), ds);

            ds.setSpace (dataset.getTracks (), dataset.getCylinders (),
                         dataset.getExtents (), dataset.getPercentUsed ());
            ds.setDisposition (dataset.getDsorg (), dataset.getRecfm (),
                               dataset.getLrecl (), dataset.getBlksize ());
          }
        }
        break;
    }

    sendRequest (new DatasetRequest (this, UPDATE, ds));
  }

  private void setSpace (TableDataset dataset, String details, int t1, int t2, int t3)
  {
    if (details.trim ().isEmpty ())
      return;

    if (details.length () >= t1)
      dataset.setTracks (getInteger ("tracks", details.substring (0, t1).trim ()));
    if (details.length () >= t2)
      dataset.setPercentUsed (getInteger ("pct", details.substring (t1, t2).trim ()));
    if (details.length () >= t3)
      dataset.setExtents (getInteger ("ext", details.substring (t2, t3).trim ()));
    if (details.length () > t3)
      dataset.setDevice (details.substring (t3).trim ());
  }

  private void setDisposition (TableDataset dataset, String details, int t1, int t2,
      int t3)
  {
    if (details.trim ().isEmpty ())
      return;

    if (details.length () >= t1)
      dataset.setDsorg (details.substring (0, t1).trim ());
    if (details.length () >= t2)
      dataset.setRecfm (details.substring (t1, t2).trim ());
    if (details.length () >= t3)
      dataset.setLrecl (getInteger ("lrecl", details.substring (t2, t3).trim ()));
    if (details.length () > t3)
      dataset.setBlksize (getInteger ("blksize", details.substring (t3).trim ()));
  }

  private void setDates (TableDataset dataset, String details, Dataset ds)
  {
    if (details.trim ().isEmpty ())
      return;

    String created = details.substring (0, 11).trim ();
    String expires = details.substring (11, 22).trim ();
    String referred = details.substring (22).trim ();

    dataset.setCreated (created);
    dataset.setExpires (expires);
    dataset.setReferredDate (referred);

    //    System.out.printf ("Created: [%s]%n", details.substring (0, 11).trim ());
    //    System.out.println (dataset.getCreated ());
    //    System.out.printf ("Expires: [%s]%n", details.substring (11, 22).trim ());
    //    System.out.println (dataset.getExpires ());
    //    System.out.printf ("Referred: [%s]%n", details.substring (22).trim ());
    //    System.out.println (dataset.getReferredDate ());
    //    System.out.println (dataset.getReferredTime ());
    ds.setDates (created, expires, referred);
  }

  private boolean checkMemberList (List<Field> screenFields)
  {
    if (screenFields.size () < 14)
      return false;

    if (listMatchesArray (fieldManager.getMenus (), pdsMenus))
      return checkMemberList1 (screenFields);

    if (listMatchesArray (fieldManager.getMenus (), memberMenus))
      return checkMemberList2 (screenFields);

    return false;
  }

  private boolean checkMemberList1 (List<Field> screenFields)
  {
    Field field = screenFields.get (8);
    int location = field.getFirstLocation ();
    if (location != 161)
      return false;

    String mode = field.getText ().trim ();
    int[] tabs1 = null;
    int[] tabs2 = null;

    switch (mode)
    {
      case "LIBRARY":                             // 3.1
        tabs1 = new int[] { 12, 25, 38, 47 };
        tabs2 = new int[] { 12, 21, 31, 43 };
        break;

      case "EDIT":                                // 3.4:e
      case "BROWSE":                              // 3.4:b
      case "VIEW":                                // 3.4:v
      case "DSLIST":                              // 3.4:m
        tabs1 = new int[] { 9, 21, 33, 42 };
        tabs2 = new int[] { 9, 17, 25, 36 };
        break;
      default:
        System.out.printf ("Unexpected mode1: [%s]%n", mode);
        return false;
    }

    field = screenFields.get (9);
    if (field.getFirstLocation () != 179)
      return false;

    String datasetName = field.getText ().trim ();
    currentPDS = datasetName;
    Dataset ds = new Dataset (datasetName);

    List<Field> headings = fieldManager.getRowFields (4);

    for (int row = 5; row < screenDimensions.rows; row++)
    {
      List<Field> rowFields = fieldManager.getRowFields (row);
      if (rowFields.size () != 4 || rowFields.get (1).getText ().equals ("**End** "))
        break;

      String memberName = rowFields.get (1).getText ().trim ();
      Matcher matcher = memberNamePattern.matcher (memberName);
      if (!matcher.matches ())
      {
        System.out.printf ("Invalid member name: %s%n", memberName);
        break;
      }
      String details = rowFields.get (3).getText ();

      //      Dataset member = new Dataset (datasetName + "(" + memberName + ")");
      //      screenMembers.add (member);
      TableDataset member = addMember (datasetName, memberName);
      Member m = new Member (ds, memberName);

      if (headings.size () == 7 || headings.size () == 10)
        screenType1 (member, details, tabs1, m);
      else if (headings.size () == 13)
        screenType2 (member, details, tabs2, m);
      else
        System.out.printf ("Headings size: %d%n", headings.size ());
    }

    return true;
  }

  private boolean checkMemberList2 (List<Field> screenFields)
  {
    Field field = screenFields.get (7);
    int location = field.getFirstLocation ();
    if (location != 161)
      return false;

    String mode = field.getText ().trim ();
    if (!(mode.equals ("EDIT")      // Menu option 1 (browse mode not selected)
        || mode.equals ("BROWSE")   // Menu option 1 (browse mode selected)
        || mode.equals ("VIEW")))   // Menu option 2
    {
      System.out.printf ("Unexpected mode2: [%s]%n", mode);
      return false;
    }

    int[] tabs1 = { 12, 25, 38, 47 };
    int[] tabs2 = { 12, 21, 31, 43 };

    field = screenFields.get (8);
    if (field.getFirstLocation () != 170)
      return false;
    String datasetName = field.getText ().trim ();
    currentPDS = datasetName;
    Dataset ds = new Dataset (datasetName);

    List<Field> headings = fieldManager.getRowFields (4);

    int screenType = 0;
    if (headings.size () == 10
        && fieldManager.textMatchesTrim (headings.get (5), "Created"))
      screenType = 1;
    else if (headings.size () == 13
        && fieldManager.textMatchesTrim (headings.get (5), "Init"))
      screenType = 2;
    else
      dumpFields (headings);

    if (screenType == 0)
      return false;

    for (int row = 5; row < screenDimensions.rows; row++)
    {
      List<Field> rowFields = fieldManager.getRowFields (row);
      if (rowFields.size () != 4 || rowFields.get (1).getText ().equals ("**End** "))
        break;

      String memberName = rowFields.get (1).getText ().trim ();
      Matcher matcher = memberNamePattern.matcher (memberName);
      if (!matcher.matches ())
      {
        System.out.printf ("Invalid member name: %s%n", memberName);
        break;
      }
      String details = rowFields.get (3).getText ();

      TableDataset member = addMember (datasetName, memberName);
      Member m = new Member (ds, memberName);

      if (screenType == 1)
        screenType1 (member, details, tabs1, m);
      else if (screenType == 2)
        screenType2 (member, details, tabs2, m);
      else
        dumpFields (rowFields);
    }

    return true;
  }

  private TableDataset addMember (String pdsName, String memberName)
  {
    String datasetName = pdsName + "(" + memberName.trim () + ")";
    TableDataset member;

    if (siteDatasets.containsKey (datasetName))
      member = siteDatasets.get (datasetName);
    else
    {
      member = new TableDataset (datasetName);
      siteDatasets.put (datasetName, member);
    }

    screenMembers.add (member);

    return member;
  }

  private void screenType1 (TableDataset member, String details, int[] tabs, Member m)
  {
    member.setCreated (details.substring (tabs[0], tabs[1]).trim ());
    member.setReferredDate (details.substring (tabs[1], tabs[2]).trim ());
    member.setReferredTime (details.substring (tabs[2], tabs[3]).trim ());
    member.setCatalog (details.substring (tabs[3]).trim ());
    member.setExtents (getInteger ("Ext:", details.substring (0, tabs[0]).trim ()));

    int size = getInteger ("Size", details.substring (0, tabs[0]).trim ());
    String created = details.substring (tabs[0], tabs[1]);
    String changed = details.substring (tabs[1], tabs[3]);
    String id = details.substring (tabs[3]).trim ();

    m.setDates (created, changed);
    m.setID (id);
    m.setSize (size);

    sendRequest (new MemberRequest (this, UPDATE, m));
  }

  private void screenType2 (TableDataset member, String details, int[] tabs, Member m)
  {
    //    String size = details.substring (0, tabs[0]);
    //    String init = details.substring (tabs[0], tabs[1]);
    //    String mod = details.substring (tabs[1], tabs[2]);
    String vvmm = details.substring (tabs[2], tabs[3]).trim ();
    String id = details.substring (tabs[3]);
    //    System.out.printf ("[%s]%n", vvmm);

    int size = getInteger ("Size", details.substring (0, tabs[0]).trim ());
    int init = getInteger ("Init", details.substring (tabs[0], tabs[1]).trim ());
    int mod = getInteger ("Mod", details.substring (tabs[1], tabs[2]).trim ());

    if (!vvmm.isEmpty ())
    {
      int vv = getInteger ("VV", vvmm.substring (0, 2));
      int mm = getInteger ("MM", vvmm.substring (3));
      m.setSize (size, init, mod, vv, mm);
    }

    member.setCatalog (id.trim ());       // (mis)use the catalog column
    member.setExtents (size);             // (mis)use the extents column

    m.setID (id);

    sendRequest (new MemberRequest (this, UPDATE, m));
  }

  private int getInteger (String id, String value)
  {
    if (value == null || value.isEmpty () || value.equals ("?"))
      return 0;

    try
    {
      return Integer.parseInt (value);
    }
    catch (NumberFormatException e)
    {
      System.out.printf ("ParseInt error with %s: [%s]%n", id, value);
      return 0;
    }
  }

  private void checkSingleDataset (List<Field> fields)
  {
    if (fields.size () < 13)
      return;

    List<Field> rowFields = fieldManager.getRowFields (0, 3);
    if (rowFields.size () == 0)
      return;

    int fldNo = 0;
    for (Field field : rowFields)
    {
      if (field.getFirstLocation () % screenDimensions.columns == 1
          && (field.getDisplayLength () == 10 || field.getDisplayLength () == 9)
          && fldNo + 2 < rowFields.size ())
      {
        String text1 = field.getText ().trim ();
        String text2 = rowFields.get (fldNo + 1).getText ().trim ();
        String text3 = rowFields.get (fldNo + 2).getText ();

        if ((text1.equals ("EDIT") || text1.equals ("VIEW") || text1.equals ("BROWSE"))
            && (text3.equals ("Columns") || text3.equals ("Line")))
        {
          int pos = text2.indexOf (' ');
          String datasetName = pos < 0 ? text2 : text2.substring (0, pos);
          String memberName = "";
          int pos1 = datasetName.indexOf ('(');
          if (pos1 > 0 && datasetName.endsWith (")"))
          {
            memberName = datasetName.substring (pos1 + 1, datasetName.length () - 1);
            datasetName = datasetName.substring (0, pos1);
          }
          Matcher matcher = datasetNamePattern.matcher (datasetName);
          if (matcher.matches ())
          {
            //  System.out.printf ("%-11s %-20s %s%n", text1, datasetName, memberName);
            singleDataset = datasetName;
            if (!memberName.isEmpty ())
              singleDataset += "(" + memberName + ")";
            if (!recentDatasetNames.contains (singleDataset))
              recentDatasetNames.add (singleDataset);
          }
        }
      }
      fldNo++;
    }
  }

  private boolean listMatchesArray (List<String> list, String[] array)
  {
    if (list.size () != array.length)
      return false;
    int i = 0;
    for (String text : list)
      if (!array[i++].equals (text))
        return false;
    return true;
  }

  private void dumpFields (List<Field> fields)
  {
    fields.forEach (System.out::println);
    //    for (Field field : fields)
    //      System.out.println (field);
    System.out.println ("-------------------------");
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append ("Screen details:\n");
    text.append (String.format ("TSO screen ........ %s%n", isTSOCommandScreen));
    text.append (String.format ("Prompt field ...... %s%n", tsoCommandField));
    text.append (String.format ("Prompt line ....... %d%n", promptFieldLine));
    text.append (String.format ("Dataset list ...... %s%n", isDatasetList));
    text.append (String.format ("Members list ...... %s%n", isMemberList));
    text.append (String.format ("Current dataset ... %s%n", currentPDS));
    text.append (String.format ("Single dataset .... %s%n", singleDataset));
    text.append (String.format ("Userid/prefix ..... %s / %s%n", userid, prefix));
    text.append (String.format ("Datasets for ...... %s%n", datasetsMatching));
    text.append (String.format ("Volume ............ %s%n", datasetsOnVolume));
    text.append (String.format ("Datasets .......... %s%n",
                                screenDatasets == null ? "" : screenDatasets.size ()));
    text.append (String
        .format ("Recent datasets ... %s%n",
                 screenDatasets == null ? "" : recentDatasetNames.size ()));
    int i = 0;
    for (String datasetName : recentDatasetNames)
      text.append (String.format ("            %3d ... %s%n", ++i, datasetName));

    return text.toString ();
  }

  private void sendRequest (DatabaseRequest request)
  {
    if (queue != null)
      try
      {
        queue.put (request);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace ();
      }
  }

  @Override
  public void processResult (DatabaseRequest request)
  {
    //    System.out.println (request);
  }
}