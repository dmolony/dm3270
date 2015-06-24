package com.bytezone.dm3270.display;

import static com.bytezone.dm3270.application.Console.Function.TERMINAL;
import static com.bytezone.dm3270.commands.AIDCommand.NO_AID_SPECIFIED;

import java.awt.Toolkit;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import com.bytezone.dm3270.application.Console.Function;
import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.attributes.ColorAttribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.commands.WriteControlCharacter;
import com.bytezone.dm3270.filetransfer.FileStage;
import com.bytezone.dm3270.jobs.JobStage;
import com.bytezone.dm3270.plugins.PluginsStage;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class Screen extends Canvas implements DisplayScreen
{
  private final ScreenPacker screenPacker = new ScreenPacker (this);
  private byte currentAID;
  private byte replyMode;
  private byte[] replyTypes = new byte[0];

  private final ScreenPosition[] screenPositions;

  private final FieldManager fieldManager;
  private final FontManager fontManager;
  private final JobStage jobStage;
  private final FileStage fileStage;
  private final PluginsStage pluginsStage;
  private final Pen pen;

  private final Cursor cursor = new Cursor (this);
  private final Function function;
  private final GraphicsContext graphicsContext;

  private final int xOffset = 4; // padding left and right
  private final int yOffset = 4; // padding top and bottom

  public final int rows;
  public final int columns;
  public final int screenSize;

  private int insertedCursorPosition = -1;
  private boolean keyboardLocked;
  private boolean insertMode;
  private boolean readModifiedAll = false;

  private final boolean recording = true;
  private final ScreenHistory screenHistory = new ScreenHistory ();

  public enum BuildInstruction
  {
    BUILD_FIELDS, DONT_BUILD_FIELDS
  }

  public Screen (int rows, int columns, Preferences prefs, Function function,
      PluginsStage pluginsStage)
  {
    this.rows = rows;
    this.columns = columns;
    screenSize = rows * columns;
    this.function = function;

    pen = new Pen (this);
    fieldManager = new FieldManager (this, pen.getBase ());

    jobStage = new JobStage ();
    fileStage = new FileStage (prefs);
    this.pluginsStage = pluginsStage;
    pluginsStage.setScreen (this);

    graphicsContext = getGraphicsContext2D ();
    fontManager = new FontManager (this, prefs);

    // ScreenContext baseContext = fieldManager.getPen ().getBase ();
    ScreenContext baseContext = pen.getBase ();
    CharacterSize characterSize = fontManager.getCharacterSize ();

    screenPositions = new ScreenPosition[screenSize];
    for (int i = 0; i < screenSize; i++)
      screenPositions[i] =
          new ScreenPosition (i, graphicsContext, characterSize, baseContext);

    addTSOCommandStatusChangeListener (jobStage);
  }

  public void setConsolePane (ConsolePane consolePane)
  {
    jobStage.setConsolePane (consolePane);
  }

  public FieldManager getFieldManager ()
  {
    return fieldManager;
  }

  public FontManager getFontManager ()
  {
    return fontManager;
  }

  // called by WriteCommand.process()
  public PluginsStage getPluginsStage ()
  {
    return pluginsStage;
  }

  public JobStage getJobStage ()
  {
    return jobStage;
  }

  public FileStage getFileStage ()
  {
    return fileStage;
  }

  // called from ConsoleKeyEvent in order to fix a java bug on OSX
  public void doFontSmaller ()
  {
    fontManager.smaller ();
  }

  public Function getFunction ()
  {
    return function;
  }

  // display a message on the screen - only used when logging off
  public void displayText (String text)
  {
    graphicsContext.setFill (ColorAttribute.colors[8]); // black
    graphicsContext.fillRect (0, 0, getWidth (), getHeight ());
    graphicsContext.setFill (ColorAttribute.colors[5]); // turquoise

    int x = 120;
    int y = 100;
    int height = 20;

    String[] lines = text.split ("\n");
    for (String line : lines)
    {
      graphicsContext.fillText (line, x, y);
      y += height;
    }
  }

  @Override
  public int validate (int position)
  {
    while (position < 0)
      position += screenSize;
    while (position >= screenSize)
      position -= screenSize;
    return position;
  }

  @Override
  public Pen getPen ()
  {
    // return fieldManager.getPen ();
    return pen;
  }

  public Cursor getScreenCursor ()
  {
    return cursor;
  }

  @Override
  public ScreenPosition getScreenPosition (int position)
  {
    return screenPositions[position];
  }

  public ScreenPosition[] getScreenPositions ()
  {
    return screenPositions;
  }

  public void resetInsertMode ()
  {
    if (insertMode)
      toggleInsertMode ();
  }

  public void toggleInsertMode ()
  {
    insertMode = !insertMode;
    notifyKeyboardStatusChange ("");
  }

  public boolean isInsertMode ()
  {
    return insertMode;
  }

  public void insertCursor ()
  {
    insertedCursorPosition = cursor.getLocation (); // move it here later
  }

  @Override
  public void insertCursor (int position)
  {
    insertedCursorPosition = position; // move it here later
  }

  // called from EraseAllUnprotectedCommand.process()
  public void eraseAllUnprotected ()
  {
    Field firstUnprotectedField = fieldManager.eraseAllUnprotected ();

    restoreKeyboard ();
    resetModified ();
    setAID (AIDCommand.NO_AID_SPECIFIED);
    drawScreen ();

    if (firstUnprotectedField != null)
      cursor.moveTo (firstUnprotectedField.getFirstLocation ());
  }

  void drawPosition (int position, boolean hasCursor)
  {
    int row = position / columns;
    int col = position % columns;
    drawPosition (screenPositions[position], row, col, hasCursor);
  }

  public void buildFields (WriteControlCharacter wcc)
  {
    fieldManager.buildFields (); // what about resetModified?

    boolean isTSOCommandScreen = fieldManager.isTSOCommandScreen ();
    Field tsoCommandField = fieldManager.getTSOCommandField ();
    notifyTSOCommandStatusChange (isTSOCommandScreen, tsoCommandField);
  }

  public void checkRecording ()
  {
    if (recording && fieldManager.size () > 0 && !keyboardLocked)
    {
      byte savedReplyMode = replyMode;
      byte[] savedReplyTypes = getReplyTypes ();

      screenHistory.requestScreen (this); // will call readBuffer()

      replyMode = savedReplyMode;
      replyTypes = savedReplyTypes;
    }
  }

  public void drawScreen ()
  {
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

    drawPosition (cursor.getLocation (), true); // draw the cursor
  }

  private void drawPosition (ScreenPosition screenPosition, int row, int col,
      boolean hasCursor)
  {
    CharacterSize characterSize = fontManager.getCharacterSize (); // too slow!!
    int x = xOffset + col * characterSize.getWidth ();
    int y = yOffset + row * characterSize.getHeight ();

    screenPosition.draw (x, y, hasCursor);
  }

  void characterSizeChanged (CharacterSize characterSize)
  {
    setWidth (characterSize.getWidth () * columns + xOffset * 2);
    setHeight (characterSize.getHeight () * rows + yOffset * 2);

    graphicsContext.setFont (characterSize.getFont ());
  }

  @Override
  public void clearScreen ()
  {
    eraseScreen ();

    for (ScreenPosition sp : screenPositions)
      sp.reset ();

    cursor.moveTo (0);
    pen.reset ();
  }

  void eraseScreen ()
  {
    graphicsContext.setFill (ColorAttribute.colors[8]); // black
    graphicsContext.fillRect (0, 0, getWidth (), getHeight ());
  }

  public Field getHomeField ()
  {
    List<Field> fields = fieldManager.getUnprotectedFields ();
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

  public void setFieldText (Field field, String text)
  {
    try
    {
      field.setText (text.getBytes ("CP1047"));
      field.setModified (true);
      field.draw (); // draws the field without a cursor
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }
  }

  // ---------------------------------------------------------------------------------//
  // Convert screen contents to an AID command
  // ---------------------------------------------------------------------------------//

  // called from ConsoleKeyPress.handle() in response to a user command
  // called from this.readModifiedFields(0x..) below
  public AIDCommand readModifiedFields ()
  {
    return screenPacker.readModifiedFields (currentAID, getScreenCursor ().getLocation (),
                                            fieldManager, readModifiedAll);
  }

  // Called from ReadCommand.process() in response to a ReadBuffer (F2) command
  // Called from ReadPartitionSF.process() in response to a ReadBuffer (F2) command
  // Called from Screen.lockKeyboard()
  public AIDCommand readBuffer ()
  {
    return screenPacker.readBuffer (screenPositions, getScreenCursor ().getLocation (),
                                    currentAID, replyMode, replyTypes);
  }

  // Called from ReadCommand.process() in response to a ReadModified (F6)
  // or a ReadModifiedAll (6E) command
  // Called from ReadPartitionSF.process() in response to a ReadModified (F6)
  // or a ReadModifiedAll (6E) command
  public AIDCommand readModifiedFields (byte type)
  {
    switch (type)
    {
      case Command.READ_MODIFIED_F6:
        return readModifiedFields ();

      case Command.READ_MODIFIED_ALL_6E:
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
    setAID (NO_AID_SPECIFIED);
    cursor.setVisible (true);
    keyboardLocked = false;
    notifyKeyboardStatusChange ("");
  }

  public void lockKeyboard (String keyName)
  {
    keyboardLocked = true;
    notifyKeyboardStatusChange (keyName);

    if (function == TERMINAL)
      cursor.setVisible (false);
  }

  public void resetModified ()
  {
    for (Field field : fieldManager.getUnprotectedFields ())
      if (field.isModified ())
        field.setModified (false);
  }

  public boolean isKeyboardLocked ()
  {
    return keyboardLocked;
  }

  // ---------------------------------------------------------------------------------//
  // Listener events
  // ---------------------------------------------------------------------------------//

  private final Set<KeyboardStatusListener> keyboardStatusListeners = new HashSet<> ();

  void notifyKeyboardStatusChange (String keyName)
  {
    for (KeyboardStatusListener listener : keyboardStatusListeners)
      listener.keyboardStatusChanged (keyboardLocked, keyName, insertMode);
  }

  public void addStatusChangeListener (KeyboardStatusListener listener)
  {
    keyboardStatusListeners.add (listener);
  }

  public void removeStatusChangeListener (KeyboardStatusListener listener)
  {
    keyboardStatusListeners.remove (listener);
  }

  private final Set<TSOCommandStatusListener> tsoCommandStatusListeners =
      new HashSet<> ();

  void notifyTSOCommandStatusChange (boolean isTSOCommandScreen, Field tsoCommandField)
  {
    for (TSOCommandStatusListener listener : tsoCommandStatusListeners)
      listener.screenChanged (isTSOCommandScreen, tsoCommandField);
  }

  public void addTSOCommandStatusChangeListener (TSOCommandStatusListener listener)
  {
    tsoCommandStatusListeners.add (listener);
  }

  public void removeTSOCommandStatusChangeListener (TSOCommandStatusListener listener)
  {
    tsoCommandStatusListeners.remove (listener);
  }

  // ---------------------------------------------------------------------------------//
  // Screen history
  // ---------------------------------------------------------------------------------//

  public ScreenHistory pause () // triggered by cmd-s
  {
    if (screenHistory.size () == 0)
      return null;

    screenHistory.pause (keyboardLocked);
    keyboardLocked = true;

    return screenHistory;
  }

  public void resume () // also triggered by cmd-s
  {
    keyboardLocked = screenHistory.resume ();
  }

  // ---------------------------------------------------------------------------------//
  // Debugging
  // ---------------------------------------------------------------------------------//

  private void dumpScreenPositions ()
  {
    dumpScreenPositions (0, 1920);
  }

  private void dumpScreenPositions (int from, int to)
  {
    while (from < to)
      System.out.println (screenPositions[from++]);
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