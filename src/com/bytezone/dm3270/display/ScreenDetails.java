package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.assistant.Dataset;

public class ScreenDetails
{
  private static final String[] tsoMenus =
      { "Menu", "List", "Mode", "Functions", "Utilities", "Help" };
  private static final String[] pdsMenus =
      { "Menu", "Functions", "Confirm", "Utilities", "Help" };

  private static String ispfScreen = "ISPF Primary Option Menu";
  private static String zosScreen = "z/OS Primary Option Menu";
  private static String ispfShell = "ISPF Command Shell";

  private final Screen screen;

  private final List<Dataset> datasets = new ArrayList<> ();
  private final List<Dataset> members = new ArrayList<> ();

  private String datasetsMatching;
  private String datasetsOnVolume;

  private Field tsoCommandField;
  private boolean isTSOCommandScreen;
  private boolean isDatasetList;
  private boolean isMemberList;

  private String currentDataset = "";
  private String userid = "";
  private String prefix = "";

  public ScreenDetails (Screen screen)
  {
    this.screen = screen;
  }

  public void check (FieldManager fieldManager)
  {
    tsoCommandField = null;
    isTSOCommandScreen = false;
    datasets.clear ();
    members.clear ();
    currentDataset = "";

    List<Field> screenFields = fieldManager.getFields ();
    if (screenFields.size () <= 2)
      return;

    if (hasPromptField (screenFields))
    {
      if (prefix.isEmpty ())
        checkPrefixScreen (screenFields);// initial ISPF screen

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

  public boolean isKeyboardLocked ()
  {
    return screen.isKeyboardLocked ();
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

  private boolean hasPromptField (List<Field> screenFields)
  {
    List<Field> rowFields = getRowFields (screenFields, 2, 2);
    for (int i = 0; i < rowFields.size (); i++)
    {
      Field field = rowFields.get (i);
      String text = field.getText ();
      int column = field.getFirstLocation () % screen.columns;
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
    if (screenFields.size () < 73)
      return;

    Field field = screenFields.get (10);
    String heading = field.getText ();
    if (!ispfScreen.equals (heading) && !zosScreen.equals (heading))
      return;

    field = screenFields.get (23);
    if (!" User ID . :".equals (field.getText ()))
      return;
    if (field.getFirstLocation () != 457)
      return;

    field = screenFields.get (24);
    if (field.getFirstLocation () != 470)
      return;

    userid = field.getText ().trim ();

    field = screenFields.get (72);
    if (!" TSO prefix:".equals (field.getText ()))
      return;
    if (field.getFirstLocation () != 1017)
      return;

    field = screenFields.get (73);
    if (field.getFirstLocation () != 1030)
      return;

    prefix = field.getText ().trim ();
  }

  private boolean checkTSOCommandScreen (List<Field> screenFields)
  {
    if (screenFields.size () < 14)
      return false;

    Field field = screenFields.get (10);
    if (!ispfShell.equals (field.getText ()))
      return false;

    int workstationFieldNo = 13;
    field = screenFields.get (workstationFieldNo);
    String workstationText = "Enter TSO or Workstation commands below:";
    if (!workstationText.equals (field.getText ()))
    {
      ++workstationFieldNo;
      field = screenFields.get (workstationFieldNo);
      if (!workstationText.equals (field.getText ()))
        return false;
    }

    if (!listMatchesArray (getMenus (screenFields), tsoMenus))
      return false;

    field = screenFields.get (workstationFieldNo + 5);
    if (field.getDisplayLength () != 234)
      return false;

    tsoCommandField = field;

    return true;
  }

  private boolean checkDatasetList (List<Field> screenFields)
  {
    datasetsOnVolume = "";
    datasetsMatching = "";

    if (screenFields.size () < 21)
      return false;

    List<Field> rowFields = getRowFields (screenFields, 2, 2);
    if (rowFields.size () == 0)
      return false;

    String text = rowFields.get (0).getText ();
    if (!text.startsWith ("DSLIST - Data Sets "))
      return false;

    String rowText = "";
    String locationText = "";

    int firstRow = 0;
    int totalRows = 0;
    int maxRows = 0;

    int pos = text.indexOf ("Row ");
    if (pos > 0)
    {
      rowText = text.substring (pos + 4);
      locationText = text.substring (19, pos).trim ();

      pos = rowText.indexOf (" of ");
      if (pos > 0)
      {
        firstRow = Integer.parseInt (rowText.substring (0, pos).trim ());
        totalRows = Integer.parseInt (rowText.substring (pos + 4).trim ());
        maxRows = totalRows - firstRow + 1;
      }
    }

    if (locationText.startsWith ("on volume "))
      datasetsOnVolume = locationText.substring (10);
    else if (locationText.startsWith ("Matching "))
      datasetsMatching = locationText.substring (9);

    if (false)
    {
      System.out.printf ("%n[%s]%n", text);
      System.out.printf ("First row : %d%n", firstRow);
      System.out.printf ("Total rows: %d%n", totalRows);
      System.out.printf ("Max rows  : %d%n%n", maxRows);
    }

    rowFields = getRowFields (screenFields, 5, 2);
    if (rowFields.size () == 0)
      return false;

    text = rowFields.get (0).getText ();
    if (!text.startsWith ("Command - Enter"))
      return false;

    String heading = "";
    int screenType = 0;
    int linesPerDataset = 0;
    int datasetsToProcess = 0;
    int nextLine = 0;

    System.out.println (rowFields.size ());
    switch (rowFields.size ())
    {
      case 3:
        heading = rowFields.get (1).getText ().trim ();
        if (heading.startsWith ("Tracks"))
          screenType = 1;
        else if (heading.startsWith ("Dsorg"))
          screenType = 2;
        else
          System.out.println ("Not 1 or 2");
        break;

      case 4:
        heading = rowFields.get (2).getText ().trim ();
        if ("Volume".equals (heading))
          screenType = 3;
        else
          System.out.println ("Not 3");
        break;

      case 6:
        List<Field> rowFields2 = getRowFields (screenFields, 7);
        if (rowFields2.size () == 1)
        {
          String line = rowFields2.get (0).getText ().trim ();
          if (line.equals ("Catalog"))
          {
            screenType = 4;
            linesPerDataset = 3;
            nextLine = 9;
            datasetsToProcess = Math.min (maxRows, 4);
          }
          else if (line.startsWith ("--"))
          {
            screenType = 5;
            linesPerDataset = 2;
            nextLine = 8;
            datasetsToProcess = Math.min (maxRows, 6);
          }
          else
            System.out.println ("Expected 'Catalog'");
        }
        else
          System.out.println ("Not 4 or 5");
        break;

      default:
        System.out.printf ("Unexpected number of fields: %d%n", rowFields.size ());
    }

    if (screenType == 0)
    {
      System.out.println ("Screen not recognised");
      return false;
    }

    if (screenType <= 3)
    {
      linesPerDataset = 1;
      nextLine = 7;
      datasetsToProcess = Math.min (maxRows, 17);
    }

    if (false)
    {
      System.out.printf ("Screen type        : %d%n", screenType);
      System.out.printf ("Lines per dataset  : %d%n", linesPerDataset);
      System.out.printf ("First line         : %d%n", nextLine);
      System.out.printf ("Datasets to process: %d%n%n", datasetsToProcess);
    }

    while (datasetsToProcess > 0)
    {
      Dataset dataset = null;
      rowFields = getRowFields (screenFields, nextLine, linesPerDataset);

      switch (screenType)
      {
        case 1:
          if (rowFields.size () == 2)
          {
            dataset = addDataset (rowFields.get (0));
            setSpace2 (dataset, rowFields.get (1).getText ());
          }
          break;

        case 2:
          if (rowFields.size () == 2)
          {
            dataset = addDataset (rowFields.get (0));
            setDisposition2 (dataset, rowFields.get (1).getText ());
          }
          break;

        case 3:
          if (rowFields.size () == 3)
          {
            dataset = addDataset (rowFields.get (0));
            dataset.setVolume (rowFields.get (2).getText ().trim ());
          }
          break;

        case 4:
          if (rowFields.size () == 7)
          {
            dataset = addDataset (rowFields.get (0));
            dataset.setVolume (rowFields.get (2).getText ().trim ());
            setSpace1 (dataset, rowFields.get (3).getText ());
            setDisposition1 (dataset, rowFields.get (4).getText ());
            setDates (dataset, rowFields.get (5).getText ());
            dataset.setCatalog (rowFields.get (6).getText ().trim ());
          }

          nextLine++;// skip the row of hyphens
          break;

        case 5:
          if (rowFields.size () >= 3)
          {
            dataset = addDataset (rowFields.get (0));
            dataset.setVolume (rowFields.get (2).getText ().trim ());
          }

          if (rowFields.size () >= 6)
          {
            setSpace1 (dataset, rowFields.get (3).getText ());
            setDisposition1 (dataset, rowFields.get (4).getText ());
            setDates (dataset, rowFields.get (5).getText ());
          }

          nextLine++;// skip the row of hyphens
          break;
      }

      datasetsToProcess--;
      nextLine += linesPerDataset;
    }

    return true;
  }

  private Dataset addDataset (Field field)
  {
    Dataset dataset = new Dataset (field.getText ().trim ());
    datasets.add (dataset);
    return dataset;
  }

  private void setSpace1 (Dataset dataset, String details)
  {
    if (details.trim ().isEmpty ())
      return;

    String tracks = details.substring (0, 6);
    String pct = details.substring (7, 10);
    String extents = details.substring (10, 14);
    String device = details.substring (15);

    try
    {
      dataset.setTracks (Integer.parseInt (tracks.trim ()));
      dataset.setPercentUsed (Integer.parseInt (pct.trim ()));
      dataset.setExtents (Integer.parseInt (extents.trim ()));
      dataset.setDevice (device.trim ());
    }
    catch (NumberFormatException e)
    {
      System.out.printf ("NFE: %s%n", details);
    }
  }

  private void setSpace2 (Dataset dataset, String details)
  {
    if (details.trim ().isEmpty ())
      return;

    String tracks = details.substring (0, 6);
    String pct = details.substring (8, 11);
    String extents = details.substring (11, 15);
    String device = details.substring (17);

    try
    {
      dataset.setTracks (Integer.parseInt (tracks.trim ()));
      dataset.setPercentUsed (Integer.parseInt (pct.trim ()));
      dataset.setExtents (Integer.parseInt (extents.trim ()));
      dataset.setDevice (device.trim ());
    }
    catch (NumberFormatException e)
    {
      System.out.printf ("NFE: %s%n", details);
    }
  }

  private void setDisposition1 (Dataset dataset, String details)
  {
    if (details.trim ().isEmpty ())
      return;

    String dsorg = details.substring (0, 5);
    String recfm = details.substring (5, 10);
    String lrecl = details.substring (10, 16);
    String blksize = details.substring (16);

    dataset.setDsorg (dsorg.trim ());
    dataset.setRecfm (recfm.trim ());
    try
    {
      dataset.setLrecl (Integer.parseInt (lrecl.trim ()));
      dataset.setBlksize (Integer.parseInt (blksize.trim ()));
    }
    catch (NumberFormatException e)
    {
      System.out.printf ("NFE: %s%n", details);
    }
  }

  private void setDisposition2 (Dataset dataset, String details)
  {
    if (details.trim ().isEmpty ())
      return;

    String dsorg = details.substring (0, 5);
    String recfm = details.substring (6, 11);
    String lrecl = details.substring (12, 18);
    String blksize = details.substring (19);

    dataset.setDsorg (dsorg.trim ());
    dataset.setRecfm (recfm.trim ());

    try
    {
      dataset.setLrecl (Integer.parseInt (lrecl.trim ()));
      dataset.setBlksize (Integer.parseInt (blksize.trim ()));
    }
    catch (NumberFormatException e)
    {
      System.out.printf ("NFE: %s%n", details);
    }
  }

  private void setDates (Dataset dataset, String details)
  {
    if (details.trim ().isEmpty ())
      return;

    dataset.setCreated (details.substring (0, 10).trim ());
    dataset.setExpires (details.substring (11, 20).trim ());
    dataset.setReferred (details.substring (22).trim ());
  }

  private boolean checkMemberList (List<Field> screenFields)
  {
    if (screenFields.size () < 14)
      return false;

    if (!listMatchesArray (getMenus (screenFields), pdsMenus))
      return false;

    Field field = screenFields.get (8);
    int location = field.getFirstLocation ();
    if (location != 161)
      return false;

    String mode = field.getText ().trim ();
    if (!mode.equals ("EDIT") && !mode.equals ("BROWSE"))
      System.out.printf ("Unexpected mode: [%s]%n", mode);

    field = screenFields.get (9);
    if (field.getFirstLocation () != 179)
      return false;
    String datasetName = field.getText ().trim ();

    field = screenFields.get (10);
    if (field.getFirstLocation () != 221)
      return false;
    String rowText = field.getText ().trim ();
    if (!"Row".equals (rowText))
      return false;

    field = screenFields.get (12);
    if (field.getFirstLocation () != 231)
      return false;
    String ofText = field.getText ().trim ();
    if (!"of".equals (ofText))
      return false;

    int rowFrom = getInteger ("RowFrom", screenFields.get (11).getText ().trim ());
    int rowTo = getInteger ("RowTo", screenFields.get (13).getText ().trim ());

    List<Field> headings = getRowFields (screenFields, 4);
    int maxRows = Math.min (19, rowTo - rowFrom + 1) + 5;

    for (int row = 5; row < maxRows; row++)
    {
      List<Field> rowFields = getRowFields (screenFields, row);

      String memberName = rowFields.get (1).getText ().trim ();
      Dataset member = new Dataset (datasetName + "(" + memberName + ")");
      members.add (member);

      String details = rowFields.get (3).getText ();

      if (headings.size () == 7)
      {
        String size = details.substring (3, 9);
        String created = details.substring (11, 21);
        String modified = details.substring (23, 33);
        String time = details.substring (34, 42);
        String id = details.substring (44);

        member.setCreated (created);
        member.setReferred (modified);
        member.setCatalog (id);
        member.setExtents (getInteger ("Ext1:" + memberName, size.trim ()));
      }
      else if (headings.size () == 13)
      {
        String size = details.substring (3, 9);
        String init = details.substring (11, 17);
        String mod = details.substring (19, 25);
        String vvmm = details.substring (31, 36);
        String id = details.substring (44);

        member.setCatalog (id);
        member.setExtents (getInteger ("Ext2:" + memberName, size.trim ()));
      }
      else
        System.out.println ("Unexpected headings size: " + headings.size ());
    }

    return true;
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
    int location = field.getFirstLocation ();
    if (location != 161)
      return;

    String text = field.getText ().trim ();
    if (!text.equals ("EDIT") && !text.equals ("VIEW"))
      return;

    field = fields.get (12);
    location = field.getFirstLocation ();
    if (location != 172)
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

    Field field = fields.get (7);
    int location = field.getFirstLocation ();
    if (location != 161)
      return;

    String text = field.getText ();
    if (!text.equals ("BROWSE   "))
      return;

    field = fields.get (8);
    location = field.getFirstLocation ();
    if (location != 171)
      return;

    text = field.getText ().trim ();
    int pos = text.indexOf (' ');
    if (pos > 0)
      currentDataset = text.substring (0, pos);
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

  private List<String> getMenus (List<Field> screenFields)
  {
    List<String> menus = new ArrayList<> ();

    for (Field field : screenFields)
    {
      if (field.getFirstLocation () >= screen.columns)
        break;

      if (field.isProtected () && field.isVisible () && field.getDisplayLength () > 1)
      {
        String text = field.getText ().trim ();
        if (!text.isEmpty ())
          menus.add (text);
      }
    }

    return menus;
  }

  private List<Field> getRowFields (List<Field> fields, int requestedRow)
  {
    int firstLocation = requestedRow * screen.columns;
    int lastLocation = firstLocation + screen.columns - 1;
    return getFieldsInRange (fields, firstLocation, lastLocation);
  }

  private List<Field> getRowFields (List<Field> fields, int requestedRowFrom, int rows)
  {
    int firstLocation = requestedRowFrom * screen.columns;
    int lastLocation = (requestedRowFrom + rows) * screen.columns - 1;
    return getFieldsInRange (fields, firstLocation, lastLocation);
  }

  private List<Field> getFieldsInRange (List<Field> fields, int firstLocation,
      int lastLocation)
  {
    List<Field> rowFields = new ArrayList<> ();
    for (Field field : fields)
    {
      int location = field.getFirstLocation ();
      if (location < firstLocation)
        continue;
      if (location > lastLocation)
        break;
      if (field.getDisplayLength () > 0)
        rowFields.add (field);
    }
    return rowFields;
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