package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.plugins.PluginData;
import com.bytezone.dm3270.plugins.PluginField;

public class FieldManager
{
  private final Screen screen;
  private final ScreenContext baseContext;
  private final ScreenDetails screenDetails;

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
    this.baseContext = baseContext;
    this.screenDetails = screen.getScreenDetails ();
  }

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

    while (ptr != first)// not wrapped around to the first field yet
    {
      ScreenPosition screenPosition = screen.getScreenPosition (ptr);

      // check for the start of a new field
      if (screenPosition.isStartField ())
      {
        if (start >= 0)
        // if there is a field to add
        {
          addField (new Field (screen, positions));
          positions.clear ();
        }
        else
          first = ptr;// this is the first field on the screen

        start = ptr;// beginning of the current field
      }

      // add ScreenPosition to the current field
      if (start >= 0)
        // if we are in a field...
        positions.add (screenPosition);// collect next field's positions

      // increment ptr and wrap around
      if (++ptr == screen.screenSize)
      {// faster than validate()
        ptr = 0;
        if (first == -1)
          break;// wrapped around and still no fields
      }
    }

    if (start >= 0 && positions.size () > 0)
      addField (new Field (screen, positions));

    assert(dataPositions + fields.size () == 1920) || fields.size () == 0;

    // build screen contexts for every position and link uprotected fields
    Field previousUnprotectedField = null;
    for (Field field : fields)
    {
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

    screenDetails.check (this);
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

  public Field getField (int position)// this needs to be improved
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

  Field eraseAllUnprotected ()
  {
    if (unprotectedFields.size () == 0)
      return null;

    for (Field field : unprotectedFields)
      field.clear (true);

    return unprotectedFields.get (0);
  }

  public int getScreenRows ()
  {
    return screen.rows;
  }

  public int getScreenColumns ()
  {
    return screen.columns;
  }

  List<Field> getRowFields (int requestedRow)
  {
    int firstLocation = requestedRow * screen.columns;
    int lastLocation = firstLocation + screen.columns - 1;
    return getFieldsInRange (firstLocation, lastLocation);
  }

  List<Field> getRowFields (int requestedRowFrom, int rows)
  {
    int firstLocation = requestedRowFrom * screen.columns;
    int lastLocation = (requestedRowFrom + rows) * screen.columns - 1;
    return getFieldsInRange (firstLocation, lastLocation);
  }

  private List<Field> getFieldsInRange (int firstLocation, int lastLocation)
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

  // ---------------------------------------------------------------------------------//
  // Convert internal Fields to ScreenFields for use by plugins
  // ---------------------------------------------------------------------------------//

  public PluginData getPluginScreen (int sequence, int row, int column)
  {
    List<PluginField> screenFields = new ArrayList<> ();
    int count = 0;

    for (Field field : fields)
      screenFields.add (field.getScreenField (sequence, count++));

    return new PluginData (sequence, row, column, screenFields);
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
    text.append (String.format ("  Output         : %4d%n",
                                dataPositions - inputPositions));
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