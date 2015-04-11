package com.bytezone.dm3270.display;

import java.awt.Toolkit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import com.bytezone.dm3270.application.Console.Function;
import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.Attribute.AttributeType;
import com.bytezone.dm3270.attributes.ColorAttribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.orders.BufferAddress;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.structuredfields.SetReplyMode;

public class Screen extends Canvas
{
  private static final boolean DONT_REBUILD_FIELDS = false;
  private static final byte[] buffer = new byte[4096];

  private final ScreenPosition[] screenPositions;
  private final CharacterSize characterSize;        // contains font-specific values
  private final FieldManager fieldManager = new FieldManager (this);
  private final ContextManager contextHandler = new ContextManager ();
  private final Cursor cursor = new Cursor (this);
  private final Function function;

  private final float xOffset = 4;      // padding left and right
  private final float yOffset = 4;      // padding top and bottom

  private int insertedCursorPosition = -1;
  private boolean keyboardLocked;
  private boolean insertMode;
  private boolean readModifiedAll = false;

  private byte currentAID;
  private byte replyMode;
  private byte[] replyTypes = new byte[0];

  public final int rows;
  public final int columns;
  public final int screenSize;

  public Screen (int rows, int columns, Font font, Function function)
  {
    this.rows = rows;
    this.columns = columns;
    screenSize = rows * columns;
    this.function = function;

    GraphicsContext gc = getGraphicsContext2D ();
    //    gc.translate (0.5, 0.5);                            // what does this do?
    characterSize = new CharacterSize (font);
    setFont (font);

    screenPositions = new ScreenPosition[rows * columns];
    ScreenContext baseContext = contextHandler.getBase ();
    for (int i = 0; i < screenPositions.length; i++)
      screenPositions[i] = new ScreenPosition (gc, characterSize, baseContext);
  }

  private void setFont (Font font)
  {
    characterSize.changeFont (font);

    setWidth (characterSize.getWidth () * columns + xOffset * 2);
    setHeight (characterSize.getHeight () * rows + yOffset * 2);

    getGraphicsContext2D ().setFont (font);
  }

  public Function getFunction ()
  {
    return function;
  }

  public void displayText (String text)
  {
    GraphicsContext gc = getGraphicsContext2D ();
    gc.setFill (Color.BLACK);
    gc.fillRect (0, 0, getWidth (), getHeight ());
    gc.setFill (ColorAttribute.colors[5]);
    int x = 120;
    int y = 100;
    int height = 20;

    String[] lines = text.split ("\n");
    for (String line : lines)
    {
      gc.fillText (line, x, y);
      y += height;
    }
  }

  public int validate (int position)
  {
    while (position < 0)
      position += screenSize;
    while (position >= screenSize)
      position -= screenSize;
    return position;
  }

  public Cursor getScreenCursor ()
  {
    return cursor;
  }

  public ScreenPosition getScreenPosition (int position)
  {
    return screenPositions[position];
  }

  public ScreenPosition[] getScreenPositions ()
  {
    return screenPositions;
  }

  public ContextManager getContextHandler ()
  {
    return contextHandler;
  }

  public void toggleInsertMode ()
  {
    insertMode = !insertMode;
    notifyKeyboardStatusChange ();
  }

  public boolean isInsertMode ()
  {
    return insertMode;
  }

  public void insertCursor ()
  {
    insertedCursorPosition = cursor.getLocation ();    // move it here later
  }

  // called from EraseAllUnprotectedCommand.process()
  public void eraseAllUnprotected ()
  {
    Field firstUnprotectedField = fieldManager.eraseAllUnprotected ();

    restoreKeyboard ();
    resetModified ();
    setAID (AIDCommand.NO_AID_SPECIFIED);
    drawScreen (DONT_REBUILD_FIELDS);

    if (firstUnprotectedField != null)
      cursor.moveTo (firstUnprotectedField.getFirstLocation ());
  }

  void drawPosition (int position, boolean hasCursor)
  {
    int row = position / columns;
    int col = position % columns;
    drawPosition (screenPositions[position], row, col, hasCursor);
  }

  public void drawScreen (boolean buildFields)
  {
    if (buildFields)
      fieldManager.buildFields ();      // what about resetModified?

    int pos = 0;
    for (int row = 0; row < rows; row++)
      for (int col = 0; col < columns; col++)
        drawPosition (screenPositions[pos++], row, col, false);

    if (insertedCursorPosition >= 0)
    {
      cursor.moveTo (insertedCursorPosition);
      insertedCursorPosition = -1;
      cursor.setVisible (true);
    }

    drawPosition (cursor.getLocation (), true);
  }

  private void drawPosition (ScreenPosition screenPosition, int row, int col,
      boolean hasCursor)
  {
    double x = xOffset + col * characterSize.getWidth ();
    double y = yOffset + row * characterSize.getHeight ();

    screenPosition.draw (x, y, hasCursor);
  }

  public void clearScreen ()
  {
    GraphicsContext gc = getGraphicsContext2D ();
    gc.setFill (Color.BLACK);
    gc.fillRect (0, 0, getWidth (), getHeight ());

    for (ScreenPosition sp : screenPositions)
      sp.reset ();

    cursor.moveTo (0);
  }

  @Override
  public boolean isResizable ()     // should apply to the Stage, not the Canvas
  {
    return false;
  }

  public Field getField (int position)
  {
    return fieldManager.getField (position);
  }

  public List<Field> getFields ()
  {
    return fieldManager.getFields ();
  }

  public List<Field> getUnprotectedFields ()
  {
    return fieldManager.getUnprotectedFields ();
  }

  public Field getHomeField ()
  {
    List<Field> fields = getUnprotectedFields ();
    if (fields != null && fields.size () > 0)
      return fields.get (0);
    return null;
  }

  public void setAID (byte aid)
  {
    currentAID = aid;
  }

  public byte getAID ()
  {
    return currentAID;
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
  // Convert screen contents to an AID command
  // ---------------------------------------------------------------------------------//

  // called from ConsoleKeyPress.handle() in response to a user command
  // called from readModifiedFields(0x..) below
  public AIDCommand readModifiedFields ()
  {
    // pack the AID
    int ptr = 0;
    buffer[ptr++] = currentAID;               // whatever key was pressed

    // PA keys and the CLR key only return the AID byte
    if (!readModifiedAll)
      if (currentAID == AIDCommand.AID_PA1 || currentAID == AIDCommand.AID_PA2
          || currentAID == AIDCommand.AID_PA3 || currentAID == AIDCommand.AID_CLEAR)
        return new AIDCommand (this, buffer, 0, ptr);

    // pack the cursor address
    BufferAddress ba = new BufferAddress (getScreenCursor ().getLocation ());
    ptr = ba.packAddress (buffer, ptr);

    // pack all modified fields
    for (Field field : getUnprotectedFields ())
      if (field.isModified ())
        ptr = packField (field, buffer, ptr);

    return new AIDCommand (this, buffer, 0, ptr);
  }

  // Called from ReadCommand.process() in response to a ReadBuffer (F2) command
  // Called from ReadPartitionSF.process() in response to a ReadBuffer (F2) command
  public AIDCommand readBuffer ()
  {
    // pack the AID
    int ptr = 0;
    buffer[ptr++] = currentAID;

    // pack the cursor address
    BufferAddress ba = new BufferAddress (getScreenCursor ().getLocation ());
    ptr = ba.packAddress (buffer, ptr);

    // pack every screen location
    for (ScreenPosition sp : screenPositions)
      if (sp.isStartField ())
        ptr = packStartPosition (sp, buffer, ptr);
      else
        ptr = packDataPosition (sp, buffer, ptr);       // don't suppress nulls

    return new AIDCommand (this, buffer, 0, ptr);
  }

  // Called from ReadCommand.process() in response to a ReadModified (F6)
  // or a ReadModifiedAll (6E) command
  // Called from ReadPartitionSF.process() in response to a ReadModified (F6)
  // or a ReadModifiedAll (6E) command
  public AIDCommand readModifiedFields (byte type)
  {
    switch (type)
    {
      case (byte) 0xF6:
        return readModifiedFields ();

      case 0x6E:
        readModifiedAll = true;
        AIDCommand command = readModifiedFields ();
        readModifiedAll = false;
        return command;

      default:
        System.out.println ("Unknown type in Screen.readModifiedFields()");
    }

    return null;
  }

  // ---------------------------------------------------------------------------------//
  // Pack ScreenPosition routines - based on current ReplyMode setting
  // ---------------------------------------------------------------------------------//

  private int packStartPosition (ScreenPosition sp, byte[] buffer, int ptr)
  {
    assert sp.isStartField ();

    StartFieldAttribute sfa = sp.getStartFieldAttribute ();

    if (replyMode == SetReplyMode.RM_FIELD)
    {
      buffer[ptr++] = Order.START_FIELD;
      buffer[ptr++] = sfa.getAttributeValue ();
    }
    else
    {
      buffer[ptr++] = Order.START_FIELD_EXTENDED;

      List<Attribute> attributes = sp.getAttributes ();
      buffer[ptr++] = (byte) (attributes.size () + 1);    // include StartFieldAttribute

      ptr = sfa.pack (buffer, ptr);                       // pack the SFA first
      for (Attribute attribute : attributes)
        ptr = attribute.pack (buffer, ptr);               // then pack the rest
    }
    return ptr;
  }

  private int packDataPosition (ScreenPosition sp, byte[] buffer, int ptr)
  {
    if (replyMode == SetReplyMode.RM_CHARACTER)
      for (Attribute attribute : sp.getAttributes ())
        if (attribute.getAttributeType () == AttributeType.RESET)
        {
          buffer[ptr++] = Order.SET_ATTRIBUTE;
          ptr = attribute.pack (buffer, ptr);
        }
        else
          for (byte b : replyTypes)
            if (attribute.matches (b))
            {
              buffer[ptr++] = Order.SET_ATTRIBUTE;
              ptr = attribute.pack (buffer, ptr);
              break;
            }

    if (sp.isGraphicsChar ())
      buffer[ptr++] = Order.GRAPHICS_ESCAPE;

    buffer[ptr++] = sp.getByte ();

    return ptr;
  }

  private int packField (Field field, byte[] buffer, int ptr)
  {
    assert field.isModified ();

    for (ScreenPosition sp : field)
      if (sp.isStartField ())
      {
        buffer[ptr++] = Order.SET_BUFFER_ADDRESS;
        BufferAddress ba = new BufferAddress (field.getFirstLocation ());
        ptr = ba.packAddress (buffer, ptr);
      }
      else if (!sp.isNull ())
        ptr = packDataPosition (sp, buffer, ptr);       // suppress nulls

    return ptr;
  }

  // ---------------------------------------------------------------------------------//
  // Events to be processed from WriteControlCharacter.process()
  // ---------------------------------------------------------------------------------//

  public void resetPartition ()
  {
  }

  public void startPrinter ()
  {
  }

  public void soundAlarm ()
  {
    Toolkit.getDefaultToolkit ().beep ();
  }

  public void restoreKeyboard ()
  {
    keyboardLocked = false;
    notifyKeyboardStatusChange ();
    setAID (AIDCommand.NO_AID_SPECIFIED);
    cursor.setVisible (true);
  }

  public void lockKeyboard ()
  {
    keyboardLocked = true;
    notifyKeyboardStatusChange ();
    if (function == Function.TERMINAL)
      cursor.setVisible (false);
  }

  public void resetModified ()
  {
    int fieldsReset = 0;
    for (Field field : getUnprotectedFields ())
      if (field.isModified ())
      {
        field.setModified (false);
        ++fieldsReset;
      }

    //    if (fieldsReset > 0)
    //      System.out.printf ("Fields reset: %d%n", fieldsReset);
  }

  public boolean isKeyboardLocked ()
  {
    return keyboardLocked;
  }

  // ---------------------------------------------------------------------------------//
  // Listener events
  // ---------------------------------------------------------------------------------//

  private final Set<KeyboardStatusListener> keyboardStatusListeners = new HashSet<> ();

  void notifyKeyboardStatusChange ()
  {
    for (KeyboardStatusListener listener : keyboardStatusListeners)
      listener.keyboardStatusChanged (keyboardLocked, insertMode);
  }

  public void addStatusChangeListener (KeyboardStatusListener listener)
  {
    keyboardStatusListeners.add (listener);
  }

  public void removeStatusChangeListener (KeyboardStatusListener listener)
  {
    keyboardStatusListeners.remove (listener);
  }

  // ---------------------------------------------------------------------------------//
  // Debugging
  // ---------------------------------------------------------------------------------//

  public String getFieldText ()
  {
    return fieldManager.getFieldsText ();
  }

  public String getScreenText ()
  {
    StringBuilder text = new StringBuilder ();
    int pos = 0;
    for (ScreenPosition sp : screenPositions)
    {
      if (sp.isStartField ())
        text.append ("%");
      else
        text.append (sp.getChar ());
      if (++pos % columns == 0)
        text.append ("\n");
    }

    text.append ("\n");
    text.append (fieldManager.getTotalsText ());

    return text.toString ();
  }
}