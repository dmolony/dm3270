package com.bytezone.dm3270.display;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Screen extends Canvas
{
  private final ScreenPosition[] screenPositions;
  private final CharacterSize characterSize;        // contains font-specific values
  private final FieldManager fieldManager = new FieldManager (this);

  public final int rows;
  public final int columns;
  public final int screenSize;

  private final float xOffset = 4;      // padding left and right
  private final float yOffset = 4;      // padding top and bottom

  // spacing between characters for outlining
  private final boolean expanded = true;
  private final double expandedWidth = .5;
  private final double expandedHeight = 1.6;

  public Screen (int rows, int columns, Font font)
  {
    this.rows = rows;
    this.columns = columns;
    screenSize = rows * columns;

    GraphicsContext gc = getGraphicsContext2D ();
    characterSize = new CharacterSize (font);

    screenPositions = new ScreenPosition[rows * columns];
    for (int i = 0; i < screenPositions.length; i++)
      screenPositions[i] = new ScreenPosition (gc, characterSize);
  }

  public void setFont (Font font)
  {
    characterSize.changeFont (font);

    setWidth (characterSize.width * columns + xOffset * 2
        + (expanded ? (columns - 1) * expandedWidth : 0));
    setHeight (characterSize.height * rows + yOffset * 2
        + (expanded ? (rows + 1) * expandedHeight : 0));

    getGraphicsContext2D ().setFont (font);
    drawScreen ();
  }

  public int validate (int position)
  {
    while (position < 0)
      position += screenSize;
    while (position >= screenSize)
      position -= screenSize;
    return position;
  }

  public ScreenPosition getScreenPosition (int position)
  {
    return screenPositions[position];
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

  public void drawPosition (int position, boolean hasCursor)
  {
    int row = position / columns;
    int col = position % columns;
    drawPosition (screenPositions[position], row, col, hasCursor);
  }

  public void drawScreen ()
  {
    int pos = 0;
    for (int row = 0; row < rows; row++)
      for (int col = 0; col < columns; col++)
        drawPosition (screenPositions[pos++], row, col, false);
  }

  public void clearScreen ()
  {
    GraphicsContext gc = getGraphicsContext2D ();
    gc.setFill (Color.BLACK);
    gc.fillRect (0, 0, getWidth (), getHeight ());
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
}