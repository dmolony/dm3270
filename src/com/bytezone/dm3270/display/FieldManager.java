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
    List<ScreenPosition> positions = new ArrayList<ScreenPosition> ();

    int start = -1;
    int first = -1;

    int ptr = 0;
    while (ptr != first)
    {
      ScreenPosition screenPosition = screen.getScreenPosition (ptr);
      if (screenPosition.isStartField ())
      {
        if (start >= 0)                     // if there is a field to add
        {
          Field field = new Field (screen, start, ptr - 1, positions);
          fields.add (field);
          positions.clear ();
        }
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
  }

  public Field getField (int position)      // this needs to be improved
  {
    for (Field field : fields)
      if (field.contains (position))
        return field;
    return null;
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