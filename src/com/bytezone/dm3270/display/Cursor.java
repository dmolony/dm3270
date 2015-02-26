package com.bytezone.dm3270.display;

public class Cursor
{
  private final Screen screen;

  private int currentPosition;
  private Field currentField;

  public Cursor (Screen screen)
  {
    this.screen = screen;
  }

  public void draw ()
  {
    screen.drawPosition (currentPosition, true);
  }

  public void moveTo (int position)
  {
    screen.drawPosition (currentPosition, false);
    currentPosition = screen.validate (position);
    screen.drawPosition (currentPosition, true);

    if (!currentField.contains (currentPosition))
      currentField = screen.getField (currentPosition);
  }
}
