package com.bytezone.dm3270.display;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import com.bytezone.dm3270.attributes.ColorAttribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.orders.Order;

public class UserScreen extends Canvas implements DisplayScreen
{
  private ScreenPosition[] screenPositions;
  private final int rows = 24;
  private final int columns = 80;
  private final int screenSize = rows * columns;

  private final int xOffset = 4;      // padding left and right
  private final int yOffset = 4;      // padding top and bottom

  private final AIDCommand command;
  private final Pen pen;
  GraphicsContext gc = getGraphicsContext2D ();

  public UserScreen (AIDCommand command)
  {
    this.command = command;
    this.pen = new Pen (this);
  }

  private void createScreen (CharacterSize characterSize)
  {
    screenPositions = new ScreenPosition[screenSize];
    ScreenContext base = pen.getBase ();

    for (int i = 0; i < screenSize; i++)
      screenPositions[i] = new ScreenPosition (i, gc, characterSize, base);

    for (Order order : command)
      order.process (this);
  }

  public void drawScreen (CharacterSize characterSize)
  {
    if (screenPositions == null)
      createScreen (characterSize);

    int width = characterSize.getWidth ();
    int height = characterSize.getHeight ();

    int pos = 0;
    for (int row = 0; row < rows; row++)
      for (int col = 0; col < columns; col++)
      {
        int x = xOffset + col * width;
        int y = yOffset + row * height;

        ScreenPosition screenPosition = screenPositions[pos++];
        screenPosition.draw (x, y, false);
      }
  }

  @Override
  public Pen getPen ()
  {
    return pen;
  }

  @Override
  public ScreenPosition getScreenPosition (int position)
  {
    return screenPositions[position];
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

  private void eraseScreen ()
  {
    gc.setFill (ColorAttribute.colors[8]);                // black
    gc.fillRect (0, 0, getWidth (), getHeight ());
  }

  @Override
  public void insertCursor (int position)
  {
  }
}