package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.input.KeyCode;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.orders.BufferAddress;

public class Cursor2
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

  public Cursor2 (Screen screen)
  {
    this.screen = screen;
  }

  public void tab (boolean backTab)
  {
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

  public void backspace ()
  {
    int first = currentField.getFirstLocation ();
    if (currentPosition != first)
    {
      int newPosition = screen.validate (currentPosition) - 1;
      screen.getScreenPosition (newPosition).setChar ((byte) 0x00);
      moveTo (newPosition);
    }
  }

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
  }

  // called from Orders when building the screen
  public void setChar (byte value)
  {
    ScreenPosition2 sp = screen.getScreenPosition (currentPosition);

    sp.reset ();
    if (unappliedAttributes.size () > 0)
      applyAttributes (sp);

    sp.setChar (value);
  }

  // called from Orders when building the screen
  public void setGraphicsChar (byte value)
  {
    ScreenPosition2 sp = screen.getScreenPosition (currentPosition);

    sp.reset ();
    if (unappliedAttributes.size () > 0)
      applyAttributes (sp);

    sp.setGraphicsChar (value);
  }

  public void add (Attribute attribute)
  {
    unappliedAttributes.add (attribute);
  }

  private void applyAttributes (ScreenPosition2 sp)
  {
    for (Attribute attribute : unappliedAttributes)
      sp.addAttribute (attribute);
    unappliedAttributes.clear ();
  }

  public void move (KeyCode keyCode)
  {
    if (keyCode == KeyCode.LEFT)
      move (Direction.LEFT);
    else if (keyCode == KeyCode.RIGHT)
      move (Direction.RIGHT);
    else if (keyCode == KeyCode.UP)
      move (Direction.UP);
    else if (keyCode == KeyCode.DOWN)
      move (Direction.DOWN);
  }

  public void move (Direction direction)
  {
    int newPosition = -1;

    switch (direction)
    {
      case RIGHT:
        newPosition = currentPosition + 1;
        break;

      case LEFT:
        newPosition = currentPosition - 1;
        break;

      case UP:
        newPosition = currentPosition - screen.columns;
        break;

      case DOWN:
        newPosition = currentPosition + screen.columns;
        break;
    }

    moveTo (newPosition);
  }

  public void moveTo (int newPosition)
  {
    if (visible)
    {
      screen.drawPosition (currentPosition, false);
      currentPosition = screen.validate (newPosition);
      screen.drawPosition (currentPosition, true);
    }
    else
      currentPosition = screen.validate (newPosition);

    if (currentField != null && !currentField.contains (currentPosition))
      currentField = screen.getField (currentPosition);
  }

  public void draw ()
  {
    screen.drawPosition (currentPosition, visible);
  }

  public void setVisible (boolean visible)
  {
    this.visible = visible;
    draw ();
  }

  public ScreenPosition2 getScreenPosition ()
  {
    return screen.getScreenPosition (currentPosition);
  }

  public Field getCurrentField ()
  {
    if (currentField == null)
      currentField = screen.getField (currentPosition);
    return currentField;
  }

  public int getLocation ()
  {
    return currentPosition;
  }

  public BufferAddress getBufferAddress ()
  {
    return new BufferAddress (currentPosition);
  }

  // called from WCC.process() - we are about to process orders so all the screen
  // fields have been reset and moveTo() will be called repeatedly
  public void reset ()
  {
    currentField = null;
  }
}