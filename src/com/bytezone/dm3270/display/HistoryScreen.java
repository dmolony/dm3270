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
  private final ScreenDimensions screenDimensions;

  private final AIDCommand command;
  private final ContextManager contextManager;
  private final FieldManager fieldManager;
  private Pen pen;
  private final GraphicsContext gc;

  // created by HistoryManager.add()
  HistoryScreen (ScreenDimensions screenDimensions, AIDCommand command,
      ContextManager contextManager, FieldManager fieldManager)
  {
    this.screenDimensions = screenDimensions;
    this.contextManager = contextManager;
    this.fieldManager = fieldManager;
    this.command = command;

    gc = getGraphicsContext2D ();
  }

  @Override
  public ScreenDimensions getScreenDimensions ()
  {
    return screenDimensions;
  }

  public boolean matches (AIDCommand command)
  {
    return this.command.matches (command);
  }

  // called by ConsolePane.changeScreen()
  public void drawScreen (FontDetails fontDetails)
  {
    if (screenPositions == null)
      createScreen (fontDetails);

    for (ScreenPosition screenPosition : screenPositions)
      screenPosition.draw (false);
  }

  private void createScreen (FontDetails fontDetails)
  {
    setWidth (fontDetails.width * screenDimensions.columns
        + screenDimensions.xOffset * 2);
    setHeight (fontDetails.height * screenDimensions.rows + screenDimensions.yOffset * 2);

    gc.setFont (fontDetails.font);

    screenPositions = new ScreenPosition[screenDimensions.size];
    pen = Pen.getInstance (screenPositions, gc, contextManager, screenDimensions);

    clearScreen ();
    for (Order order : command)
      order.process (this);

    List<List<ScreenPosition>> protoFields = FieldManager.divide (screenPositions);
    for (List<ScreenPosition> protoField : protoFields)
      fieldManager.setContexts (protoField);
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
    text.append (String.format ("Rows %d, Columns %d%n", screenDimensions.rows,
                                screenDimensions.columns));
    for (ScreenPosition sp : screenPositions)
    {
      text.append (sp);
      text.append ("\n");
    }
    return text.toString ();
  }
}