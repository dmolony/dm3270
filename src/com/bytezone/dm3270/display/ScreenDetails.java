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
    if (screenFields.size () > 2)
    {
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

      if (false)
      {
        System.out.println (this);
        screen.listTSOCommands ();
      }
    }
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
    if (!"Enter TSO or Workstation commands below:".equals (field.getText ()))
    {
      ++workstationFieldNo;
      field = screenFields.get (workstationFieldNo);
      if (!"Enter TSO or Workstation commands below:".equals (field.getText ()))
        return false;
    }

    List<String> menus = getMenus (screenFields);
    if (menus.size () != tsoMenus.length)
      return false;

    int i = 0;
    for (String menu : menus)
      if (!tsoMenus[i++].equals (menu))
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

    if (rowFields.size () == 3)
    {
      heading = rowFields.get (1).getText ().trim ();
      if (heading.startsWith ("Tracks"))
        screenType = 1;
      else if (heading.startsWith ("Dsorg"))
        screenType = 2;
    }
    else if (rowFields.size () == 4)
    {
      heading = rowFields.get (2).getText ().trim ();
      if ("Volume".equals (heading))
        screenType = 3;
    }
    else if (rowFields.size () == 6)
    {
      if (!datasetsOnVolume.isEmpty ())
      {
        screenType = 5;
        linesPerDataset = 2;
        nextLine = 8;
        datasetsToProcess = Math.min (maxRows, 6);
      }
      else
      {
        screenType = 4;
        linesPerDataset = 3;
        nextLine = 9;
        datasetsToProcess = Math.min (maxRows, 4);
      }
    }
    else
      System.out.printf ("Unexpected number of fields: %d%n", rowFields.size ());

    if (screenType >= 1 && screenType <= 3)
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

    if (screenType == 0)
    {
      System.out.println ("Screen not recognised");
      return false;
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
          dataset = addDataset (rowFields.get (0));
          dataset.setVolume (rowFields.get (2).getText ().trim ());
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
          dataset = addDataset (rowFields.get (0));
          dataset.setVolume (rowFields.get (2).getText ().trim ());

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
    if (!details.trim ().isEmpty ())
    {
      String tracks = details.substring (0, 6);
      String pct = details.substring (7, 10);
      String extents = details.substring (10, 14);
      String device = details.substring (15);

      dataset.setTracks (tracks.trim ());
      dataset.setPercentUsed (pct.trim ());
      dataset.setExtents (extents.trim ());
      dataset.setDevice (device.trim ());
    }
  }

  private void setSpace2 (Dataset dataset, String details)
  {
    if (!details.trim ().isEmpty ())
    {
      String tracks = details.substring (0, 6);
      String pct = details.substring (8, 11);
      String extents = details.substring (11, 15);
      String device = details.substring (17);

      dataset.setTracks (tracks.trim ());
      dataset.setPercentUsed (pct.trim ());
      dataset.setExtents (extents.trim ());
      dataset.setDevice (device.trim ());
    }
  }

  private void setDisposition1 (Dataset dataset, String details)
  {
    if (!details.trim ().isEmpty ())
    {
      String dsorg = details.substring (0, 5);
      String recfm = details.substring (5, 10);
      String lrecl = details.substring (10, 16);
      String blksize = details.substring (16);

      dataset.setDsorg (dsorg.trim ());
      dataset.setRecfm (recfm.trim ());
      dataset.setLrecl (lrecl.trim ());

      int bls = Integer.parseInt (blksize.trim ());
      dataset.setBlksize (String.format ("%,7d", bls));
    }
  }

  private void setDisposition2 (Dataset dataset, String details)
  {
    if (!details.trim ().isEmpty ())
    {
      String dsorg = details.substring (0, 5);
      String recfm = details.substring (6, 11);
      String lrecl = details.substring (12, 18);
      String blksize = details.substring (19);

      dataset.setDsorg (dsorg.trim ());
      dataset.setRecfm (recfm.trim ());
      dataset.setLrecl (lrecl.trim ());

      int bls = Integer.parseInt (blksize.trim ());
      dataset.setBlksize (String.format ("%,7d", bls));
    }
  }

  private void setDates (Dataset dataset, String details)
  {
    if (!details.trim ().isEmpty ())
    {
      dataset.setCreated (details.substring (0, 10).trim ());
      dataset.setExpires (details.substring (11, 20).trim ());
      dataset.setReferred (details.substring (22).trim ());
    }
  }

  private boolean checkMemberList (List<Field> screenFields)
  {
    if (screenFields.size () < 14)
      return false;

    List<String> menus = getMenus (screenFields);
    if (menus.size () != pdsMenus.length)
      return false;

    int i = 0;
    for (String menu : menus)
      if (!pdsMenus[i++].equals (menu))
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

    int rowFrom = Integer.parseInt (screenFields.get (11).getText ().trim ());
    int rowTo = Integer.parseInt (screenFields.get (13).getText ().trim ());

    //    System.out.print ("\nMember list of " + datasetName + " in " + mode + " mode");
    //    System.out.printf ("- row %d of %d%n", rowFrom, rowTo);

    List<Field> headings = getFieldsOnRow (screenFields, 4);
    int maxRows = Math.min (19, rowTo - rowFrom + 1) + 5;

    for (int row = 5; row < maxRows; row++)
    {
      List<Field> rowFields = getFieldsOnRow (screenFields, row);

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
        //        System.out.printf ("%3d [%-8s] [%s] [%s] [%s] [%s] [%s]%n", row - 5 + rowFrom,
        //                           memberName, size, created, modified, time, id);
        member.setCreated (created);
        member.setReferred (modified);
      }
      else if (headings.size () == 13)
      {
        String size = details.substring (3, 9);
        String init = details.substring (11, 17);
        String mod = details.substring (19, 25);
        String vvmm = details.substring (31, 36);
        String id = details.substring (44);
        //        System.out.printf ("%3d [%-8s] [%s] [%s] [%s] [%s] [%s]%n", row - 5 + rowFrom,
        //                           memberName, size, init, mod, vvmm, id);
      }
      else
        System.out.println ("Unexpected headings size: " + headings.size ());
    }
    System.out.println ();

    return true;
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
    {
      String dataset = text.substring (0, pos);
      currentDataset = dataset;
    }
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
    {
      String dataset = text.substring (0, pos);
      currentDataset = dataset;
    }
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

  private List<Field> getFieldsOnRow (List<Field> fields, int requestedRow)
  {
    int firstLocation = requestedRow * screen.columns;
    int lastLocation = firstLocation + screen.columns - 1;
    return getFields (fields, firstLocation, lastLocation);
  }

  private List<Field> getRowFields (List<Field> fields, int requestedRowFrom, int rows)
  {
    int firstLocation = requestedRowFrom * screen.columns;
    int lastLocation = (requestedRowFrom + rows) * screen.columns - 1;
    return getFields (fields, firstLocation, lastLocation);
  }

  private List<Field> getFields (List<Field> fields, int firstLocation, int lastLocation)
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