package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.assistant.Dataset;

public class ScreenWatcher
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

  private final Map<String, Dataset> siteDatasets = new TreeMap<> ();
  private final List<Dataset> screenDatasets = new ArrayList<> ();
  private final List<Dataset> screenMembers = new ArrayList<> ();
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

  public ScreenWatcher (FieldManager fieldManager, ScreenDimensions screenDimensions)
  {
    this.fieldManager = fieldManager;
    this.screenDimensions = screenDimensions;
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

  public Optional<Dataset> getDataset (String datasetName)
  {
    if (siteDatasets.containsKey (datasetName))
      return Optional.of (siteDatasets.get (datasetName));
    return Optional.empty ();
  }

  public List<Dataset> getDatasets ()
  {
    return screenDatasets;
  }

  public List<Dataset> getMembers ()
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
    Dataset dataset;
    if (siteDatasets.containsKey (datasetName))
      dataset = siteDatasets.get (datasetName);
    else
    {
      dataset = new Dataset (datasetName);
      siteDatasets.put (datasetName, dataset);
    }

    screenDatasets.add (dataset);

    switch (screenType)
    {
      case 1:
        if (rowFields.size () == 2)
          setSpace (dataset, rowFields.get (1).getText (), 6, 11, 15);
        break;

      case 2:
        if (rowFields.size () == 2)
          setDisposition (dataset, rowFields.get (1).getText (), 5, 11, 18);
        break;

      case 3:
        if (rowFields.size () == 3)
          dataset.setVolume (rowFields.get (2).getText ().trim ());
        break;

      case 4:
        if (rowFields.size () == 7)
        {
          dataset.setVolume (rowFields.get (2).getText ().trim ());
          setSpace (dataset, rowFields.get (3).getText (), 6, 10, 14);
          setDisposition (dataset, rowFields.get (4).getText (), 5, 10, 16);
          setDates (dataset, rowFields.get (5).getText ());

          String catalog = rowFields.get (6).getText ().trim ();
          if (datasetNamePattern.matcher (catalog).matches ())
            dataset.setCatalog (catalog);
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
            setDates (dataset, rowFields.get (5).getText ());
          }
        }
        break;
    }
  }

  private void setSpace (Dataset dataset, String details, int t1, int t2, int t3)
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

  private void setDisposition (Dataset dataset, String details, int t1, int t2, int t3)
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

  private void setDates (Dataset dataset, String details)
  {
    if (details.trim ().isEmpty ())
      return;

    dataset.setCreated (details.substring (0, 11).trim ());
    dataset.setExpires (details.substring (11, 22).trim ());
    dataset.setReferredDate (details.substring (22).trim ());
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
      Dataset member = addMember (datasetName, memberName);

      if (headings.size () == 7 || headings.size () == 10)
        screenType1 (member, details, tabs1);
      else if (headings.size () == 13)
        screenType2 (member, details, tabs2);
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

      Dataset member = addMember (datasetName, memberName);

      if (screenType == 1)
        screenType1 (member, details, tabs1);
      else if (screenType == 2)
        screenType2 (member, details, tabs2);
      else
        dumpFields (rowFields);
    }

    return true;
  }

  private Dataset addMember (String pdsName, String memberName)
  {
    String datasetName = pdsName + "(" + memberName.trim () + ")";
    Dataset member;

    if (siteDatasets.containsKey (datasetName))
      member = siteDatasets.get (datasetName);
    else
    {
      member = new Dataset (datasetName);
      siteDatasets.put (datasetName, member);
    }

    screenMembers.add (member);

    return member;
  }

  private void screenType1 (Dataset member, String details, int[] tabs)
  {
    member.setCreated (details.substring (tabs[0], tabs[1]).trim ());
    member.setReferredDate (details.substring (tabs[1], tabs[2]).trim ());
    member.setReferredTime (details.substring (tabs[2], tabs[3]).trim ());
    member.setCatalog (details.substring (tabs[3]).trim ());
    member.setExtents (getInteger ("Ext:", details.substring (0, tabs[0]).trim ()));
  }

  private void screenType2 (Dataset member, String details, int[] tabs)
  {
    String size = details.substring (0, tabs[0]);
    String init = details.substring (tabs[0], tabs[1]);
    String mod = details.substring (tabs[1], tabs[2]);
    String vvmm = details.substring (tabs[2], tabs[3]);
    String id = details.substring (tabs[3]);

    member.setCatalog (id.trim ());
    member.setExtents (getInteger ("Ext:", size.trim ()));
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
}