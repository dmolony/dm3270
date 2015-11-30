package com.bytezone.dm3270.display;

import java.util.List;

import com.bytezone.dm3270.attributes.ColorAttribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.orders.Order;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class HistoryScreen extends Canvas implements DisplayScreen
{
  private ScreenPosition[] screenPositions;
  private final int rows;
  private final int columns;
  private final int screenSize;

  private final int xOffset = 4;      // padding left and right
  private final int yOffset = 4;      // padding top and bottom

  private final AIDCommand command;
  private Pen pen;
  private final GraphicsContext gc;

  // created by HistoryManager.add()
  HistoryScreen (int rows, int columns, AIDCommand command)
  {
    this.command = command;
    this.rows = rows;
    this.columns = columns;
    screenSize = rows * columns;

    gc = getGraphicsContext2D ();
  }

  public boolean matches (AIDCommand command)
  {
    return this.command.matches (command);
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

  private void createScreen (FontData fontData)
  {
    setWidth (fontData.getWidth () * columns + xOffset * 2);
    setHeight (fontData.getHeight () * rows + yOffset * 2);

    gc.setFont (fontData.getFont ());

    screenPositions = new ScreenPosition[screenSize];
    pen = new PenType1 (screenPositions);

    clearScreen ();
    for (Order order : command)
      order.process (this);

    List<List<ScreenPosition>> protoFields = FieldManager.divide (screenPositions);
    for (List<ScreenPosition> protoField : protoFields)
      FieldManager.setContexts (protoField);
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
  public ScreenPosition[] getScreenPositions ()
  {
    return screenPositions;
  }

  @Override
  public int validate (int position)
  {
    return pen.validate (position);
  }

  @Override
  public void clearScreen ()
  {
    gc.setFill (ColorAttribute.colors[8]);                // black
    gc.fillRect (0, 0, getWidth (), getHeight ());
    pen.clearScreen ();
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