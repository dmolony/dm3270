package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.plugins.PluginData;
import com.bytezone.dm3270.plugins.ScreenField;

public class FieldManager
{
  private static final String[] tsoMenus = { "Menu", "List", "Mode", "Functions",
                                            "Utilities", "Help" };
  private final Screen screen;
  //  private final Pen pen;
  private final ScreenContext baseContext;

  private final List<Field> fields = new ArrayList<> ();
  private final List<Field> unprotectedFields = new ArrayList<> ();
  private final List<Field> emptyFields = new ArrayList<> ();

  private int dataPositions;
  private int inputPositions;
  private int hiddenProtectedFields;
  private int hiddenUnprotectedFields;

  public FieldManager (Screen screen, ScreenContext baseContext)
  {
    this.screen = screen;
    //    pen = new Pen (screen);
    //    pen = screen.getPen ();
    this.baseContext = baseContext;
  }

  //  public Pen getPen ()
  //  {
  //    return pen;
  //  }

  //  public void reset ()
  //  {
  //    pen.reset ();
  //  }

  // this is called after the pen and screen positions have been modified
  public void buildFields ()
  {
    fields.clear ();
    unprotectedFields.clear ();
    emptyFields.clear ();

    dataPositions = 0;
    inputPositions = 0;
    hiddenProtectedFields = 0;
    hiddenUnprotectedFields = 0;

    List<ScreenPosition> positions = new ArrayList<ScreenPosition> ();

    int start = -1;
    int first = -1;
    int ptr = 0;

    while (ptr != first)                   // not wrapped around to the first field yet
    {
      ScreenPosition screenPosition = screen.getScreenPosition (ptr);

      // check for the start of a new field
      if (screenPosition.isStartField ())
      {
        if (start >= 0)                    // if there is a field to add
        {
          addField (new Field (screen, positions));
          positions.clear ();
        }
        else
          first = ptr;                     // this is the first field on the screen

        start = ptr;                       // beginning of the current field
      }

      // add ScreenPosition to the current field
      if (start >= 0)                      // if we are in a field...
        positions.add (screenPosition);    // collect next field's positions

      // increment ptr and wrap around
      if (++ptr == screen.screenSize)      // faster than validate()
      {
        ptr = 0;
        if (first == -1)                   // wrapped around and still no fields
          break;
      }
    }

    if (start >= 0 && positions.size () > 0)
      addField (new Field (screen, positions));

    assert (dataPositions + fields.size () == 1920) || fields.size () == 0;

    // build screen contexts for every position and link uprotected fields
    Field previousUnprotectedField = null;
    for (Field field : fields)
    {
      //      field.setScreenContexts (pen.getBase ());
      field.setScreenContexts (baseContext);
      if (field.isUnprotected ())
      {
        unprotectedFields.add (field);
        if (previousUnprotectedField != null)
          previousUnprotectedField.linkToNext (field);
        previousUnprotectedField = field;
      }
    }

    if (unprotectedFields.size () > 0)
    {
      // link first unprotected field to the last one
      Field firstField = unprotectedFields.get (0);
      Field lastField = unprotectedFields.get (unprotectedFields.size () - 1);
      lastField.linkToNext (firstField);

      // link protected fields to unprotected fields
      Field prev = lastField;
      Field next = firstField;

      for (Field field : fields)
        if (field.isProtected ())
        {
          field.setNext (next);
          field.setPrevious (prev);
        }
        else
        {
          next = field.getNextUnprotectedField ();
          prev = field;
        }
    }

    //    getMenus ();
    getTSOCommandField ();
  }

  private void addField (Field field)
  {
    fields.add (field);

    dataPositions += field.getDisplayLength ();

    if (field.getDisplayLength () == 0)
      emptyFields.add (field);

    if (field.isUnprotected ())
      inputPositions += field.getDisplayLength ();

    if (field.isHidden ())
      if (field.isProtected ())
        ++hiddenProtectedFields;
      else
        ++hiddenUnprotectedFields;
  }

  public Field getField (int position)      // this needs to be improved
  {
    for (Field field : fields)
      if (field.contains (position))
        return field;
    return null;
  }

  public List<Field> getUnprotectedFields ()
  {
    return unprotectedFields;
  }

  public List<Field> getFields ()
  {
    return fields;
  }

  public int size ()
  {
    return fields.size ();
  }

  //  public void drawFields ()
  //  {
  //    for (Field field : fields)
  //      field.draw ();
  //  }

  //  public void drawUnprotectedFields ()
  //  {
  //    for (Field field : unprotectedFields)
  //      field.draw ();
  //  }

  Field eraseAllUnprotected ()
  {
    if (unprotectedFields.size () == 0)
      return null;

    for (Field field : unprotectedFields)
      field.clear (true);

    return unprotectedFields.get (0);
  }

  // ---------------------------------------------------------------------------------//
  // Convert internal Fields to ScreenFields for use by plugins
  // ---------------------------------------------------------------------------------//

  public PluginData getPluginScreen (int sequence, int row, int column)
  {
    List<ScreenField> screenFields = new ArrayList<> ();
    int count = 0;

    for (Field field : fields)
      screenFields.add (field.getScreenField (sequence, count++));

    return new PluginData (sequence, row, column, screenFields);
  }

  // ---------------------------------------------------------------------------------//
  // Interpret screen
  // ---------------------------------------------------------------------------------//

  public List<String> getMenus ()
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

  public Field getTSOCommandField ()
  {
    int maxLocation = screen.columns * 4 + 20;
    int minLocation = screen.columns;
    boolean promptFound = false;
    Field commandField = null;

    for (Field field : fields)
    {
      if (field.getFirstLocation () > maxLocation)
        break;

      if (field.getFirstLocation () < minLocation)
        continue;

      int length = field.getDisplayLength ();

      if (promptFound)
      {
        if (field.isProtected () || field.isHidden () || length < 48 || length > 70)
          break;

        commandField = field;
        break;
      }

      int column = field.getFirstLocation () % screen.columns;
      if (column > 2)
        continue;

      if (field.isUnprotected () || field.isHidden () || length < 8 || length > 15)
        continue;

      String text = field.getText ();

      if (text.endsWith ("===>"))
        promptFound = true;             // next loop iteration will return the field
    }

    return commandField;
  }

  public boolean isTSOCommandScreen ()
  {
    if (fields.size () < 14)
      return false;

    Field field = fields.get (10);
    if (!"ISPF Command Shell".equals (field.getText ()))
      return false;

    field = fields.get (13);
    if (!"Enter TSO or Workstation commands below:".equals (field.getText ()))
      return false;

    List<String> menus = getMenus ();
    if (menus.size () != tsoMenus.length)
      return false;

    int i = 0;
    for (String menu : menus)
      if (!tsoMenus[i++].equals (menu))
        return false;

    return true;
  }

  // ---------------------------------------------------------------------------------//
  // Debugging
  // ---------------------------------------------------------------------------------//

  public String getTotalsText ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Start fields     : %4d%n", fields.size ()));
    text.append (String.format ("  Zero length    : %4d%n", emptyFields.size ()));
    text.append (String.format ("  Unprotected    : %4d   (%d hidden)%n",
                                unprotectedFields.size (), hiddenUnprotectedFields));
    text.append (String.format ("  Protected      : %4d   (%d hidden)%n%n",
                                fields.size () - unprotectedFields.size (),
                                hiddenProtectedFields));

    text.append (String.format ("Screen positions : %4d%n",
                                dataPositions + fields.size ()));
    text.append (String.format ("  Attributes     : %4d%n", fields.size ()));
    text.append (String.format ("  Output         : %4d%n", dataPositions
        - inputPositions));
    text.append (String.format ("  Input          : %4d", inputPositions));

    return text.toString ();
  }

  public String getFieldsText ()
  {
    StringBuilder text = new StringBuilder ();

    int count = 0;
    for (Field field : fields)
      text.append (String.format ("%4d %s%n", count++, field));

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}