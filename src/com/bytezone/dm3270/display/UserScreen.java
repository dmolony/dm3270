package com.bytezone.dm3270.display;

import javafx.scene.canvas.Canvas;

import com.bytezone.dm3270.commands.AIDCommand;

public class UserScreen extends Canvas implements DisplayScreen
{
  private ScreenPosition[] screenPositions;
  private final int rows = 24;
  private final int columns = 80;
  private final int screenSize = rows * columns;

  private final int xOffset = 4;      // padding left and right
  private final int yOffset = 4;      // padding top and bottom

  private CharacterSize characterSize;
  private final AIDCommand command;
  private final Pen pen;

  public UserScreen (AIDCommand command)
  {
    this.command = command;
    this.pen = new Pen (this);
  }

  private void createScreen ()
  {
    screenPositions = new ScreenPosition[screenSize];
  }

  public void drawScreen (CharacterSize characterSize)
  {
    if (screenPositions == null)
      createScreen ();

    int pos = 0;
    for (int row = 0; row < rows; row++)
      for (int col = 0; col < columns; col++)
        drawPosition (screenPositions[pos++], row, col, false);
  }

  private void drawPosition (ScreenPosition screenPosition, int row, int col,
      boolean hasCursor)
  {
    int x = xOffset + col * characterSize.getWidth ();
    int y = yOffset + row * characterSize.getHeight ();

    screenPosition.draw (x, y, hasCursor);
  }

  @Override
  public Pen getPen ()
  {
    return pen;
  }

  @Override
  public ScreenPosition getScreenPosition (int position)
  {
    return null;
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
  public void clearScreen ()
  {
  }

  @Override
  public void insertCursor (int position)
  {
  }
}