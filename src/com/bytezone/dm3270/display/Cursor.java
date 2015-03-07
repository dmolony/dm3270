package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.attributes.Attribute;

public class Cursor
{
  private final Screen screen;

  private int currentPosition;
  private Field currentField;
  private boolean visible;

  private final List<Attribute> unappliedAttributes = new ArrayList<> ();

  public enum Direction
  {
    LEFT, RIGHT, UP, DOWN
  }

  public Cursor (Screen screen)
  {
    this.screen = screen;
  }

  public void draw ()
  {
    screen.drawPosition (currentPosition, visible);
  }

  public void setVisible (boolean visible)
  {
    this.visible = visible;
    if (visible)
    {
      setCurrentField ();
      notifyCursorMove (0, currentPosition);
    }
    else
      resetCurrentField ();
    draw ();
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
  public void typeChar (byte value)
  {
    if (currentField != null && currentField.isUnprotected ())
    {
      screen.getScreenPosition (currentPosition).setChar (value);
      currentField.setModified (true);

      int newPosition = screen.validate (currentPosition + 1);
      if (!currentField.contains (newPosition))
      {
        Field newField = currentField.getNextUnprotectedField ();
        newPosition = newField.getFirstLocation ();
      }
      moveTo (newPosition);
    }
    //    else
    //      System.out.println ("Can't type here");         // lock the keyboard?
  }

  public void backspace ()
  {
    if (currentField != null && currentField.isUnprotected ())
    {

      int first = currentField.getFirstLocation ();
      if (currentPosition != first)
      {
        int newPosition = screen.validate (currentPosition) - 1;
        screen.getScreenPosition (newPosition).setChar ((byte) 0x00);
        moveTo (newPosition);
      }
    }
  }

  // called from Orders when building the screen
  public void setChar (byte value)
  {
    ScreenPosition sp = screen.getScreenPosition (currentPosition);

    sp.reset ();
    if (unappliedAttributes.size () > 0)
      applyAttributes (sp);

    sp.setChar (value);
  }

  // called from Orders when building the screen
  public void setGraphicsChar (byte value)
  {
    ScreenPosition sp = screen.getScreenPosition (currentPosition);

    sp.reset ();
    if (unappliedAttributes.size () > 0)
      applyAttributes (sp);

    sp.setGraphicsChar (value);
  }

  public void add (Attribute attribute)
  {
    unappliedAttributes.add (attribute);
  }

  private void applyAttributes (ScreenPosition sp)
  {
    for (Attribute attribute : unappliedAttributes)
      sp.addAttribute (attribute);
    unappliedAttributes.clear ();
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

    moveTo (newField.getFirstLocation ());
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
        moveTo (currentPosition - screen.columns);
        break;

      case DOWN:
        moveTo (currentPosition + screen.columns);
        break;
    }
  }

  public void moveTo (int newPosition)
  {
    if (visible)
    {
      int oldPosition = currentPosition;
      screen.drawPosition (currentPosition, false);
      currentPosition = screen.validate (newPosition);
      screen.drawPosition (currentPosition, true);
      notifyCursorMove (oldPosition, currentPosition);
    }
    else
      currentPosition = screen.validate (newPosition);

    if (currentField != null && !currentField.contains (currentPosition))
      setCurrentField ();
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
    currentField = screen.getField (currentPosition);
    if (currentField != lastField)
      notifyFieldChange (lastField, currentField);
  }

  // ---------------------------------------------------------------------------------//
  // Listener events
  // ---------------------------------------------------------------------------------//

  private final List<FieldChangeListener> fieldChangeListeners = new ArrayList<> ();
  private final List<CursorMoveListener> cursorMoveListeners = new ArrayList<> ();

  void notifyFieldChange (Field oldField, Field currentField)
  {
    for (FieldChangeListener listener : fieldChangeListeners)
      listener.fieldChanged (oldField, currentField);
  }

  public void addFieldChangeListener (FieldChangeListener listener)
  {
    fieldChangeListeners.add (listener);
  }

  public void removeFieldChangeListener (FieldChangeListener listener)
  {
    fieldChangeListeners.remove (listener);
  }

  void notifyCursorMove (int oldLocation, int currentLocation)
  {
    for (CursorMoveListener listener : cursorMoveListeners)
      listener.cursorMoved (oldLocation, currentLocation);
  }

  public void addCursorMoveListener (CursorMoveListener listener)
  {
    cursorMoveListeners.add (listener);
  }

  public void removeCursorMoveListener (CursorMoveListener listener)
  {
    cursorMoveListeners.remove (listener);
  }
}