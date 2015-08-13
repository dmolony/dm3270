package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.datasets.Dataset;

public class ScreenDetails
{
  private static final String[] tsoMenus =
      { "Menu", "List", "Mode", "Functions", "Utilities", "Help" };
  private static final String[] pdsMenus =
      { "Menu", "Functions", "Confirm", "Utilities", "Help" };

  private final Screen screen;

  private FieldManager fieldManager;
  private List<Field> fields;
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
    this.fieldManager = fieldManager;

    tsoCommandField = null;
    isTSOCommandScreen = false;
    datasets.clear ();
    members.clear ();

    fields = fieldManager.getFields ();
    if (fields.size () > 2)
      checkTSOCommandField ();
  }

  public Field getTSOCommandField ()
  {
    return tsoCommandField;
  }

  public boolean isTSOCommandScreen ()
  {
    return isTSOCommandScreen;
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

  private void checkTSOCommandField ()
  {
    int maxLocation = screen.columns * 5 + 20;
    int minLocation = screen.columns;
    boolean promptFound = false;

    for (Field field : fieldManager.getFields ())
    {
      //      System.out.println (field);
      if (field.getFirstLocation () > maxLocation)
        break;

      if (field.getFirstLocation () < minLocation)
        continue;

      int length = field.getDisplayLength ();

      if (promptFound)
      {
        if (field.isProtected () || field.isHidden ())
          break;

        if (length < 48 || (length > 70 && length != 234))
          break;

        tsoCommandField = field;
        break;
      }

      int column = field.getFirstLocation () % screen.columns;
      if (column > 2)
        continue;

      if (field.isUnprotected () || field.isHidden () || length < 4 || length > 15)
        continue;

      String text = field.getText ();

      if (text.endsWith ("===>"))
        promptFound = true;// next loop iteration will return the field
    }

    if (promptFound)
    {
      isTSOCommandScreen = checkTSOCommandScreen ();

      if (prefix.isEmpty ())
        checkPrefixScreen ();

      currentDataset = "";
      isDatasetList = checkDatasetList ();

      if (!isDatasetList)
      {
        checkEditOrViewDataset ();
        if (currentDataset.isEmpty ())
          checkBrowseDataset ();
      }

      if (!isDatasetList)
        isMemberList = checkMemberList ();
    }
  }

  private void checkPrefixScreen ()
  {
    if (fields.size () < 73)
      return;

    String ispfScreen = "ISPF Primary Option Menu";

    Field field = fields.get (10);
    if (!ispfScreen.equals (field.getText ()))
      return;

    field = fields.get (23);
    if (!" User ID . :".equals (field.getText ()))
      return;
    if (field.getFirstLocation () != 457)
      return;

    field = fields.get (24);
    if (field.getFirstLocation () != 470)
      return;

    userid = field.getText ().trim ();

    field = fields.get (72);
    if (!" TSO prefix:".equals (field.getText ()))
      return;
    if (field.getFirstLocation () != 1017)
      return;

    field = fields.get (73);
    if (field.getFirstLocation () != 1030)
      return;

    prefix = field.getText ().trim ();
  }

  private boolean checkTSOCommandScreen ()
  {
    if (fields.size () < 14)
      return false;

    Field field = fields.get (10);
    if (!"ISPF Command Shell".equals (field.getText ()))
      return false;

    int workstationFieldNo = 13;
    field = fields.get (workstationFieldNo);
    if (!"Enter TSO or Workstation commands below:".equals (field.getText ()))
    {
      ++workstationFieldNo;
      field = fields.get (workstationFieldNo);
      if (!"Enter TSO or Workstation commands below:".equals (field.getText ()))
        return false;
    }

    List<String> menus = getMenus ();
    if (menus.size () != tsoMenus.length)
      return false;

    int i = 0;
    for (String menu : menus)
      if (!tsoMenus[i++].equals (menu))
        return false;

    field = fields.get (workstationFieldNo + 5);
    if (field.getDisplayLength () != 234)
      return false;

    return true;
  }

  private boolean checkDatasetList ()
  {
    datasetsOnVolume = "";
    datasetsMatching = "";

    if (fields.size () < 21)
      return false;

    List<Field> fields = getRowFields (2, 2);
    if (fields.size () == 0)
      return false;

    String text = fields.get (0).getText ();
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

    fields = getRowFields (5, 2);
    if (fields.size () == 0)
      return false;

    text = fields.get (0).getText ();
    if (!text.startsWith ("Command - Enter"))
      return false;

    String heading = "";
    int screenType = 0;
    int linesPerDataset = 0;
    int datasetsToProcess = 0;
    int nextLine = 0;

    if (fields.size () == 3)
    {
      heading = fields.get (1).getText ().trim ();
      if (heading.startsWith ("Tracks"))
        screenType = 1;
      else if (heading.startsWith ("Dsorg"))
        screenType = 2;
    }
    else if (fields.size () == 4)
    {
      heading = fields.get (2).getText ().trim ();
      if ("Volume".equals (heading))
        screenType = 3;
    }
    else if (fields.size () == 6)
    {
      //      fields = getRowFields (7, 1);
      //      if (fields.get (0).getText ().startsWith ("---"))
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
      System.out.printf ("Unexpected number of fields: %d%n", fields.size ());

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
      String datasetName = "";
      Dataset dataset = null;
      fields = getRowFields (nextLine, linesPerDataset);
      switch (screenType)
      {
        case 1:
          if (fields.size () != 2)
            break;

          datasetName = fields.get (0).getText ().trim ();
          dataset = new Dataset (datasetName);
          datasets.add (dataset);
          String details = fields.get (1).getText ();

          if (details.trim ().isEmpty ())
            break;

          dataset.setTracks (details.substring (0, 6).trim ());
          dataset.setPercentUsed (details.substring (7, 11).trim ());
          dataset.setExtents (details.substring (12, 15).trim ());
          dataset.setDevice (details.substring (17).trim ());

          break;

        case 2:
          if (fields.size () != 2)
            break;

          datasetName = fields.get (0).getText ().trim ();
          dataset = new Dataset (datasetName);
          datasets.add (dataset);
          details = fields.get (1).getText ();

          if (details.trim ().isEmpty ())
            break;

          dataset.setDsorg (details.substring (0, 5).trim ());
          dataset.setRecfm (details.substring (6, 11).trim ());
          dataset.setLrecl (details.substring (12, 19).trim ());
          String blkSize = details.substring (20).trim ();
          int bls = Integer.parseInt (blkSize);
          dataset.setBlksize (String.format ("%,7d", bls));

          break;

        case 3:
          datasetName = fields.get (0).getText ().trim ();
          dataset = new Dataset (datasetName);
          datasets.add (dataset);
          details = fields.get (2).getText ();
          dataset.setVolume (details.trim ());
          break;

        case 4:
          if (fields.size () != 7)
            break;

          datasetName = fields.get (0).getText ().trim ();
          dataset = new Dataset (datasetName);
          datasets.add (dataset);

          details = fields.get (2).getText ();
          dataset.setVolume (details.trim ());

          details = fields.get (3).getText ();
          if (!details.trim ().isEmpty ())
          {
            int length = details.length ();
            if (length > 6)
              dataset.setTracks (details.substring (0, 6).trim ());
            if (length > 10)
              dataset.setPercentUsed (details.substring (7, 10).trim ());
            if (length > 14)
              dataset.setExtents (details.substring (11, 14).trim ());
            if (length > 15)
              dataset.setDevice (details.substring (15).trim ());
          }

          details = fields.get (4).getText ();
          if (!details.trim ().isEmpty ())
          {
            dataset.setDsorg (details.substring (0, 5).trim ());
            dataset.setRecfm (details.substring (5, 10).trim ());
            dataset.setLrecl (details.substring (10, 17).trim ());
            blkSize = details.substring (17).trim ();
            try
            {
              bls = Integer.parseInt (blkSize);
              dataset.setBlksize (String.format ("%,7d", bls));
            }
            catch (NumberFormatException e)
            {
              System.out.println ("bollocks");
              System.out.printf ("[%s]%n", blkSize);
            }
          }

          details = fields.get (5).getText ();
          if (!details.trim ().isEmpty ())
          {
            dataset.setCreated (details.substring (0, 10).trim ());
            dataset.setExpires (details.substring (11, 20).trim ());
            dataset.setReferred (details.substring (22).trim ());
          }

          details = fields.get (6).getText ();
          dataset.setCatalog (details.trim ());

          nextLine++;// skip the row of hyphens
          break;

        case 5:
          datasetName = fields.get (0).getText ().trim ();
          dataset = new Dataset (datasetName);
          datasets.add (dataset);

          details = fields.get (2).getText ();
          dataset.setVolume (details.trim ());

          if (fields.size () >= 6)
          {
            details = fields.get (3).getText ();
            dataset.setTracks (details.substring (0, 6).trim ());
            dataset.setPercentUsed (details.substring (7, 10).trim ());
            dataset.setExtents (details.substring (11, 14).trim ());
            dataset.setDevice (details.substring (15).trim ());

            details = fields.get (4).getText ();
            dataset.setDsorg (details.substring (0, 5).trim ());
            dataset.setRecfm (details.substring (5, 10).trim ());
            dataset.setLrecl (details.substring (10, 17).trim ());
            blkSize = details.substring (17).trim ();
            try
            {
              bls = Integer.parseInt (blkSize);
              dataset.setBlksize (String.format ("%,7d", bls));
            }
            catch (NumberFormatException e)
            {
              System.out.println ("bollocks");
              System.out.printf ("[%s]%n", blkSize);
            }

            details = fields.get (5).getText ();
            if (!details.trim ().isEmpty ())
            {
              dataset.setCreated (details.substring (0, 10).trim ());
              dataset.setExpires (details.substring (11, 20).trim ());
              dataset.setReferred (details.substring (22).trim ());
            }
          }

          nextLine++;// skip the row of hyphens
          break;
      }

      datasetsToProcess--;
      nextLine += linesPerDataset;
    }

    return true;
  }

  private boolean checkMemberList ()
  {
    if (fields.size () < 14)
      return false;

    List<String> menus = getMenus ();
    if (menus.size () != pdsMenus.length)
      return false;

    int i = 0;
    for (String menu : menus)
      if (!pdsMenus[i++].equals (menu))
        return false;

    Field field = fields.get (8);
    int location = field.getFirstLocation ();
    if (location != 161)
      return false;

    String mode = field.getText ().trim ();

    field = fields.get (9);
    if (field.getFirstLocation () != 179)
      return false;
    String datasetName = field.getText ().trim ();

    field = fields.get (10);
    if (field.getFirstLocation () != 221)
      return false;
    String rowText = field.getText ().trim ();
    if (!"Row".equals (rowText))
      return false;

    field = fields.get (12);
    if (field.getFirstLocation () != 231)
      return false;
    String ofText = field.getText ().trim ();
    if (!"of".equals (ofText))
      return false;

    int rowFrom = Integer.parseInt (fields.get (11).getText ().trim ());
    int rowTo = Integer.parseInt (fields.get (13).getText ().trim ());
    //    int totalMembers = rowTo - rowFrom + 1;

    System.out.print ("\nMember list of " + datasetName + " in " + mode + " mode");
    System.out.printf ("- row %d of %d%n", rowFrom, rowTo);

    List<Field> headings = getFieldsOnRow (4);
    int maxRows = Math.min (19, rowTo - rowFrom + 1) + 5;

    for (int row = 5; row < maxRows; row++)
    {
      List<Field> fields = getFieldsOnRow (row);

      String memberName = fields.get (1).getText ().trim ();
      Dataset member = new Dataset (datasetName + "(" + memberName + ")");
      members.add (member);

      String details = fields.get (3).getText ();

      if (headings.size () == 7)
      {
        String size = details.substring (3, 9);
        String created = details.substring (11, 21);
        String modified = details.substring (23, 33);
        String time = details.substring (34, 42);
        String id = details.substring (44);
        System.out.printf ("%3d [%-8s] [%s] [%s] [%s] [%s] [%s]%n", row - 5 + rowFrom,
                           memberName, size, created, modified, time, id);
      }
      else if (headings.size () == 13)
      {
        String size = details.substring (3, 9);
        String init = details.substring (11, 17);
        String mod = details.substring (19, 25);
        String vvmm = details.substring (31, 36);
        String id = details.substring (44);
        System.out.printf ("%3d [%-8s] [%s] [%s] [%s] [%s] [%s]%n", row - 5 + rowFrom,
                           memberName, size, init, mod, vvmm, id);
      }
      else
        System.out.println ("Unexpected headings size: " + headings.size ());
    }

    return true;
  }

  private void checkEditOrViewDataset ()
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

  private void checkBrowseDataset ()
  {
    if (fields.size () < 8)
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

  private List<String> getMenus ()
  {
    List<String> menus = new ArrayList<> ();

    for (Field field : fields)
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

  private List<Field> getFieldsOnRow (int requestedRow)
  {
    int firstLocation = requestedRow * screen.columns;
    int lastLocation = firstLocation + screen.columns - 1;
    return getFields (firstLocation, lastLocation);
  }

  private List<Field> getRowFields (int requestedRowFrom, int rows)
  {
    int firstLocation = requestedRowFrom * screen.columns;
    int lastLocation = (requestedRowFrom + rows) * screen.columns - 1;
    return getFields (firstLocation, lastLocation);
  }

  private List<Field> getFields (int firstLocation, int lastLocation)
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
    text.append (String.format ("TSO field ...... %s%n", tsoCommandField));
    text.append (String.format ("Dataset list ... %s%n", isDatasetList));
    text.append (String.format ("Userid/prefix .. %s / %s%n", userid, prefix));
    text.append (String.format ("Datasets for ... %s%n", datasetsMatching));
    text.append (String.format ("Volume ......... %s%n", datasetsOnVolume));
    text.append (String.format ("Datasets ....... %s%n",
                                datasets == null ? "" : datasets.size ()));

    return text.toString ();
  }
}