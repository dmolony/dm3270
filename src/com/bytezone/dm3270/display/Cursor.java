package com.bytezone.dm3270.display;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Cursor
{
  private static final boolean WITH_CURSOR = true;
  private static final boolean WITHOUT_CURSOR = false;

  private final Screen screen;
  private ScreenDimensions screenDimensions;

  private int currentPosition;
  private Field currentField;
  private boolean visible = false;    // this should match the keyboard locked status

  public enum Direction
  {
    LEFT, RIGHT, UP, DOWN
  }

  public Cursor (Screen screen, ScreenDimensions screenDimensions)
  {
    this.screen = screen;
    this.screenDimensions = screenDimensions;
  }

  public void setVisible (boolean visible)
  {
    this.visible = visible;
    if (visible)
    {
      setCurrentField ();
      notifyCursorMove (0, currentPosition, currentField, 0);
    }
    else
      resetCurrentField ();

    screen.drawPosition (currentPosition, visible);
  }

  public boolean isVisible ()
  {
    return visible;
  }

  void setScreenDimensions (ScreenDimensions screenDimensions)
  {
    this.screenDimensions = screenDimensions;
  }

  public ScreenPosition getScreenPosition ()
  {
    return screen.getScreenPosition (currentPosition);
  }

  public Field getCurrentField ()
  {
    if (currentField == null)
      setCurrentField ();
    return currentField;
  }

  public int getLocation ()
  {
    return currentPosition;
  }

  // ---------------------------------------------------------------------------------//
  // Update screen contents
  // ---------------------------------------------------------------------------------//

  // called from ConsoleKeyEvent when the user types
  // called from AIDCommand.checkForPrettyMove()
  public void typeChar (byte value)
  {
    if (currentField != null && currentField.isUnprotected ()
        && currentField.getCursorOffset () > 0)
    {
      if (screen.isInsertMode ())
      {
        int start = currentField.getCursorOffset ();
        int end = currentField.getDisplayLength ();

        // don't lose data from the end of the field
        byte lastByte = currentField.getByteAt (end);
        if (lastByte != 0x00 && lastByte != 0x40)
          return;

        currentField.push (start, end);
        currentField.draw ();     // draws the field without the cursor
      }

      screen.getScreenPosition (currentPosition).setChar (value);
      currentField.setModified (true);

      int newPosition = screen.validate (currentPosition + 1);
      if (!currentField.contains (newPosition))
        newPosition = currentField.getNextUnprotectedField ().getFirstLocation ();

      moveTo (newPosition);
    }
  }

  public void home ()
  {
    Optional<Field> field = screen.getHomeField ();
    if (field.isPresent ())
      moveTo (field.get ().getFirstLocation ());
  }

  public void backspace ()
  {
    if (currentField != null && currentField.isUnprotected ()
        && currentField.getCursorOffset () > 0)
    {
      int start = currentField.getCursorOffset ();
      if (start > 1)
      {
        moveTo (screen.validate (currentPosition - 1));
        int end = currentField.getDisplayLength ();
        currentField.pull (start - 1, end);
        currentField.setModified (true);
        notifyFieldChange (currentField, currentField);
      }

      currentField.draw ();                     // draws the field without the cursor
      screen.drawPosition (currentPosition, visible);    // redraw the cursor
    }
  }

  public void delete ()
  {
    if (currentField != null && currentField.isUnprotected ()
        && currentField.getCursorOffset () > 0)
    {
      int start = currentField.getCursorOffset ();
      if (start > 0)
      {
        int end = currentField.getDisplayLength ();
        currentField.pull (start, end);
        currentField.setModified (true);
        notifyFieldChange (currentField, currentField);
      }
      currentField.draw ();                      // draws the field without the cursor
      screen.drawPosition (currentPosition, visible);     // redraw the cursor
    }
  }

  public void eraseEOL ()
  {
    if (currentField != null && currentField.isUnprotected ()
        && currentField.getCursorOffset () > 0)
    {
      int start = currentField.getCursorOffset ();
      if (start > 0)
      {
        int end = currentField.getDisplayLength ();
        currentField.clearData (start, end);
        currentField.setModified (true);
        notifyFieldChange (currentField, currentField);
      }
      currentField.draw ();                     // draws the field without the cursor
      screen.drawPosition (currentPosition, visible);     // redraw the cursor
    }
  }

  // ---------------------------------------------------------------------------------//
  // Cursor movement
  // ---------------------------------------------------------------------------------//

  public void tab (boolean backTab)
  {
    if (currentField == null)
      return;

    Field newField = null;

    if (currentField.isUnprotected ())
    {
      int first = currentField.getFirstLocation ();
      int sfaPosition = screen.validate (first - 1);

      if (backTab)
      {
        if (currentPosition == first || currentPosition == sfaPosition)
          newField = currentField.getPreviousUnprotectedField ();
        else
          newField = currentField;
      }
      else
      {
        if (currentPosition == sfaPosition)
          newField = currentField;
        else
          newField = currentField.getNextUnprotectedField ();
      }
    }
    else
    {
      if (backTab)
        newField = currentField.getPreviousUnprotectedField ();
      else
        newField = currentField.getNextUnprotectedField ();
    }

    if (newField != null)
      moveTo (newField.getFirstLocation ());
  }

  public void newLine ()
  {
    if (currentField == null)
      return;

    int oldRow = currentPosition / screenDimensions.columns;
    int oldPosition = currentPosition;

    while (true)
    {
      tab (false);
      if (currentPosition <= oldPosition)     // backwards or didn't move
        break;
      int newRow = currentPosition / screenDimensions.columns;
      if (newRow != oldRow)
        break;
    }
  }

  public void move (Direction direction)
  {
    switch (direction)
    {
      case RIGHT:
        moveTo (currentPosition + 1);
        break;

      case LEFT:
        moveTo (currentPosition - 1);
        break;

      case UP:
        moveTo (currentPosition - screenDimensions.columns);
        break;

      case DOWN:
        moveTo (currentPosition + screenDimensions.columns);
        break;
    }
  }

  // called from Screen.eraseAllUnprotected()
  // called from Screen.draw()
  // called from Screen.clearScreen()
  // called from WriteCommand.process()
  // called from AIDCommand.process()
  // called from PluginsStage.processReply()
  public void moveTo (int newPosition)
  {
    int oldPosition = currentPosition;
    currentPosition = screen.validate (newPosition);

    if (currentPosition != oldPosition)
    {
      notifyCursorMove (oldPosition, currentPosition, currentField, 0);

      if (visible)
      {
        screen.drawPosition (oldPosition, WITHOUT_CURSOR);
        screen.drawPosition (currentPosition, WITH_CURSOR);
      }

      if (currentField != null && !currentField.contains (currentPosition))
        setCurrentField ();
    }
  }

  // ---------------------------------------------------------------------------------//
  // Update currentField
  // ---------------------------------------------------------------------------------//

  private void resetCurrentField ()
  {
    Field lastField = currentField;
    currentField = null;
    if (currentField != lastField)
      notifyFieldChange (lastField, currentField);
  }

  private void setCurrentField ()
  {
    Field lastField = currentField;
    //    currentField = screen.getFieldManager ().getFieldAt (currentPosition);
    Optional<Field> field = screen.getFieldManager ().getFieldAt (currentPosition);
    if (field.isPresent ())
    {
      currentField = field.get ();
      if (currentField != lastField)
        notifyFieldChange (lastField, currentField);
    }
  }

  // ---------------------------------------------------------------------------------//
  // FieldChangeListener
  // ---------------------------------------------------------------------------------//

  private final Set<FieldChangeListener> fieldChangeListeners = new HashSet<> ();

  void notifyFieldChange (Field oldField, Field currentField)
  {
    fieldChangeListeners.forEach (l -> l.fieldChanged (oldField, currentField));
  }

  public void addFieldChangeListener (FieldChangeListener listener)
  {
    if (!fieldChangeListeners.contains (listener))
      fieldChangeListeners.add (listener);
  }

  public void removeFieldChangeListener (FieldChangeListener listener)
  {
    if (fieldChangeListeners.contains (listener))
      fieldChangeListeners.remove (listener);
  }

  // ---------------------------------------------------------------------------------//
  // CursorMoveListener
  // ---------------------------------------------------------------------------------//

  private final Set<CursorMoveListener> cursorMoveListeners = new HashSet<> ();

  void notifyCursorMove (int oldLocation, int currentLocation, Field currentField,
      int offset)
  {
    cursorMoveListeners
        .forEach (l -> l.cursorMoved (oldLocation, currentLocation, currentField));
  }

  public void addCursorMoveListener (CursorMoveListener listener)
  {
    if (!cursorMoveListeners.contains (listener))
      cursorMoveListeners.add (listener);
  }

  public void removeCursorMoveListener (CursorMoveListener listener)
  {
    if (cursorMoveListeners.contains (listener))
      cursorMoveListeners.remove (listener);
  }
}