package com.bytezone.dm3270.application;

import java.awt.GraphicsEnvironment;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

public class ScreenCanvas extends Canvas
{
  private static final byte TOP_LEFT = (byte) 0xC5;
  private static final byte TOP_RIGHT = (byte) 0xD5;
  private static final byte BOTTOM_LEFT = (byte) 0xC4;
  private static final byte BOTTOM_RIGHT = (byte) 0xD4;
  private static final byte HORIZONTAL_LINE = (byte) 0xA2;
  private static final byte VERTICAL_LINE = (byte) 0x85;

  private final int screenColumns;
  private final int screenRows;
  private final GraphicsContext gc;

  private float charWidth;
  private float charHeight;
  private float ascent;

  private final float xOffset = 4;
  private final float yOffset = 2;

  public ScreenCanvas (int rows, int columns, Font font)
  {
    screenRows = rows;
    screenColumns = columns;
    gc = getGraphicsContext2D ();
    changeFont (font);

    if (false)
    {
      System.out.println (font);
      String logicalFonts[] =
          { "Serif", "SansSerif", "Monospaced", "Dialog", "DialogInput" };
      GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment ();
      for (String s : env.getAvailableFontFamilyNames ())
        System.out.println (s);
    }
  }

  public void changeFont (Font font)
  {
    FontMetrics fm = Toolkit.getToolkit ().getFontLoader ().getFontMetrics (font);

    charWidth = fm.computeStringWidth ("w");
    charHeight = fm.getLineHeight ();
    ascent = fm.getAscent ();

    setWidth (charWidth * screenColumns + xOffset * 2);
    setHeight (charHeight * screenRows + yOffset * 2);

    gc.setFont (font);
  }

  public void draw (ScreenPosition screenPosition)
  {
    ScreenContext context = screenPosition.getScreenContext ();
    char character = screenPosition.getChar ();

    double x = xOffset + screenPosition.getColumn () * charWidth;
    double y = yOffset + screenPosition.getRow () * charHeight;

    if (screenPosition.hasCursor ())
    {
      gc.setFill (context.foregroundColor);
      gc.fillRect (x + .15, y + .15, charWidth - .3, charHeight - .3);
    }
    else
    {
      gc.setFill (context.reverseVideo ? Color.YELLOW : context.backgroundColor);
      gc.fillRect (x, y, charWidth + .4, charHeight + .4);
    }

    if (screenPosition.isVisible ())
    {
      if (screenPosition.isGraphicsCharacter ())
        doGraphics (screenPosition, x, y);
      else
      {
        gc.setFill (screenPosition.hasCursor () ? context.backgroundColor
            : context.foregroundColor);
        gc.fillText (character + "", x, y + ascent);
      }

      if (context.underscore)
      {
        gc.setStroke (context.foregroundColor);
        y += charHeight - 1;
        gc.strokeLine (x, y, x + charWidth, y);
      }
    }
  }

  private void doGraphics (ScreenPosition screenPosition, double x, double y)
  {
    ScreenContext context = screenPosition.getScreenContext ();
    gc.setStroke (screenPosition.hasCursor () ? context.backgroundColor
        : context.foregroundColor);

    double dx = 4.3;
    //    gc.setLineWidth (1.5);

    switch (screenPosition.getByte ())
    {
      case HORIZONTAL_LINE:
        gc.strokeLine (x, y + 10, x + charWidth, y + 10);
        break;

      case VERTICAL_LINE:
        gc.strokeLine (x + dx, y, x + dx, y + charHeight);
        break;

      case TOP_LEFT:
        gc.strokeLine (x + dx, y + 11, x + dx, y + 20);
        gc.strokeLine (x + dx, y + 10, x + 10, y + 10);
        break;

      case TOP_RIGHT:
        gc.strokeLine (x + dx, y + 11, x + dx, y + 20);
        gc.strokeLine (x, y + 10, x + dx, y + 10);
        break;

      case BOTTOM_LEFT:
        gc.strokeLine (x + dx, y, x + dx, y + 10);
        gc.strokeLine (x + dx, y + 10, x + 10, y + 10);
        break;

      case BOTTOM_RIGHT:
        gc.strokeLine (x + dx, y, x + dx, y + 10);
        gc.strokeLine (x, y + 10, x + dx, y + 10);
        break;

      default:
        gc.fillText (screenPosition.getChar () + "", x, y + ascent);
        System.out.printf ("Unknown graphics character: %02X%n",
                           screenPosition.getByte ());
    }
  }

  public void erase (ScreenPosition screenPosition)
  {
    ScreenContext context = screenPosition.getScreenContext ();

    double x = xOffset + screenPosition.getColumn () * charWidth;
    double y = yOffset + screenPosition.getRow () * charHeight;

    gc.setFill (context.backgroundColor);
    gc.fillRect (x, y, charWidth, charHeight);
  }

  public void clearScreen ()
  {
    gc.setFill (Color.BLACK);
    gc.fillRect (0, 0, getWidth (), getHeight ());
  }

  @Override
  public boolean isResizable ()
  {
    return false;
  }
}