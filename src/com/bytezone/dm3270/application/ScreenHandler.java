package com.bytezone.dm3270.application;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;

import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.ContextManager;
import com.bytezone.dm3270.display.ScreenContext;
import com.bytezone.dm3270.orders.BufferAddress;
import com.bytezone.dm3270.orders.BufferAddressSource;

public class ScreenHandler
{
  private final int rows;
  private final int columns;
  private final int screenSize;

  private final ScreenPosition[] screenPositions;
  private final ScreenCanvas screenCanvas;
  private final ContextManager contextHandler = new ContextManager ();

  private final List<ScreenField> screenFields = new ArrayList<ScreenField> ();
  private final List<ScreenField> unprotectedFields = new ArrayList<ScreenField> ();

  private ScreenField currentField;
  private final Cursor cursor;

  private boolean keyboardLocked;
  private boolean resetModified;
  private boolean erased;

  private BufferAddress insertedCursorAddress;
  private byte aid;

  private byte replyMode;
  private byte[] replyTypes = new byte[0];

  private ConsoleStage consoleStage;

  public enum FieldProtectionType
  {
    PROTECTED, MODIFIABLE
  }

  public ScreenHandler (int rows, int columns, Font font)
  {
    this.rows = rows;
    this.columns = columns;
    screenSize = rows * columns;

    screenCanvas = new ScreenCanvas (rows, columns, font);
    screenPositions = new ScreenPosition[screenSize];

    for (int i = 0; i < screenPositions.length; i++)
      screenPositions[i] = new ScreenPosition (this, i);

    cursor = new Cursor (this, screenPositions, screenCanvas);
  }

  public void setConsoleStage (ConsoleStage consoleStage)
  {
    this.consoleStage = consoleStage;
    cursor.setConsoleStage (consoleStage);
  }

  public int getRows ()
  {
    return rows;
  }

  public int getColumns ()
  {
    return columns;
  }

  public int getScreenSize ()
  {
    return screenSize;
  }

  public ScreenCanvas getScreenCanvas ()
  {
    return screenCanvas;
  }

  public Cursor getCursor ()
  {
    return cursor;
  }

  ScreenField getCurrentField ()
  {
    return currentField;
  }

  public ContextManager getContextHandler ()
  {
    return contextHandler;
  }

  public void draw (boolean rebuild)
  {
    if (rebuild)
    {
      buildFields ();
      setCurrentField ();
    }

    for (ScreenField screenField : screenFields)
      if (screenField.isHidden ())
        for (ScreenPosition screenPosition : screenField.getScreenPositions ())
          screenCanvas.erase (screenPosition);
      else
        for (ScreenPosition screenPosition : screenField.getScreenPositions ())
          screenCanvas.draw (screenPosition);

    screenCanvas.draw (cursor.getScreenPosition ());    // protected fields are erased
  }

  private void setCurrentField ()
  {
    setCurrentField (null);
    int cursorLocation = cursor.getLocation ();
    for (ScreenField sf : screenFields)
      if (sf.contains (cursorLocation))
      {
        setCurrentField (sf);
        break;
      }
  }

  private void setCurrentField (ScreenField screenField)
  {
    currentField = screenField;
    consoleStage.setCurrentField (screenField);
  }

  public void fieldModified (ScreenField screenField)
  {
    consoleStage.setFieldType (screenField);
  }

  public void eraseScreen (boolean erase)
  {
    if (erase)
    {
      screenCanvas.clearScreen ();
      for (ScreenPosition screenPosition : screenPositions)
      {
        screenPosition.reset ();
        screenPosition.clearAttributes ();
      }
    }
    erased = erase;
  }

  public void insertCursor ()
  {
    insertedCursorAddress = cursor.getAddress ();    // make it visible later
  }

  public int getCursorPosition ()
  {
    return cursor.getLocation ();
  }

  public byte getAID ()
  {
    return aid;
  }

  public void setAID (byte aid)
  {
    this.aid = aid;
  }

  public void setAID (String keyName)
  {
    this.aid = AIDCommand.getKey (keyName);
  }

  public ScreenField getField (BufferAddressSource source)
  {
    int location = source.getBufferAddress ().getLocation ();
    for (ScreenField sf : screenFields)
      if (sf.contains (location))
        return sf;
    return null;
  }

  public ScreenPosition getScreenPosition (int location)
  {
    return screenPositions[location];
  }

  // ---------------------------------------------------------------------------------//
  // build a list of all fields on the screen
  // redo all of the screen contexts
  // ---------------------------------------------------------------------------------//
  public void buildFields ()
  {
    screenFields.clear ();
    unprotectedFields.clear ();
    ScreenField previousUnprotectedField = null;    // used to link unprotected fields

    int ptr = 0;
    while (ptr < screenPositions.length)
    {
      ScreenPosition screenPosition = screenPositions[ptr];
      if (screenPosition.isStartField ())
      {
        ScreenField screenField = new ScreenField (this, ptr);
        screenFields.add (screenField);
        finishField (screenField);

        // link to previous unprotected field
        if (screenField.isModifiable ())
        {
          unprotectedFields.add (screenField);
          if (previousUnprotectedField != null)
            previousUnprotectedField.linkToNext (screenField);
          previousUnprotectedField = screenField;
        }

        ptr += screenField.getLength ();      // doesn't include the SFA
      }
      ptr++;
    }

    // link first unprotected field to the last one
    if (unprotectedFields.size () > 0)
    {
      ScreenField first = unprotectedFields.get (0);
      ScreenField last = unprotectedFields.get (unprotectedFields.size () - 1);
      last.linkToNext (first);
    }

    if (resetModified)
    {
      for (ScreenField field : unprotectedFields)
        field.setModified (false);
      resetModified = false;
    }

    if (insertedCursorAddress != null)
    {
      cursor.setAddress (insertedCursorAddress);
      insertedCursorAddress = null;
    }
    else
      cursor.setLocation (0);

    cursor.setVisible (true);
  }

  private void finishField (ScreenField screenField)
  {
    int ptr = screenField.getStartPosition ();        // start at the attribute position
    ScreenContext currentContext = contextHandler.getBase ();

    while (true)
    {
      ScreenPosition screenPosition = screenPositions[ptr];
      screenPosition.setScreenContext (currentContext);
      screenPosition.applyAttributes ();

      screenField.add (screenPosition);                     // may change the intensity
      currentContext = screenPosition.getScreenContext ();  // updated with attributes

      int nextPos = ptr + 1;
      if (nextPos >= screenSize)
        nextPos = 0;

      // start of the next field means the end of this one
      if (screenPositions[nextPos].isStartField ())
      {
        screenField.setEndPosition (ptr);
        break;
      }

      ptr = nextPos;
    }
  }

  // ---------------------------------------------------------------------------------//
  // Events to be processed
  // ---------------------------------------------------------------------------------//

  public void resetPartition ()
  {
    //    cursor.setLocation (0);
  }

  public void startPrinter ()
  {
  }

  public void soundAlarm ()
  {
    System.out.println ("Sound alarm");
    //    Toolkit.getDefaultToolkit ().beep ();
  }

  public void restoreKeyboard ()
  {
    keyboardLocked = false;
    if (consoleStage != null)
      consoleStage.setStatus ("");
  }

  public void lockKeyboard ()
  {
    keyboardLocked = true;
    if (consoleStage != null)
      consoleStage.setStatus ("Inhibit");
  }

  public void resetModified ()
  {
    resetModified = true;     // will happen after the screen is rebuilt
  }

  public void setReplyMode (byte replyMode, byte[] replyTypes)
  {
    this.replyMode = replyMode;
    this.replyTypes = replyTypes;
  }

  public byte getReplyMode ()
  {
    return replyMode;
  }

  public byte[] getReplyTypes ()
  {
    return replyTypes;
  }

  // ---------------------------------------------------------------------------------//
  // Keyboard events
  // ---------------------------------------------------------------------------------//

  public boolean isKeyboardLocked ()
  {
    return keyboardLocked;
  }

  public void enter ()
  {
    cursor.setVisible (false);
  }

  public void tab (boolean shiftIsDown)
  {
    cursor.setVisible (false);

    if (currentField != null && currentField.isModifiable ())
    {
      // use the linked list contained in unprotected fields
      ScreenPosition sp = cursor.getScreenPosition ();
      ScreenPosition firstPosition = currentField.getScreenPositions ().get (1);
      // what if a field has no screenPositions?
      setCurrentField (shiftIsDown ? (sp == firstPosition ? currentField.getPrevious ()
          : currentField) : currentField.getNext ());
      cursor.setLocation (currentField.getStartPosition () + 1);
    }
    else
    {
      // search the list of unprotected fields
      // what about screens with no fields??
      setCurrentField (shiftIsDown ? previousUnprotectedField ()
          : nextUnprotectedField ());
      cursor.setLocation (currentField.getStartPosition () + 1);
    }

    cursor.setVisible (true);
  }

  public void backspace ()
  {
    cursor.setVisible (false);

    cursor.moveLeft ();
    ScreenPosition sp = cursor.getScreenPosition ();
    if (sp.isStartField ())
    {
      setCurrentField (currentField.getPrevious ());
      cursor.setLocation (currentField.getStartPosition () + 1);
      sp = cursor.getScreenPosition ();
    }

    sp.setCharacter ((byte) 0);
    cursor.setVisible (true);
  }

  public void moveCursor (KeyCode keyCode)
  {
    cursor.setVisible (false);

    if (keyCode == KeyCode.LEFT)
      cursor.moveLeft ();
    else if (keyCode == KeyCode.RIGHT)
      cursor.moveRight ();
    else if (keyCode == KeyCode.UP)
      cursor.moveUp ();
    else if (keyCode == KeyCode.DOWN)
      cursor.moveDown ();

    setCurrentField ();
    cursor.setVisible (true);
  }

  // ---------------------------------------------------------------------------------//
  // Field traversal
  // ---------------------------------------------------------------------------------//

  public ScreenField nextUnprotectedField ()
  {
    for (ScreenField sf : unprotectedFields)
      if (sf.getStartPosition () >= cursor.getLocation ())
        return sf;
    return unprotectedFields.get (0);
  }

  public ScreenField previousUnprotectedField ()
  {
    for (int i = unprotectedFields.size () - 1; i >= 0; i--)
    {
      ScreenField sf = unprotectedFields.get (i);
      if (sf.getStartPosition () < cursor.getLocation ())
        return sf;
    }
    return unprotectedFields.get (unprotectedFields.size () - 1);
  }

  // ---------------------------------------------------------------------------------//
  // Field list
  // ---------------------------------------------------------------------------------//

  // Used by ReadModifiedCommand.process() and EraseAllUnprotected.process()
  public List<ScreenField> getScreenFields (FieldProtectionType fpt)
  {
    List<ScreenField> list = new ArrayList<ScreenField> ();
    for (ScreenField sf : screenFields)
    {
      ScreenPosition sp = screenPositions[sf.getStartPosition ()];
      StartFieldAttribute sfa = sp.getStartFieldAttribute ();
      boolean isProtected = sfa.isProtected ();
      boolean isProtectedRequired = fpt == FieldProtectionType.PROTECTED;
      if (isProtected == isProtectedRequired)
        list.add (sf);
    }

    return list;
  }

  public List<ScreenField> getScreenFields ()
  {
    return screenFields;
  }

  // ---------------------------------------------------------------------------------//
  // Debugging
  // ---------------------------------------------------------------------------------//

  public void dumpFields ()
  {
    System.out.println ();
    for (ScreenField sf : screenFields)
      System.out.println (sf);
  }

  public void dumpScreen ()
  {
    System.out.println ();
    int pos = 0;
    for (ScreenPosition sp : screenPositions)
    {
      if (sp.isStartField ())
        System.out.print (sp.isHighIntensity () ? "&" : "%");
      else
        System.out.print (sp.getChar ());
      if (++pos % columns == 0)
        System.out.println ();
    }
  }

  public void dumpScreenPositions ()
  {
    int startFields = 0;
    for (ScreenPosition sp : screenPositions)
      if (sp.isStartField ())
        ++startFields;
    System.out.printf ("There are %d start fields%n", startFields);
  }

  public void debug ()
  {
    System.out.printf ("Total screen fields: %d%n", screenFields.size ());
  }
}