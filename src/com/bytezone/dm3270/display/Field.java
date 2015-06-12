package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.plugins.ScreenField;

public class Field implements Iterable<ScreenPosition>
{
  private final Screen screen;
  private final int startPosition;      // position of StartFieldAttribute
  private final int endPosition;        // last data position of this field
  private Field next, previous;         // unprotected fields

  private final StartFieldAttribute startFieldAttribute;
  private final List<ScreenPosition> screenPositions = new ArrayList<> ();

  private final boolean debug = false;

  public Field (Screen screen, int start, int end, List<ScreenPosition> positions)
  {
    assert positions.size () == (start > end ? screen.screenSize - start + end + 1 : end
        - start + 1);

    this.screen = screen;
    startPosition = start;
    endPosition = end;
    screenPositions.addAll (positions);
    startFieldAttribute = positions.get (0).getStartFieldAttribute ();

    if (startFieldAttribute.isHidden ())
      for (ScreenPosition screenPosition : positions)
        screenPosition.setVisible (false);
  }

  void setScreenContexts (ScreenContext base)
  {
    ScreenContext defaultContext = screenPositions.get (0).getScreenContext ();
    for (ScreenPosition sp : screenPositions)
      if (sp.getScreenContext () == base)
        sp.setScreenContext (defaultContext);
  }

  //  void setScreenContexts ()
  //  {
  //    ContextManager contextManager = screen.getContextManager ();
  //    ScreenPosition startFieldScreenPosition = screenPositions.get (0);
  //    StartFieldAttribute sfa = startFieldScreenPosition.getStartFieldAttribute ();
  //    ScreenContext screenContext = contextManager.getBase ();
  //
  //    screenContext = sfa.process (contextManager, screenContext);
  //    for (Attribute attribute : startFieldScreenPosition.getAttributes ())
  //      screenContext = attribute.process (contextManager, screenContext);
  //    ScreenContext baseContext = screenContext;
  //
  //    if (debug)
  //      System.out.printf ("%n%nNew field : %s", baseContext);
  //
  //    int position = 0;
  //    for (ScreenPosition screenPosition : screenPositions)
  //    {
  //      if (debug)
  //      {
  //        String spText = screenPosition.toString ();
  //        if (!spText.isEmpty ())
  //          System.out.printf ("%n  %4d %s : ", position, screenPosition);
  //      }
  //
  //      if (position++ > 0)
  //        for (Attribute attribute : screenPosition.getAttributes ())
  //          if (attribute.getAttributeType () == AttributeType.RESET)
  //            screenContext = baseContext;
  //          else
  //            screenContext = attribute.process (contextManager, screenContext);
  //
  //      if (debug)
  //        System.out.print (screenPosition.getChar ());
  //
  //      screenPosition.setScreenContext (screenContext);
  //    }
  //  }

  // link two unprotected fields to each other
  void linkToNext (Field nextField)
  {
    assert !isProtected ();
    this.next = nextField;
    nextField.previous = this;
  }

  void setPrevious (Field field)
  {
    this.previous = field;
  }

  void setNext (Field field)
  {
    this.next = field;
  }

  public Field getNextUnprotectedField ()
  {
    return next;
  }

  public Field getPreviousUnprotectedField ()
  {
    return previous;
  }

  public StartFieldAttribute getStartFieldAttribute ()
  {
    return startFieldAttribute;
  }

  public int getDisplayLength ()
  {
    return screenPositions.size () - 1;
  }

  public int getFirstLocation ()
  {
    return screen.validate (startPosition + 1);
  }

  public int getCursorOffset ()
  {
    Cursor cursor = screen.getScreenCursor ();
    int cursorLocation = cursor.getLocation ();
    if (cursorLocation >= startPosition)
      return cursorLocation - startPosition;
    return screen.screenSize - startPosition + cursorLocation;
  }

  public boolean isHidden ()
  {
    return startFieldAttribute.isHidden ();
  }

  public boolean isProtected ()
  {
    return startFieldAttribute.isProtected ();
  }

  public boolean isUnprotected ()
  {
    return !startFieldAttribute.isProtected ();
  }

  public boolean isAlphanumeric ()
  {
    return startFieldAttribute.isAlphanumeric ();
  }

  public boolean isModified ()
  {
    return startFieldAttribute.isModified ();
  }

  public boolean isVisible ()
  {
    return startFieldAttribute.isVisible ();
  }

  public void setModified (boolean modified)
  {
    startFieldAttribute.setModified (modified);
  }

  public boolean contains (int position)
  {
    if (startPosition <= endPosition)
      return position >= startPosition && position <= endPosition;
    return position >= startPosition || position <= endPosition;
  }

  public void draw ()
  {
    int position = startPosition;
    while (true)
    {
      screen.drawPosition (position, false);
      if (position == endPosition)
        break;
      position = screen.validate (position + 1);
    }
  }

  public void clear (boolean setModified)
  {
    if (setModified)                 // don't reset any already set flags
      setModified (true);

    for (int i = 1; i < screenPositions.size (); i++)
      screenPositions.get (i).reset ();
  }

  public void clear (int first, int last)
  {
    for (int i = first; i <= last; i++)
      screenPositions.get (i).setChar ((byte) 0);
  }

  // overwrites each position with the position to its right (delete)
  public void pull (int first, int last)
  {
    ScreenPosition spFirst = screenPositions.get (first);
    while (first < last)
    {
      ScreenPosition sp = screenPositions.get (++first);
      spFirst.setChar (sp.getByte ());
      spFirst.setScreenContext (sp.getScreenContext ());
      spFirst = sp;
    }

    screenPositions.get (last).setChar ((byte) 0);
    screenPositions.get (last).clearAttributes ();
  }

  //overwrites each position with the position to its left (insert)
  public void push (int first, int last)
  {
    ScreenPosition spLast = screenPositions.get (last);
    while (first < last)
    {
      ScreenPosition sp = screenPositions.get (--last);
      spLast.setChar (sp.getByte ());
      spLast.setScreenContext (sp.getScreenContext ());
      spLast = sp;
    }
  }

  public byte getByteAt (int position)
  {
    return screenPositions.get (position).getByte ();
  }

  public String getText ()
  {
    if (startPosition == endPosition)
      return "";

    char[] buffer = new char[getDisplayLength ()];
    int position = screen.validate (startPosition + 1);
    int ptr = 0;

    while (true)
    {
      buffer[ptr++] = screen.getScreenPosition (position).getChar ();
      if (position == endPosition)
        break;
      position = screen.validate (position + 1);
    }
    return new String (buffer);
  }

  public void setText (byte[] buffer)
  {
    assert startPosition != endPosition;
    int position = screen.validate (startPosition + 1);
    int ptr = 0;

    while (true)
    {
      screen.getScreenPosition (position).setChar (buffer[ptr++]);
      if (position == endPosition || ptr == buffer.length)
        break;
      position = screen.validate (position + 1);
    }
  }

  public ScreenField getScreenField (int screenSequence, int fieldSequence)
  {
    int firstLocation = getFirstLocation ();
    int row = firstLocation / screen.columns;
    int column = firstLocation % screen.columns;
    int length = getDisplayLength ();
    ScreenField screenField =
        new ScreenField (fieldSequence, firstLocation, row, column, length,
            isProtected (), isAlphanumeric (), isVisible (), isModified (), getText ());
    return screenField;
  }

  public String toStringWithLinks ()
  {
    StringBuilder text = new StringBuilder (toString ());
    if (previous != null)
    {
      text.append ("\n          <-- ");
      text.append (previous);
    }
    if (next != null)
    {
      text.append ("\n          --> ");
      text.append (next);
    }
    return text.toString ();
  }

  @Override
  public String toString ()
  {
    return String.format ("%04d-%04d %s [%s]", startPosition, endPosition,
                          startFieldAttribute.getAcronym (), getText ());
  }

  @Override
  public Iterator<ScreenPosition> iterator ()
  {
    return screenPositions.iterator ();
  }
}