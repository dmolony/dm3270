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
  private List<Dataset> datasets;
  private List<Dataset> members;

  private String datasetsMatching;
  private String datasetsOnVolume;

  private Field tsoCommandField;
  private boolean isTSOCommandScreen;
  private boolean isDatasetList;
  private boolean isMemberList;
  private String currentDataset;
  private String userid = "";
  private String prefix = "";

  public ScreenDetails (Screen screen)
  {
    this.screen = screen;
  }

  public void check (FieldManager fieldManager)
  {
    this.fieldManager = fieldManager;
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
    tsoCommandField = null;

    for (Field field : fieldManager.getFields ())
    {
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
    int totalMembers = rowTo - rowFrom + 1;

    System.out.print ("\nMember list of " + datasetName + " in " + mode + " mode");
    System.out.printf ("- row %d of %d%n", rowFrom, rowTo);

    List<Field> headings = getFieldsOnRow (4);
    members = new ArrayList<> ();

    for (int row = 5; row < totalMembers + 5; row++)
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
        System.out.printf ("%2d [%-8s] [%s] [%s] [%s] [%s] [%s]%n", row - 4, memberName,
                           size, created, modified, time, id);
      }
      else if (headings.size () == 13)
      {
        String size = details.substring (3, 9);
        String init = details.substring (11, 17);
        String mod = details.substring (19, 25);
        String vvmm = details.substring (31, 36);
        String id = details.substring (44);
        System.out.printf ("%2d [%-8s] [%s] [%s] [%s] [%s] [%s]%n", row - 4, memberName,
                           size, init, mod, vvmm, id);
      }
    }

    return true;
  }

  private boolean checkDatasetList ()
  {
    datasetsOnVolume = "";
    datasetsMatching = "";

    if (fields.size () < 21)
      return false;

    Field field = fields.get (9);
    int location = field.getFirstLocation ();
    if (location != 161)
      return false;

    String text = field.getText ();
    if (!text.startsWith ("DSLIST - Data Sets "))
      return false;

    for (int i = 11; i < 13; i++)
    {
      field = fields.get (i);
      location = field.getFirstLocation ();
      if (location == 241)
        break;
    }
    if (!field.getText ().equals ("Command ===>"))
      return false;

    int pos = text.indexOf ("Row ");
    String category =
        pos > 0 ? text.substring (19, pos).trim () : text.substring (19).trim ();

    if (category.startsWith ("Matching"))
      datasetsMatching = category.substring (9).trim ();
    else if (category.startsWith ("on volume "))
      datasetsOnVolume = category.substring (10).trim ();
    else
      System.out.println ("Unknown category: " + category);

    for (int i = 17; i < 21; i++)
    {
      field = fields.get (i);
      text = field.getText ().trim ();
      location = field.getFirstLocation ();
      if (text.startsWith ("Command - Enter"))
      {
        getDatasetList (i);
        break;
      }
    }

    return true;
  }

  private void getDatasetList (int startField)
  {
    if (fields.size () < startField + 3)
      return;

    int fieldNo = startField + 1;
    String text = fields.get (fieldNo).getText ().trim ();
    if ("Message".equals (text))
      ++fieldNo;

    String heading = fields.get (fieldNo).getText ();
    boolean isVolume = " Volume ".equals (heading);
    boolean isTracks = "     Tracks %Used XT  Device  ".equals (heading);
    boolean isDsorg = "    Dsorg  Recfm  Lrecl  Blksz".equals (heading);
    boolean isCombination = false;

    if (!isVolume && !isTracks && !isDsorg)
    {
      System.out.printf ("Unknown heading [%s]%n", heading);
      return;
    }

    if (isVolume && fields.size () > fieldNo + 4)
    {
      //      String heading2 = fields.get (fieldNo + 1).getText ();
      //      String heading3 = fields.get (fieldNo + 2).getText ();
      //      String heading4 = fields.get (fieldNo + 3).getText ();
      String heading5 = fields.get (fieldNo + 4).getText ();
      //      System.out.printf ("[%s]%n", heading2);
      //      System.out.printf ("[%s]%n", heading3);
      //      System.out.printf ("[%s]%n", heading4);
      //      System.out.printf ("[%s]%n", heading5);
      isCombination = "Catalog".equals (heading5.trim ());
      if (isCombination)
        fieldNo += 4;
    }

    fieldNo += 2;
    datasets = new ArrayList<> ();
    Dataset dataset = null;

    while (fieldNo < fields.size ())
    {
      Field field = fields.get (fieldNo);
      int column = field.getFirstLocation () % screen.columns;

      String name = field.getText ();

      if (column == 1 && field.getDisplayLength () == 53)
      {
        if (field.isProtected ())
          return;
        dataset = new Dataset (name.substring (9).trim ());
        datasets.add (dataset);
      }
      else if (column == 1 && field.getDisplayLength () == 79)
      {
        if (name.startsWith ("**"))
          break;
      }
      else if (dataset != null)
      {
        String details = field.getText ();

        if (!details.trim ().isEmpty ())
        {
          if (isCombination)
          {
            if (column == 1)
            {
              dataset.setTracks (details.substring (0, 6).trim ());
              dataset.setPercentUsed (details.substring (7, 10).trim ());
              dataset.setExtents (details.substring (11, 14).trim ());
              dataset.setDevice (details.substring (15).trim ());
            }
            else if (column == 25)
            {
              dataset.setDsorg (details.substring (0, 5).trim ());
              dataset.setRecfm (details.substring (5, 10).trim ());
              dataset.setLrecl (details.substring (10, 17).trim ());
              String blkSize = details.substring (17).trim ();
              int bls = Integer.parseInt (blkSize);
              dataset.setBlksize (String.format ("%,7d", bls));
            }
            else if (column == 48)
            {
              dataset.setCreated (details.substring (0, 10).trim ());
              dataset.setExpires (details.substring (11, 20).trim ());
              dataset.setReferred (details.substring (22).trim ());
            }
            else if (column == 72)
            {
              dataset.setVolume (details.trim ());
            }
            else if (column == 11)
              dataset.setCatalog (details.trim ());
          }
          else if (isVolume)
          {
            if (column == 72)
              dataset.setVolume (details.trim ());
          }
          else if (isTracks)
          {
            dataset.setTracks (details.substring (0, 6).trim ());
            dataset.setPercentUsed (details.substring (7, 11).trim ());
            dataset.setExtents (details.substring (12, 15).trim ());
            dataset.setDevice (details.substring (17).trim ());
          }
          else if (isDsorg)
          {
            dataset.setDsorg (details.substring (0, 5).trim ());
            dataset.setRecfm (details.substring (6, 11).trim ());
            dataset.setLrecl (details.substring (12, 19).trim ());
            String blkSize = details.substring (20).trim ();
            int bls = Integer.parseInt (blkSize);
            dataset.setBlksize (String.format ("%,7d", bls));
          }
        }
      }
      fieldNo++;
    }
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
    List<Field> rowFields = new ArrayList<> ();
    int firstLocation = requestedRow * screen.columns;
    int lastLocation = firstLocation + screen.columns - 1;

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