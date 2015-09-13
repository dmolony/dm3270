package com.bytezone.dm3270.display;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.plugins.PluginField;

public class Field implements Iterable<ScreenPosition>
{
  private final Screen screen;
  private final int startPosition;// position of StartFieldAttribute
  private final int endPosition;// last data position of this field
  private Field next, previous;// unprotected fields

  private final StartFieldAttribute startFieldAttribute;
  private final List<ScreenPosition> screenPositions;

  private final boolean debug = false;

  public Field (Screen screen, List<ScreenPosition> positions)
  {
    this.screen = screen;
    ScreenPosition firstScreenPosition = positions.get (0);
    ScreenPosition lastScreenPosition = positions.get (positions.size () - 1);

    screenPositions = new ArrayList<> (positions);
    startFieldAttribute = firstScreenPosition.getStartFieldAttribute ();

    startPosition = firstScreenPosition.position;
    endPosition = lastScreenPosition.position;

    if (startFieldAttribute.isHidden ())
      for (ScreenPosition screenPosition : positions)
        screenPosition.setVisible (false);
    else if (!startFieldAttribute.isExtended ()) // remove any extended attributes
    {
      ScreenContext defaultContext = firstScreenPosition.getScreenContext ();
      for (ScreenPosition screenPosition : positions)
      {
        screenPosition.clearAttributes ();
        screenPosition.setScreenContext (defaultContext);
      }
    }
  }

  // called from FieldManager.buildFields()
      void setScreenContexts (ScreenContext base)
  {
    ScreenContext defaultContext = screenPositions.get (0).getScreenContext ();
    //    if (startPosition == 66)
    //    {
    //      System.out.println (this);
    //      System.out.println (defaultContext);
    //      System.out.println (base);
    //      System.out.println (startFieldAttribute);
    //    }
    for (ScreenPosition sp : screenPositions)
    {
      if (sp.getScreenContext () == base)
        sp.setScreenContext (defaultContext);
    }
  }

  // link two unprotected fields to each other
      void linkToNext (Field nextField)
  {
    assert!isProtected ();
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
    if (setModified) // don't reset any already set flags
      setModified (true);

    for (int i = 1; i < screenPositions.size (); i++)
      screenPositions.get (i).reset ();
  }

  public void erase ()
  {
    for (int i = 1; i < screenPositions.size (); i++)
      screenPositions.get (i).setChar ((byte) 0);
    setModified (true);
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

  // overwrites each position with the position to its left (insert)
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

  public void setText (String text)
  {
    try
    {
      erase ();// sets the field to modified
      setText (text.getBytes ("CP1047"));
      draw ();
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }
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

  public PluginField getScreenField (int screenSequence, int fieldSequence)
  {
    int firstLocation = getFirstLocation ();
    int row = firstLocation / screen.columns;
    int column = firstLocation % screen.columns;
    int length = getDisplayLength ();
    PluginField screenField =
        new PluginField (fieldSequence, firstLocation, row, column, length,
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