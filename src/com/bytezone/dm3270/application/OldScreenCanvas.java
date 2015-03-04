package com.bytezone.dm3270.application;

import javafx.scene.canvas.Canvas;
import javafx.scene.text.Font;

public class OldScreenCanvas extends Canvas
{
  //  private static final byte TOP_LEFT = (byte) 0xC5;
  //  private static final byte TOP_RIGHT = (byte) 0xD5;
  //  private static final byte BOTTOM_LEFT = (byte) 0xC4;
  //  private static final byte BOTTOM_RIGHT = (byte) 0xD4;
  //  private static final byte HORIZONTAL_LINE = (byte) 0xA2;
  //  private static final byte VERTICAL_LINE = (byte) 0x85;
  //
  //  private final int screenColumns;
  //  private final int screenRows;
  //  private final GraphicsContext gc;
  //
  //  private float charWidth;
  //  private float charHeight;
  //  private float ascent;
  //
  //  private final float xOffset = 4;
  //  private final float yOffset = 4;
  //
  //  // spacing between characters for outlining
  //  private final boolean expanded = true;
  //  private final double expandedWidth = .5;
  //  private final double expandedHeight = 1.6;
  //  private final boolean topLine = false;
  //  private final boolean bottomLine = false;

  private OldScreenCanvas (int rows, int columns, Font font)
  {
    //    screenRows = rows;
    //    screenColumns = columns;
    //    gc = getGraphicsContext2D ();
    //    //    changeFont (font);
    //
    //    if (false)
    //    {
    //      System.out.println (font);
    //      String logicalFonts[] =
    //          { "Serif", "SansSerif", "Monospaced", "Dialog", "DialogInput" };
    //      GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment ();
    //      for (String s : env.getAvailableFontFamilyNames ())
    //        System.out.println (s);
    //    }
  }

  //  public void changeFont (Font font)
  //  {
  //    FontMetrics fm = Toolkit.getToolkit ().getFontLoader ().getFontMetrics (font);
  //
  //    charWidth = fm.computeStringWidth ("w");
  //    charHeight = fm.getLineHeight ();
  //    ascent = fm.getAscent ();
  //
  //    setWidth (charWidth * screenColumns + xOffset * 2
  //        + (expanded ? (screenColumns - 1) * expandedWidth : 0));
  //    setHeight (charHeight * screenRows + yOffset * 2
  //        + (expanded ? (screenRows + 1) * expandedHeight : 0));
  //
  //    gc.setFont (font);
  //  }
  //
  //  public void draw (ScreenPosition screenPosition)
  //  {
  //    ScreenContext context = screenPosition.getScreenContext ();
  //    char character = screenPosition.getChar ();
  //    int row = screenPosition.getRow ();
  //    int col = screenPosition.getColumn ();
  //
  //    double x = xOffset + col * charWidth + (expanded ? col * expandedWidth : 0);
  //    double y = yOffset + row * charHeight + (expanded ? (row + 1) * expandedHeight : 0);
  //
  //    if (screenPosition.hasCursor ())
  //    {
  //      gc.setFill (context.foregroundColor);
  //      gc.fillRect (x + .15, y + .15, charWidth - .3, charHeight - .3);
  //    }
  //    else
  //    {
  //      gc.setFill (context.reverseVideo ? Color.YELLOW : context.backgroundColor);
  //      gc.fillRect (x, y, charWidth + .4, charHeight + .4);
  //    }
  //
  //    if (screenPosition.isVisible ())
  //    {
  //      if (screenPosition.isGraphicsCharacter ())
  //        doGraphics (screenPosition, x, y);
  //      else
  //      {
  //        gc.setFill (screenPosition.hasCursor () ? context.backgroundColor
  //            : context.foregroundColor);
  //        gc.fillText (character + "", x, y + ascent);
  //      }
  //
  //      if (context.underscore)
  //      {
  //        gc.setStroke (context.foregroundColor);
  //        double y2 = y + charHeight - 1;
  //        gc.strokeLine (x, y2, x + charWidth, y2);
  //      }
  //
  //      if (topLine && expanded)
  //      {
  //        gc.setStroke (Color.WHITE);
  //        double y2 = y - 0.8;
  //        gc.strokeLine (x, y2, x + charWidth, y2);
  //      }
  //
  //      if (bottomLine && expanded)
  //      {
  //        gc.setStroke (Color.WHITE);
  //        double y2 = y + charHeight + 0.8;
  //        gc.strokeLine (x, y2, x + charWidth, y2);
  //      }
  //    }
  //  }
  //
  //  private void doGraphics (ScreenPosition screenPosition, double x, double y)
  //  {
  //    ScreenContext context = screenPosition.getScreenContext ();
  //    gc.setStroke (screenPosition.hasCursor () ? context.backgroundColor
  //        : context.foregroundColor);
  //
  //    double dx = 4.3;
  //    //    gc.setLineWidth (1.5);
  //
  //    switch (screenPosition.getByte ())
  //    {
  //      case HORIZONTAL_LINE:
  //        gc.strokeLine (x, y + 10, x + charWidth, y + 10);
  //        break;
  //
  //      case VERTICAL_LINE:
  //        gc.strokeLine (x + dx, y, x + dx, y + charHeight);
  //        break;
  //
  //      case TOP_LEFT:
  //        gc.strokeLine (x + dx, y + 11, x + dx, y + 20);
  //        gc.strokeLine (x + dx, y + 10, x + 10, y + 10);
  //        break;
  //
  //      case TOP_RIGHT:
  //        gc.strokeLine (x + dx, y + 11, x + dx, y + 20);
  //        gc.strokeLine (x, y + 10, x + dx, y + 10);
  //        break;
  //
  //      case BOTTOM_LEFT:
  //        gc.strokeLine (x + dx, y, x + dx, y + 10);
  //        gc.strokeLine (x + dx, y + 10, x + 10, y + 10);
  //        break;
  //
  //      case BOTTOM_RIGHT:
  //        gc.strokeLine (x + dx, y, x + dx, y + 10);
  //        gc.strokeLine (x, y + 10, x + dx, y + 10);
  //        break;
  //
  //      default:
  //        gc.fillText (screenPosition.getChar () + "", x, y + ascent);
  //        System.out.printf ("Unknown graphics character: %02X%n",
  //                           screenPosition.getByte ());
  //    }
  //  }
  //
  //  public void erase (ScreenPosition screenPosition)
  //  {
  //    ScreenContext context = screenPosition.getScreenContext ();
  //
  //    double x = xOffset + screenPosition.getColumn () * charWidth;
  //    double y = yOffset + screenPosition.getRow () * charHeight;
  //
  //    gc.setFill (context.backgroundColor);
  //    gc.fillRect (x, y, charWidth, charHeight);
  //  }
  //
  //  public void clearScreen ()
  //  {
  //    gc.setFill (Color.BLACK);
  //    gc.fillRect (0, 0, getWidth (), getHeight ());
  //  }
  //
  //  @Override
  //  public boolean isResizable ()
  //  {
  //    return false;
  //  }
}