package com.bytezone.dm3270.display;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.plugins.PluginField;
import com.bytezone.dm3270.plugins.ScreenLocation;

public class Field implements Iterable<ScreenPosition>
{
  private final Screen screen;

  private final int startPosition;        // position of StartFieldAttribute
  private final int endPosition;          // last data position of this field
  private Field next, previous;           // unprotected fields

  private final StartFieldAttribute startFieldAttribute;
  private final List<ScreenPosition> screenPositions;
  private final ScreenDimensions screenDimensions;

  public Field (Screen screen, List<ScreenPosition> positions)
  {
    this.screen = screen;
    this.screenDimensions = screen.getScreenDimensions ();

    ScreenPosition firstScreenPosition = positions.get (0);
    ScreenPosition lastScreenPosition = positions.get (positions.size () - 1);

    screenPositions = new ArrayList<> (positions);
    startFieldAttribute = firstScreenPosition.getStartFieldAttribute ();

    startPosition = firstScreenPosition.getPosition ();
    endPosition = lastScreenPosition.getPosition ();
  }

  // link two unprotected fields to each other
  void linkToNext (Field nextField)
  {
    assert isUnprotected ();
    assert nextField.isUnprotected ();

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

  Field getNextUnprotectedField ()
  {
    return next;
  }

  Field getPreviousUnprotectedField ()
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

  // called from Cursor.tab()
  // called from Cursor.home()
  // called from Cursor.typeChar()
  // called from FieldManager.getFieldsInRange()
  // called from FieldManager.textMatches()
  // called from FieldManager.textMatchesTrim()
  // called from FieldManager.getMenus()
  // called from Screen.eraseAllUnprotected()
  // called from ScreenDetails.*()
  // called from ScreenPacker.packField()
  public int getFirstLocation ()
  {
    return screen.validate (startPosition + 1);
  }

  // called from Cursor.typeChar()
  // called from Cursor.backspace()
  // called from Cursor.delete()
  // called from Cursor.eraseEOL()
  // called from ConsolePane.fieldChanged()
  public int getCursorOffset ()
  {
    int cursorLocation = screen.getScreenCursor ().getLocation ();
    if (cursorLocation >= startPosition)
      return cursorLocation - startPosition;
    return screenDimensions.size - startPosition + cursorLocation;
  }

  public boolean isHidden ()
  {
    return startFieldAttribute.isHidden ();
  }

  // called from ScreenDetails.check()
  // called from FieldManager.buildFields()
  // called from FieldManager.getMenus()
  public boolean isProtected ()
  {
    return startFieldAttribute.isProtected ();
  }

  // called from Cursor.typeChar()
  // called from Cursor.backspace()
  // called from Cursor.delete()
  // called from Cursor.eraseEOL()
  // called from Cursor.tab()
  // called from FieldManager.buildFields()
  // called from ScreenDetails.check()
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

  public boolean isIntensified ()
  {
    return startFieldAttribute.isIntensified ();
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

  // called from Screen.setFieldText()
  // called from Cursor.typeChar()
  // called from Cursor.backspace()
  // called from Cursor.delete()
  // called from Cursor.eraseEOL()
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

  // called from AIDCommand.process() - ie replay
  // called from this.setText()
  public void erase ()
  {
    for (int i = 1; i < screenPositions.size (); i++)
      screenPositions.get (i).setChar ((byte) 0);
    setModified (true);
  }

  // called from FieldManager.eraseAllUnprotected()
  // called from AIDCommand.process()
  public void clearData (boolean alterModifiedFlag)
  {
    if (alterModifiedFlag)                  // don't reset any already set flags
      setModified (true);

    for (int i = 1; i < screenPositions.size (); i++)
      screenPositions.get (i).setChar ((byte) 0);         // leave screenContext
  }

  // called from AIDCommand.process()
  // called from Cursor.eraseEOL()
  public void clearData (int first, int last)
  {
    for (int i = first; i <= last; i++)
      screenPositions.get (i).setChar ((byte) 0);
  }

  // overwrites each position with the position to its right (delete)
  // called from Cursor.backspace()
  // called from Cursor.delete()
  void pull (int first, int last)
  {
    ScreenPosition spFirst = screenPositions.get (first);
    ScreenPosition spLast = screenPositions.get (last);

    while (first < last)
    {
      ScreenPosition sp = screenPositions.get (++first);
      spFirst.setChar (sp.getByte ());
      spFirst.setScreenContext (sp.getScreenContext ());
      spFirst = sp;
    }

    spLast.setChar ((byte) 0);
    spLast.clearAttributes ();
  }

  // overwrites each position with the position to its left (insert)
  // called from Cursor.typeChar()
  void push (int first, int last)
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

  // called from Cursor.typeChar()
  byte getByteAt (int position)
  {
    return screenPositions.get (position).getByte ();
  }

  // called from FieldManager.*()
  // called from Screen.*()
  // called from ScreenPacker.readModifiedFields()
  public String getText ()
  {
    if (startPosition == endPosition)
      return "";

    char[] buffer = new char[getDisplayLength ()];
    int ptr = 0;

    for (ScreenPosition screenPosition : screenPositions)
      if (!screenPosition.isStartField ())           // skip the start field attribute
        if (ptr < buffer.length)
          buffer[ptr++] = screenPosition.getChar ();
        else
          System.out.printf ("Too long: %d%n", ptr);

    return new String (buffer);
  }

  // called from TSOCommand.execute()
  public void setText (String text)
  {
    try
    {
      erase ();                                     // sets the field to modified
      setText (text.getBytes ("CP1047"));
      draw ();
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }
  }

  // called from Screen.setFieldText()
  // called from PluginsStage.processReply()
  // called from AIDCommand.process()
  public void setText (byte[] buffer)
  {
    int ptr = 1;
    for (byte b : buffer)
      if (ptr < screenPositions.size ())
        screenPositions.get (ptr++).setChar (b);
      else
      {
        System.out.println ("Buffer overrun");
        break;
      }
  }

  // called by FieldManager.getPluginScreen()
  PluginField getPluginField (int screenSequence, int fieldSequence)
  {
    int firstLocation = getFirstLocation ();
    int row = firstLocation / screenDimensions.columns;
    int column = firstLocation % screenDimensions.columns;
    int length = getDisplayLength ();

    ScreenLocation screenLocation = new ScreenLocation (firstLocation);

    return new PluginField (fieldSequence, screenLocation, length, isProtected (),
        isAlphanumeric (), isVisible (), isModified (), getText ());
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