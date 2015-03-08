package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.Attribute.AttributeType;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.orders.BufferAddress;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.structuredfields.SetReplyMode;

public class Screen extends Canvas
{
  private static final boolean DONT_REBUILD_FIELDS = false;

  private final ScreenPosition[] screenPositions;
  private final CharacterSize characterSize;        // contains font-specific values
  private final FieldManager fieldManager = new FieldManager (this);
  private final ContextManager contextHandler = new ContextManager ();
  private final Cursor cursor = new Cursor (this);

  public final int rows;
  public final int columns;
  public final int screenSize;

  private final float xOffset = 4;      // padding left and right
  private final float yOffset = 4;      // padding top and bottom

  // spacing between characters for outlining
  private final boolean expanded = true;
  private final double expandedWidth = .5;
  private final double expandedHeight = 1.6;

  private int insertedCursorPosition = -1;
  private boolean keyboardLocked;
  private boolean resetModified;

  private byte currentAID;
  private byte replyMode;
  private byte[] replyTypes = new byte[0];

  public Screen (int rows, int columns, Font font)
  {
    this.rows = rows;
    this.columns = columns;
    screenSize = rows * columns;

    GraphicsContext gc = getGraphicsContext2D ();
    characterSize = new CharacterSize (font);
    setFont (font);     // yuk - twice

    screenPositions = new ScreenPosition[rows * columns];
    ScreenContext baseContext = contextHandler.getBase ();
    for (int i = 0; i < screenPositions.length; i++)
      screenPositions[i] = new ScreenPosition (gc, characterSize, baseContext);
  }

  public void setFont (Font font)
  {
    characterSize.changeFont (font);

    setWidth (characterSize.width * columns + xOffset * 2
        + (expanded ? (columns - 1) * expandedWidth : 0));
    setHeight (characterSize.height * rows + yOffset * 2
        + (expanded ? (rows + 1) * expandedHeight : 0));

    getGraphicsContext2D ().setFont (font);
    //    drawScreen ();
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

  public void insertCursor ()
  {
    insertedCursorPosition = cursor.getLocation ();    // move it here later
  }

  public void eraseAllUnprotected ()
  {
    Field firstUnprotectedField = fieldManager.eraseAllUnprotected ();

    restoreKeyboard ();
    resetModified ();
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
    cursor.setVisible (false);

    if (buildFields)
      fieldManager.buildFields ();      // what about resetModified?

    int pos = 0;
    for (int row = 0; row < rows; row++)
      for (int col = 0; col < columns; col++)
        drawPosition (screenPositions[pos++], row, col, false);

    // Cursor.moveTo() will recalculate the current field if it is visible
    cursor.setVisible (true);
    if (insertedCursorPosition >= 0)
    {
      cursor.moveTo (insertedCursorPosition);
      insertedCursorPosition = -1;
    }
    else
      cursor.moveTo (0);
  }

  private void drawPosition (ScreenPosition screenPosition, int row, int col,
      boolean hasCursor)
  {
    double x = xOffset + col * characterSize.width        //
        + (expanded ? col * expandedWidth : 0);
    double y = yOffset + row * characterSize.height       //
        + (expanded ? (row + 1) * expandedHeight : 0);

    screenPosition.draw (x, y, hasCursor);
  }

  public void clearScreen ()
  {
    cursor.setVisible (false);

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

  public AIDCommand readBuffer ()
  {
    byte[] buffer = new byte[4096];
    int ptr = 0;
    buffer[ptr++] = AIDCommand.AID_READ_PARTITION;

    int cursorLocation = getScreenCursor ().getLocation ();
    BufferAddress ba = new BufferAddress (cursorLocation);
    ptr = ba.packAddress (buffer, ptr);

    for (ScreenPosition sp : screenPositions)
    {
      if (sp.isStartField ())
      {
        StartFieldAttribute sfa = sp.getStartFieldAttribute ();

        if (replyMode == SetReplyMode.RM_FIELD)
        {
          buffer[ptr++] = Order.START_FIELD;
          buffer[ptr++] = sfa.getValue ();
        }
        else
        {
          buffer[ptr++] = Order.START_FIELD_EXTENDED;

          List<Attribute> attributes = sp.getAttributes ();
          buffer[ptr++] = (byte) (attributes.size () + 1);    // include SFA

          ptr = sfa.pack (buffer, ptr);
          for (Attribute attribute : attributes)
            ptr = attribute.pack (buffer, ptr);
        }
      }
      else
      {
        if (replyMode == SetReplyMode.RM_CHARACTER && sp.hasAttributes ())
        {
          List<Attribute> attributes = sp.getAttributes ();
          for (Attribute attribute : attributes)
          {
            if (attribute.getAttributeType () == AttributeType.RESET)
              ptr = attribute.pack (buffer, ptr);
            else
              for (byte b : replyTypes)
                if (attribute.matches (b))
                {
                  ptr = attribute.pack (buffer, ptr);
                  break;
                }
          }
        }

        if (sp.isGraphicsChar ())
          buffer[ptr++] = Order.GRAPHICS_ESCAPE;

        buffer[ptr++] = sp.getByte ();
      }
    }

    return new AIDCommand (this, buffer, 0, ptr);
  }

  public AIDCommand readModifiedFields ()     // in response to a user key press
  {
    byte[] buffer = new byte[4096];
    int ptr = 0;
    buffer[ptr++] = getAID ();        // whatever key was pressed

    int cursorLocation = getScreenCursor ().getLocation ();
    BufferAddress ba = new BufferAddress (cursorLocation);
    ptr = ba.packAddress (buffer, ptr);

    for (Field field : getUnprotectedFields ())
      if (field.isModified ())
      {
        buffer[ptr++] = Order.SET_BUFFER_ADDRESS;
        ba = new BufferAddress (field.getFirstLocation ());
        ptr = ba.packAddress (buffer, ptr);
        ptr = field.packData (buffer, ptr);
      }

    return new AIDCommand (this, buffer, 0, ptr);
  }

  public AIDCommand readModifiedFields (byte type)
  {
    return null;
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
    notifyKeyboardStatusChange (!keyboardLocked, keyboardLocked);
  }

  public void lockKeyboard ()
  {
    keyboardLocked = true;
    notifyKeyboardStatusChange (!keyboardLocked, keyboardLocked);
  }

  public void resetModified ()
  {
    resetModified = true;     // will happen after the screen is rebuilt
  }

  public boolean isKeyboardLocked ()
  {
    return keyboardLocked;
  }

  // ---------------------------------------------------------------------------------//
  // Listener events
  // ---------------------------------------------------------------------------------//

  private final List<KeyboardStatusListener> keyboardStatusListeners = new ArrayList<> ();

  void notifyKeyboardStatusChange (boolean oldValue, boolean newValue)
  {
    for (KeyboardStatusListener listener : keyboardStatusListeners)
      listener.keyboardStatusChanged (oldValue, newValue);
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