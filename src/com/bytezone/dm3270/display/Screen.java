package com.bytezone.dm3270.display;

import static com.bytezone.dm3270.application.Console.Function.TERMINAL;
import static com.bytezone.dm3270.commands.AIDCommand.NO_AID_SPECIFIED;

import java.awt.Toolkit;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.prefs.Preferences;

import com.bytezone.dm3270.application.Console.Function;
import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.application.Site;
import com.bytezone.dm3270.assistant.AssistantStage;
import com.bytezone.dm3270.attributes.ColorAttribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.commands.WriteControlCharacter;
import com.bytezone.dm3270.plugins.PluginsStage;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;

public class Screen extends Canvas implements DisplayScreen
{
  private final static Toolkit defaultToolkit = Toolkit.getDefaultToolkit ();

  private final ScreenPacker screenPacker;
  private final Function function;

  private final ScreenPosition[] screenPositions;
  private final FieldManager fieldManager;
  private final FontManager fontManager;
  private final PluginsStage pluginsStage;
  private final AssistantStage assistantStage;
  private ConsolePane consolePane;

  private final Pen pen;
  private final Cursor cursor = new Cursor (this);

  private final int xOffset = 4;              // padding left and right
  private final int yOffset = 4;              // padding top and bottom

  public final int rows;
  public final int columns;
  public final int screenSize;

  private byte currentAID;
  private byte replyMode;
  private byte[] replyTypes = new byte[0];

  private int insertedCursorPosition = -1;
  private boolean keyboardLocked;
  private boolean insertMode;
  private boolean readModifiedAll = false;

  private final ScreenHistory screenHistory = new ScreenHistory ();

  public enum BuildInstruction
  {
    BUILD_FIELDS, DONT_BUILD_FIELDS
  }

  public Screen (int rows, int columns, Preferences prefs, Function function,
      PluginsStage pluginsStage, Site site)
  {
    this.rows = rows;
    this.columns = columns;
    screenSize = rows * columns;
    this.function = function;

    pen = new PenType1 (this);
    fieldManager = new FieldManager (this);
    assistantStage = new AssistantStage (this, site);

    screenPacker = new ScreenPacker ();
    screenPacker.addTSOCommandListener (assistantStage);
    addKeyboardStatusChangeListener (assistantStage);

    this.pluginsStage = pluginsStage;
    pluginsStage.setScreen (this);

    fontManager = new FontManager (this, prefs);
    FontData fontData = fontManager.getFontData ();

    ScreenContext baseContext = pen.getDefaultScreenContext ();
    screenPositions = new ScreenPosition[screenSize];
    GraphicsContext graphicsContext = getGraphicsContext2D ();

    for (int i = 0; i < screenSize; i++)
      screenPositions[i] = new ScreenPosition (i, graphicsContext, fontData, baseContext);

    fieldManager.addScreenChangeListener (assistantStage);
    fieldManager.addScreenChangeListener (screenPacker);
  }

  public void setStatusText (String text)
  {
    consolePane.setStatusText (text);
  }

  // this is called from the ConsolePane constructor
  // ConsolePane is needed to send an AID command
  public void setConsolePane (ConsolePane consolePane)
  {
    this.consolePane = consolePane;
    assistantStage.setConsolePane (consolePane);
    addKeyboardStatusChangeListener (consolePane);
  }

  public FieldManager getFieldManager ()
  {
    return fieldManager;
  }

  public boolean isTSOCommandScreen ()
  {
    return fieldManager.getScreenDetails ().isTSOCommandScreen ();
  }

  public Field getTSOCommandField ()
  {
    return fieldManager.getScreenDetails ().getTSOCommandField ();
  }

  public String getPrefix ()
  {
    return fieldManager.getScreenDetails ().getPrefix ();
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

  public AssistantStage getAssistantStage ()
  {
    return assistantStage;
  }

  public void closeAssistantStage ()
  {
    assistantStage.closeWindow ();
  }

  public Function getFunction ()
  {
    return function;
  }

  public String getPreviousTSOCommand ()
  {
    return screenPacker.getPreviousTSOCommand ();
  }

  // called from AIDCommand.process()
  public void addTSOCommand (String command)
  {
    screenPacker.addTSOCommand (command);
  }

  public void listTSOCommands ()
  {
    screenPacker.listTSOCommands ();
  }

  // display a message on the screen - only used when logging off
  public void displayText (String text)
  {
    GraphicsContext graphicsContext = getGraphicsContext2D ();
    graphicsContext.setFill (ColorAttribute.colors[8]);// black
    graphicsContext.fillRect (0, 0, getWidth (), getHeight ());
    graphicsContext.setFill (ColorAttribute.colors[5]);// turquoise

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
    fireKeyboardStatusChange ("");
  }

  public boolean isInsertMode ()
  {
    return insertMode;
  }

  public void insertCursor ()
  {
    insertedCursorPosition = cursor.getLocation ();   // move it here later
  }

  @Override
  public void insertCursor (int position)
  {
    insertedCursorPosition = position;                // move it here later
  }

  // called from EraseAllUnprotectedCommand.process()
  public void eraseAllUnprotected ()
  {
    Optional<Field> firstUnprotectedField = fieldManager.eraseAllUnprotected ();

    restoreKeyboard ();         // resets the AID to NO_AID_SPECIFIED
    resetModified ();
    //    setAID (AIDCommand.NO_AID_SPECIFIED);
    draw ();

    if (firstUnprotectedField.isPresent ())
      cursor.moveTo (firstUnprotectedField.get ().getFirstLocation ());
  }

  public void buildFields (WriteControlCharacter wcc)
  {
    fieldManager.buildFields ();              // what about resetModified?
  }

  // called from WriteCommand.process()
  public void checkRecording ()
  {
    byte savedReplyMode = replyMode;
    byte[] savedReplyTypes = replyTypes;

    screenHistory.requestScreen (this);     // calls setReplyMode() and readBuffer()

    setReplyMode (savedReplyMode, savedReplyTypes);
  }

  public void draw ()
  {
    FontData characterSize = fontManager.getFontData ();
    int charHeight = characterSize.getHeight ();
    int charWidth = characterSize.getWidth ();
    int pos = 0;

    for (int row = 0; row < rows; row++)
      for (int col = 0; col < columns; col++)
        drawPosition (screenPositions[pos++], row, col, false, charHeight, charWidth);

    if (insertedCursorPosition >= 0)
    {
      cursor.moveTo (insertedCursorPosition);
      insertedCursorPosition = -1;
      cursor.setVisible (true);
    }

    drawPosition (cursor.getLocation (), true);             // draw the cursor
  }

  void drawPosition (int position, boolean hasCursor)
  {
    int row = position / columns;
    int col = position % columns;
    drawPosition (screenPositions[position], row, col, hasCursor);
  }

  // called from FontManager when a new font is selected
  public void redraw ()
  {
    ((Stage) getScene ().getWindow ()).sizeToScene ();
    eraseScreen ();
    draw ();
  }

  private void drawPosition (ScreenPosition screenPosition, int row, int col,
      boolean hasCursor)
  {
    FontData characterSize = fontManager.getFontData ();
    int x = xOffset + col * characterSize.getWidth ();
    int y = yOffset + row * characterSize.getHeight ();

    screenPosition.draw (x, y, hasCursor);
  }

  private void drawPosition (ScreenPosition screenPosition, int row, int col,
      boolean hasCursor, int charHeight, int charWidth)
  {
    int x = xOffset + col * charWidth;
    int y = yOffset + row * charHeight;

    screenPosition.draw (x, y, hasCursor);
  }

  void characterSizeChanged (FontData fontData)
  {
    setWidth (fontData.getWidth () * columns + xOffset * 2);
    setHeight (fontData.getHeight () * rows + yOffset * 2);

    GraphicsContext graphicsContext = getGraphicsContext2D ();
    graphicsContext.setFont (fontData.getFont ());
    if (consolePane != null)
      consolePane.setFontData (fontData);
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
    GraphicsContext graphicsContext = getGraphicsContext2D ();
    graphicsContext.setFill (ColorAttribute.colors[8]);             // black
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

  // called from SetReplyModeSF
  public void setReplyMode (byte replyMode, byte[] replyTypes)
  {
    this.replyMode = replyMode;
    this.replyTypes = replyTypes;
  }

  public void setFieldText (Field field, String text)
  {
    try
    {
      field.setText (text.getBytes ("CP1047"));
      field.setModified (true);
      field.draw ();// draws the field without a cursor
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

  // Called from:
  //      ReadCommand.process() in response to a ReadBuffer (F2) command
  //      ReadPartitionSF.process() in response to a ReadBuffer (F2) command
  //      ScreenHistory.requestScreen()
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
    defaultToolkit.beep ();
  }

  public void restoreKeyboard ()
  {
    setAID (NO_AID_SPECIFIED);
    cursor.setVisible (true);
    keyboardLocked = false;
    fireKeyboardStatusChange ("");
  }

  public void lockKeyboard (String keyName)
  {
    keyboardLocked = true;
    fireKeyboardStatusChange (keyName);

    if (function == TERMINAL)
      cursor.setVisible (false);
  }

  public void resetModified ()
  {
    //    for (Field field : fieldManager.getUnprotectedFields ())
    //      if (field.isModified ())
    //        field.setModified (false);

    fieldManager.getUnprotectedFields ().forEach (f -> f.setModified (false));
  }

  public boolean isKeyboardLocked ()
  {
    return keyboardLocked;
  }

  // ---------------------------------------------------------------------------------//
  // Listener events
  // ---------------------------------------------------------------------------------//

  private final Set<KeyboardStatusListener> keyboardChangeListeners = new HashSet<> ();

  private void fireKeyboardStatusChange (String keyName)
  {
    KeyboardStatusChangedEvent evt =
        new KeyboardStatusChangedEvent (insertMode, keyboardLocked, keyName);
    keyboardChangeListeners.forEach (l -> l.keyboardStatusChanged (evt));
  }

  public void addKeyboardStatusChangeListener (KeyboardStatusListener listener)
  {
    if (!keyboardChangeListeners.contains (listener))
      keyboardChangeListeners.add (listener);
  }

  public void removeKeyboardStatusChangeListener (KeyboardStatusListener listener)
  {
    if (keyboardChangeListeners.contains (listener))
      keyboardChangeListeners.remove (listener);
  }

  // ---------------------------------------------------------------------------------//
  // Screen history
  // ---------------------------------------------------------------------------------//

  public Optional<ScreenHistory> pause ()             // triggered by cmd-s
  {
    if (screenHistory.size () == 0)
      return Optional.empty ();

    screenHistory.pause (keyboardLocked);
    keyboardLocked = true;

    return Optional.of (screenHistory);
  }

  public void resume ()                     // also triggered by cmd-s
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
        text.append (sp.getCharString ());
      if (++pos % columns == 0)
        text.append ("\n");
    }

    text.append ("\n");
    text.append (fieldManager.getTotalsText ());

    return text.toString ();
  }
}