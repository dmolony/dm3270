package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.ColorAttribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.orders.Order;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class HistoryScreen extends Canvas implements DisplayScreen
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

  private final List<Field> fields = new ArrayList<> ();

  // created by ScreenHistory.add()
  HistoryScreen (AIDCommand command)
  {
    this.command = command;
    gc = getGraphicsContext2D ();
  }

  public boolean matches (AIDCommand command)
  {
    return this.command.matches (command);
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

    buildFields ();
    //    fields.forEach (System.out::println);
  }

  private void buildFields ()
  {
    List<ScreenPosition> positions = new ArrayList<ScreenPosition> ();

    int start = -1;
    int first = -1;
    int ptr = 0;

    while (ptr != first)            // not wrapped around to the first field yet
    {
      ScreenPosition screenPosition = screenPositions[ptr];

      if (screenPosition.isStartField ())   // check for the start of a new field
      {
        if (start >= 0)                     // if there is a field to add
        {
          fields.add (new Field (positions));
          positions.clear ();
        }
        else
          first = ptr;                      // this is the first field on the screen

        start = ptr;                        // beginning of the current field
      }

      // add ScreenPosition to the current field
      if (start >= 0)                       // if we are in a field...
        positions.add (screenPosition);     // collect next field's positions

      // increment ptr and wrap around
      if (++ptr == screenPositions.length)       // faster than validate()
      {
        ptr = 0;
        if (first == -1)
          break;                            // wrapped around and still no fields
      }
    }

    if (start >= 0 && positions.size () > 0)
      fields.add (new Field (positions));
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

  class Field
  {
    private final StartFieldAttribute startFieldAttribute;
    private final List<ScreenPosition> positions;

    public Field (List<ScreenPosition> positions)
    {
      startFieldAttribute = positions.get (0).getStartFieldAttribute ();
      this.positions = new ArrayList<> (positions);
      setContexts ();
    }

    private void setContexts ()
    {
      ScreenContext defaultContext = startFieldAttribute.process (null, null);

      if (startFieldAttribute.isExtended ())
      {
        boolean first = true;
        ScreenContext currentContext = defaultContext;

        for (ScreenPosition screenPosition : positions)
        {
          if (first)
          {
            first = false;
            for (Attribute attribute : screenPosition.getAttributes ())
              defaultContext = attribute.process (defaultContext, defaultContext);

            currentContext = defaultContext;
          }
          else
          {
            for (Attribute attribute : screenPosition.getAttributes ())
              currentContext = attribute.process (defaultContext, currentContext);
          }
          screenPosition.setScreenContext (currentContext);
        }
      }
      else
      {
        for (ScreenPosition screenPosition : positions)
          screenPosition.setScreenContext (defaultContext);
      }
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      int start = positions.get (0).position;
      int end = positions.get (positions.size () - 1).position;
      text.append (String.format ("%4d  %4d  %4d", start, end, end - start + 1));

      return text.toString ();
    }
  }
}