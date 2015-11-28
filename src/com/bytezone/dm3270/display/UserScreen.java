package com.bytezone.dm3270.display;

import com.bytezone.dm3270.attributes.ColorAttribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.orders.Order;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class UserScreen extends Canvas implements DisplayScreen
{
  private ScreenPosition[] screenPositions;
  private final int rows = 24;
  private final int columns = 80;
  private final int screenSize = rows * columns;

  private final int xOffset = 4;      // padding left and right
  private final int yOffset = 4;      // padding top and bottom

  private final AIDCommand command;
  private Pen pen;
  private final GraphicsContext gc;

  // created by ScreenHistory.add()
  UserScreen (AIDCommand command)
  {
    this.command = command;
    gc = getGraphicsContext2D ();
  }

  public boolean matches (AIDCommand command)
  {
    return this.command.matches (command);
  }

  void characterSizeChanged (FontData characterSize)
  {
    setWidth (characterSize.getWidth () * columns + xOffset * 2);
    setHeight (characterSize.getHeight () * rows + yOffset * 2);

    gc.setFont (characterSize.getFont ());
  }

  private void createScreen (FontData fontData)
  {
    characterSizeChanged (fontData);

    screenPositions = new ScreenPosition[screenSize];
    pen = new PenType1 (screenPositions);

    clearScreen ();
    for (Order order : command)
      order.process (this);
  }

  // called by ConsolePane.changeScreen()
  public void drawScreen (FontData fontData)
  {
    if (screenPositions == null)
      createScreen (fontData);

    int width = fontData.getWidth ();
    int height = fontData.getHeight ();
    int ascent = fontData.getAscent ();
    int descent = fontData.getDescent ();

    int pos = 0;
    for (int row = 0; row < rows; row++)
      for (int col = 0; col < columns; col++)
      {
        int x = xOffset + col * width;
        int y = yOffset + row * height;

        ScreenPosition screenPosition = screenPositions[pos++];
        if (screenPosition.getScreenContext () != null)
          screenPosition.draw (gc, x, y, false, width, height, ascent, descent);
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
    return pen.validate (position);
  }

  @Override
  public void clearScreen ()
  {
    eraseScreen ();
    pen.clearScreen ();
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

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("Rows %d, Columns %d%n", rows, columns));
    for (ScreenPosition sp : screenPositions)
    {
      text.append (sp);
      text.append ("\n");
    }
    return text.toString ();
  }
}