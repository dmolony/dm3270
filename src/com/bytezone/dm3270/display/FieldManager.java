package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.plugins.PluginScreen;

public class FieldManager
{
  private final Screen screen;
  private int dataPositions;
  private int inputPositions;
  private int hiddenProtectedFields;
  private int hiddenUnprotectedFields;

  private final List<Field> fields = new ArrayList<> ();
  private final List<Field> unprotectedFields = new ArrayList<> ();
  private final List<Field> emptyFields = new ArrayList<> ();

  public FieldManager (Screen screen)
  {
    this.screen = screen;
  }

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
    while (ptr != first)                    // wrapped around to the first field
    {
      ScreenPosition screenPosition = screen.getScreenPosition (ptr);
      if (screenPosition.isStartField ())
      {
        if (start >= 0)                     // if there is a field to add
          addField (start, screen.validate (ptr - 1), positions);
        else
          first = ptr;

        start = ptr;
      }

      if (start >= 0)
        positions.add (screenPosition);    // collect next field's positions

      if (++ptr >= screen.screenSize)      // faster than validate()
      {
        ptr = 0;
        if (first == -1)                   // wrapped around and still no fields
          break;
      }
    }

    if (start >= 0 && positions.size () > 0)
      addField (start, screen.validate (ptr - 1), positions);

    assert (dataPositions + fields.size () == 1920) || fields.size () == 0;

    // build screen contexts for every position and link uprotected fields
    Field previousUnprotectedField = null;
    for (Field field : fields)
    {
      field.setScreenContexts ();
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
  }

  private void addField (int start, int end, List<ScreenPosition> positions)
  {
    Field field = new Field (screen, start, end, positions);

    fields.add (field);
    positions.clear ();

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

  public void drawFields ()
  {
    for (Field field : fields)
      field.draw ();
  }

  public void drawUnprotectedFields ()
  {
    for (Field field : unprotectedFields)
      field.draw ();
  }

  Field eraseAllUnprotected ()
  {
    if (unprotectedFields.size () == 0)
      return null;

    for (Field field : unprotectedFields)
      field.clear (true);

    return unprotectedFields.get (0);
  }

  public PluginScreen getPluginScreen ()
  {
    PluginScreen pluginScreen = new PluginScreen ();

    for (Field field : fields)
      pluginScreen.add (field.getScreenField ());

    return pluginScreen;
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

    for (Field field : fields)
    {
      text.append (field.toStringWithLinks ());
      text.append ("\n\n");
    }
    if (text.length () > 0)
    {
      text.deleteCharAt (text.length () - 1);
      text.deleteCharAt (text.length () - 1);
    }
    return text.toString ();
  }
}