package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.application.ContextHandler;

public class FieldManager
{
  private final Screen screen;
  private final ContextHandler contextHandler = new ContextHandler ();

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
    while (ptr != first)
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
        positions.add (screenPosition);

      if (++ptr >= screen.screenSize)           // faster than validate()
      {
        ptr = 0;
        if (fields.size () == 0)                // wrapped around and still no fields
          break;
      }
    }

    if (start >= 0 && positions.size () > 0)
      addField (start, screen.validate (ptr - 1), positions);

    // build screen contexts for every position and link uprotected fields
    Field previousUnprotectedField = null;
    for (Field field : fields)
    {
      field.setScreenContexts (contextHandler);
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

    dumpFields ();
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

  public void dumpFields ()
  {
    int fieldPositions = 0;
    int emptyFields = 0;
    System.out.println ();
    for (Field field : fields)
    {
      System.out.println (field.toStringWithLinks ());
      fieldPositions += field.getDisplayLength ();
      if (field.getDisplayLength () == 0)
        ++emptyFields;
    }

    System.out.println ();
    System.out.printf ("Total screen fields: %d%n", fields.size ());
    System.out.printf ("Empty fields       : %d%n", emptyFields);
    System.out.printf ("Unprotected fields : %d%n", unprotectedFields.size ());
    System.out.printf ("Field positions    : %d%n", fieldPositions);
    System.out.printf ("Data positions     : %d%n", fieldPositions + fields.size ());
  }
}