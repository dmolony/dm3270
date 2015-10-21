package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.assistant.Dataset;

public class ScreenDetails
{
  private static final String[] tsoMenus =
      { "Menu", "List", "Mode", "Functions", "Utilities", "Help" };
  private static final String[] pdsMenus =
      { "Menu", "Functions", "Confirm", "Utilities", "Help" };
  private static final String[] memberMenus =
      { "Menu", "Functions", "Utilities", "Help" };
  private static final String SPLIT_LINE = ".  .  .  .  .  .  .  .  .  .  .  .  .  "
      + ".  .  .  .  .  .  .  .  .  .  .  .  .  .";
  private static final String segment = "[A-Z@#$][-A-Z0-9@#$]{0,7}";
  private static final Pattern datasetNamePattern =
      Pattern.compile (segment + "(\\." + segment + "){0,21}");
  private static final Pattern memberNamePattern = Pattern.compile (segment);

  private static final String ispfScreen = "ISPF Primary Option Menu";
  private static final String zosScreen = "z/OS Primary Option Menu";
  private static final String ispfShell = "ISPF Command Shell";

  private final FieldManager fieldManager;
  private final int screenColumns;
  private final int screenRows;

  private final List<Dataset> datasets = new ArrayList<> ();
  private final List<Dataset> members = new ArrayList<> ();

  private String datasetsMatching;
  private String datasetsOnVolume;

  private Field tsoCommandField;
  private boolean isTSOCommandScreen;
  private boolean isDatasetList;
  private boolean isMemberList;
  private boolean isSplitScreen;

  private String currentDataset = "";
  private String userid = "";
  private String prefix = "";

  public ScreenDetails (FieldManager fieldManager, int rows, int columns)
  {
    this.fieldManager = fieldManager;
    screenRows = rows;
    screenColumns = columns;
  }

  // called by FieldManager after building a new screen
  void check ()
  {
    tsoCommandField = null;
    isTSOCommandScreen = false;
    isDatasetList = false;
    isSplitScreen = false;
    datasets.clear ();
    members.clear ();
    currentDataset = "";

    List<Field> screenFields = fieldManager.getFields ();
    if (screenFields.size () <= 2)
      return;

    isSplitScreen = checkSplitScreen ();
    if (isSplitScreen)
      return;

    if (hasPromptField ())
    {
      if (prefix.isEmpty ())
        checkPrefixScreen (screenFields);       // initial ISPF screen

      isDatasetList = checkDatasetList (screenFields);

      if (!isDatasetList)
      {
        checkEditOrViewDataset (screenFields);
        if (currentDataset.isEmpty ())
          checkBrowseDataset (screenFields);
      }

      if (!isDatasetList)
        isMemberList = checkMemberList (screenFields);
    }
    else
      isTSOCommandScreen = checkTSOCommandScreen (screenFields);
  }

  public Field getTSOCommandField ()
  {
    return tsoCommandField;
  }

  public boolean isTSOCommandScreen ()
  {
    return isTSOCommandScreen;
  }

  public boolean isDatasetList ()
  {
    return isDatasetList;
  }

  public boolean isMemberList ()
  {
    return isMemberList;
  }

  public String getCurrentDataset ()
  {
    return currentDataset;
  }

  public String getUserid ()
  {
    return userid;
  }

  public String getPrefix ()
  {
    return prefix;
  }

  public List<Dataset> getDatasets ()
  {
    return datasets;
  }

  public List<Dataset> getMembers ()
  {
    return members;
  }

  private boolean checkSplitScreen ()
  {
    for (Field field : fieldManager.getFields ())
    {
      if (!field.isProtected ())
        continue;
      if (field.getDisplayLength () != 79)
        continue;
      if (field.getFirstLocation () % screenColumns != 1)
        continue;
      if (SPLIT_LINE.equals (field.getText ()))
        return true;
    }
    return false;
  }

  private boolean hasPromptField ()
  {
    List<Field> rowFields = fieldManager.getRowFields (2, 2);
    for (int i = 0; i < rowFields.size (); i++)
    {
      Field field = rowFields.get (i);
      String text = field.getText ();
      int column = field.getFirstLocation () % screenColumns;
      int nextFieldNo = i + 1;
      if (nextFieldNo < rowFields.size () && column == 1
          && ("Command ===>".equals (text) || "Option ===>".equals (text)))
      {
        Field nextField = rowFields.get (nextFieldNo);
        int length = nextField.getDisplayLength ();
        boolean modifiable = nextField.isUnprotected ();
        boolean hidden = nextField.isHidden ();

        if (length == 66 || length == 48 && !hidden && modifiable)
        {
          tsoCommandField = nextField;
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

    while (nextLine < screenRows)
    {
      rowFields = fieldManager.getRowFields (nextLine, linesPerDataset);
      if (rowFields.size () <= 1)
        break;

      String lineText = rowFields.get (0).getText ();
      System.out.println (lineText);
      if (lineText.isEmpty () || lineText.length () < 10)
        break;
      String datasetName = lineText.substring (9).trim ();
      if (datasetName.length () > 44)
      {
        System.out.printf ("Dataset name too long: %s%n", datasetName);
        break;
      }
      Matcher matcher = datasetNamePattern.matcher (datasetName);
      if (!matcher.matches ())
      {
        // what about GDGs?
        System.out.printf ("Invalid dataset name: %s%n", datasetName);
        break;
      }
      Dataset dataset = new Dataset (datasetName);
      datasets.add (dataset);

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
            matcher = datasetNamePattern.matcher (catalog);
            if (matcher.matches ())
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

      nextLine += linesPerDataset;
      if (linesPerDataset > 1)
        nextLine++;// skip the row of hyphens
    }

    return true;
  }

  private void setSpace (Dataset dataset, String details, int t1, int t2, int t3)
  {
    if (details.trim ().isEmpty ())
      return;

    dataset.setTracks (getInteger ("tracks", details.substring (0, t1).trim ()));
    dataset.setPercentUsed (getInteger ("pct", details.substring (t1, t2).trim ()));
    dataset.setExtents (getInteger ("ext", details.substring (t2, t3).trim ()));
    dataset.setDevice (details.substring (t3).trim ());
  }

  private void setDisposition (Dataset dataset, String details, int t1, int t2, int t3)
  {
    if (details.trim ().isEmpty ())
      return;

    dataset.setDsorg (details.substring (0, t1).trim ());
    dataset.setRecfm (details.substring (t1, t2).trim ());
    dataset.setLrecl (getInteger ("lrecl", details.substring (t2, t3).trim ()));
    dataset.setBlksize (getInteger ("blksize", details.substring (t3).trim ()));
  }

  private void setDates (Dataset dataset, String details)
  {
    if (details.trim ().isEmpty ())
      return;

    dataset.setCreated (details.substring (0, 11).trim ());
    dataset.setExpires (details.substring (11, 22).trim ());
    dataset.setReferred (details.substring (22).trim ());
  }

  private boolean checkMemberList (List<Field> screenFields)
  {
    if (screenFields.size () < 14)
      return false;

    if (!listMatchesArray (fieldManager.getMenus (), pdsMenus))
      return checkMemberList2 (screenFields);

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

    List<Field> headings = fieldManager.getRowFields (4);

    for (int row = 5; row < screenRows; row++)
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

      Dataset member = new Dataset (datasetName + "(" + memberName + ")");
      members.add (member);

      if (headings.size () == 7 || headings.size () == 10)
        screenType1 (member, details, tabs1);
      else if (headings.size () == 13)
        screenType2 (member, details, tabs2);
    }

    return true;
  }

  private boolean checkMemberList2 (List<Field> screenFields)
  {
    if (!listMatchesArray (fieldManager.getMenus (), memberMenus))
      return false;

    Field field = screenFields.get (7);
    int location = field.getFirstLocation ();
    if (location != 161)
      return false;

    String mode = field.getText ().trim ();
    if (!(mode.equals ("EDIT")      // Menu option 1 (browse mode not selected)
        || mode.equals ("BROWSE")   // Menu option 1 (browse mode selected)
        || mode.equals ("VIEW")     // Menu option 2
    ))
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

    for (int row = 5; row < screenRows; row++)
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

      Dataset member = new Dataset (datasetName + "(" + memberName.trim () + ")");
      members.add (member);

      if (screenType == 1)
        screenType1 (member, details, tabs1);
      else if (screenType == 2)
        screenType2 (member, details, tabs2);
      else
        dumpFields (rowFields);
    }

    return true;
  }

  private void screenType1 (Dataset member, String details, int[] tabs)
  {
    String size = details.substring (0, tabs[0]);
    String created = details.substring (tabs[0], tabs[1]);
    String modified = details.substring (tabs[1], tabs[2]);
    String time = details.substring (tabs[2], tabs[3]);
    String id = details.substring (tabs[3]);

    member.setCreated (created.trim ());
    member.setReferred (modified.trim ());
    member.setCatalog (id.trim ());
    member.setExtents (getInteger ("Ext:", size.trim ()));
  }

  private void screenType2 (Dataset member, String details, int[] tabs)
  {
    String size = details.substring (0, tabs[0]);
    String init = details.substring (tabs[0], tabs[1]);
    String mod = details.substring (tabs[1], tabs[2]);
    String vvmm = details.substring (tabs[2], tabs[3]);
    String id = details.substring (tabs[3]);

    member.setCatalog (id);
    member.setExtents (getInteger ("Ext:", size.trim ()));
  }

  private int getInteger (String id, String value)
  {
    if (value == null || value.isEmpty ())
      return 0;

    try
    {
      return Integer.parseInt (value);
    }
    catch (NumberFormatException e)
    {
      System.out.printf ("Error with %s: [%s]%n", id, value);
      return 0;
    }
  }

  private void checkEditOrViewDataset (List<Field> fields)
  {
    if (fields.size () < 13)
      return;

    Field field = fields.get (11);
    if (field.getFirstLocation () != 161)
      return;

    String text = field.getText ().trim ();
    if (!text.equals ("EDIT") && !text.equals ("VIEW"))
      return;

    field = fields.get (12);
    if (field.getFirstLocation () != 172)
      return;

    text = field.getText ().trim ();
    int pos = text.indexOf (' ');
    if (pos > 0)
      currentDataset = text.substring (0, pos);
  }

  private void checkBrowseDataset (List<Field> fields)
  {
    if (fields.size () < 9)
      return;

    if (!fieldManager.textMatches (fields.get (7), "BROWSE   ", 161))
      return;

    Field field = fields.get (8);
    if (field.getFirstLocation () != 171)
      return;

    String text = field.getText ().trim ();
    int pos = text.indexOf (' ');
    currentDataset = pos > 0 ? text.substring (0, pos) : text;
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
    for (Field field : fields)
      System.out.println (field);
    System.out.println ("-------------------------");
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append ("Screen details:\n");
    text.append (String.format ("TSO screen ..... %s%n", isTSOCommandScreen));
    text.append (String.format ("Prompt field ... %s%n", tsoCommandField));
    text.append (String.format ("Dataset list ... %s%n", isDatasetList));
    text.append (String.format ("Members list ... %s%n", isMemberList));
    text.append (String.format ("Userid/prefix .. %s / %s%n", userid, prefix));
    text.append (String.format ("Datasets for ... %s%n", datasetsMatching));
    text.append (String.format ("Volume ......... %s%n", datasetsOnVolume));
    text.append (String.format ("Datasets ....... %s%n",
                                datasets == null ? "" : datasets.size ()));

    return text.toString ();
  }
}