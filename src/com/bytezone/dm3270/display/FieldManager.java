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
    Field previousUnprotectedField = null;    // used to link unprotected fields

    int start = -1;
    int first = -1;

    int ptr = 0;
    while (ptr != first)
    {
      ScreenPosition2 screenPosition = screen.getScreenPosition (ptr);
      if (screenPosition.isStartField ())
      {
        if (start >= 0)                     // if there is a field to add
          previousUnprotectedField =
              addField (start, ptr - 1, positions, previousUnprotectedField);
        else
          first = ptr;

        start = ptr;
      }

      if (start >= 0)
        positions.add (screenPosition);

      if (++ptr >= screen.screenSize)           // faster than validate()
      {
        ptr = 0;
        if (fields.size () == 0)                // wrapped around and still no fields
          break;
      }
    }

    if (start >= 0 && positions.size () > 0)
      addField (start, screen.validate (ptr - 1), positions, previousUnprotectedField);

    // build screen contexts for every position
    for (Field field : fields)
    {
      System.out.println (field);
    }
  }

  private Field addField (int start, int end, List<ScreenPosition2> positions,
      Field previousUnprotectedField)
  {
    Field field = new Field (screen, start, end, positions);
    fields.add (field);
    if (field.isUnprotected ())
    {
      unprotectedFields.add (field);
      if (previousUnprotectedField != null)
        previousUnprotectedField.linkToNext (field);
      previousUnprotectedField = field;
    }
    positions.clear ();
    return previousUnprotectedField;
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
}