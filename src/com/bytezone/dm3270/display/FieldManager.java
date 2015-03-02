package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

public class FieldManager
{
  private final Screen screen;

  private final List<Field> fields = new ArrayList<> ();
  private final List<Field> unprotectedFields = new ArrayList<> ();

  public FieldManager (Screen screen)
  {
    this.screen = screen;
  }

  public void buildFields ()
  {
    fields.clear ();
    unprotectedFields.clear ();
    List<ScreenPosition2> positions = new ArrayList<ScreenPosition2> ();

    int start = -1;
    int first = -1;

    int ptr = 0;
    while (ptr != first)                    // wrapped around to the first field
    {
      ScreenPosition2 screenPosition = screen.getScreenPosition (ptr);
      if (screenPosition.isStartField ())
      {
        if (start >= 0)                     // if there is a field to add
          addField (start, ptr - 1, positions);
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

    // link first unprotected field to the last one
    if (unprotectedFields.size () > 0)
    {
      Field firstField = unprotectedFields.get (0);
      Field lastField = unprotectedFields.get (unprotectedFields.size () - 1);
      lastField.linkToNext (firstField);
    }
  }

  private void addField (int start, int end, List<ScreenPosition2> positions)
  {
    Field field = new Field (screen, start, end, positions);
    fields.add (field);
    positions.clear ();
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

  // ---------------------------------------------------------------------------------//
  // Debugging
  // ---------------------------------------------------------------------------------//

  public String dumpFields ()
  {
    int fieldPositions = 0;
    int emptyFields = 0;
    int hiddenFields = 0;
    StringBuilder text = new StringBuilder ();

    for (Field field : fields)
    {
      text.append (field.toStringWithLinks ());
      text.append ("\n");
      fieldPositions += field.getDisplayLength ();
      if (field.getDisplayLength () == 0)
        ++emptyFields;
      if (field.isHidden ())
        ++hiddenFields;
    }

    text.append ("\n");
    text.append (String.format ("Total screen fields: %d%n", fields.size ()));
    text.append (String.format ("Empty fields       : %d%n", emptyFields));
    text.append (String.format ("Hidden fields      : %d%n", hiddenFields));
    text.append (String.format ("Unprotected fields : %d%n", unprotectedFields.size ()));
    text.append (String.format ("Data positions     : %d%n", fieldPositions));
    text.append (String.format ("Screen positions   : %d",
                                fieldPositions + fields.size ()));
    return text.toString ();
  }
}