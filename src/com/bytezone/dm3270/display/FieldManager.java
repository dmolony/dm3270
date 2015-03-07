package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

public class FieldManager
{
  private final Screen screen;
  private int dataPositions;

  private final List<Field> fields = new ArrayList<> ();
  private final List<Field> unprotectedFields = new ArrayList<> ();
  private final List<Field> emptyFields = new ArrayList<> ();
  private final List<Field> hiddenFields = new ArrayList<> ();

  public FieldManager (Screen screen)
  {
    this.screen = screen;
  }

  public void buildFields ()
  {
    fields.clear ();
    unprotectedFields.clear ();
    emptyFields.clear ();
    hiddenFields.clear ();
    dataPositions = 0;

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
    if (field.isHidden ())
      hiddenFields.add (field);
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

  // ---------------------------------------------------------------------------------//
  // Debugging
  // ---------------------------------------------------------------------------------//

  public String getTotalsText ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Total screen fields : %d%n", fields.size ()));
    text.append (String.format ("Empty fields        : %d%n", emptyFields.size ()));
    text.append (String.format ("Hidden fields       : %d%n", hiddenFields.size ()));
    text.append (String.format ("Unprotected fields  : %d%n", unprotectedFields.size ()));
    text.append (String.format ("Data positions      : %d%n", dataPositions));
    text.append (String.format ("Screen positions    : %d",
                                dataPositions + fields.size ()));

    return text.toString ();
  }

  public String getFieldsText ()
  {
    StringBuilder text = new StringBuilder ();

    for (Field field : fields)
    {
      text.append ("\n\n");
      text.append (field.toStringWithLinks ());
    }
    return text.toString ();
  }
}