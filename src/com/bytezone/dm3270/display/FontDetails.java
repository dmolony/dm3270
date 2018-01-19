package com.bytezone.dm3270.display;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

import javafx.geometry.Bounds;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class FontDetails
{
  public final int width;
  public final int height;
  public final int ascent;
  private final int descent;
  public final Font font;

  public final String name;
  public final int size;

  public FontDetails (String name, int size, Font font)
  {
    this.font = font;
    this.name = name;
    this.size = size;

    FontMetrics fontMetrics =
        Toolkit.getToolkit ().getFontLoader ().getFontMetrics (font);
    width = (int) (fontMetrics.computeStringWidth ("W") + 0.9);

    ascent = (int) (fontMetrics.getAscent () + fontMetrics.getLeading () + 0.9);
    descent = (int) (fontMetrics.getDescent () + 0.9);
    height = ascent + descent;
  }

  // java 9 does not allow FontMetrics
  public FontDetails (String name, int size, Font font, int notUsed)
  {
    this.font = font;
    this.name = name;
    this.size = size;

    Text text = new Text ("W");
    text.setFont (font);
    Bounds tb = text.getBoundsInLocal ();
    Rectangle stencil =
        new Rectangle (tb.getMinX (), tb.getMinY (), tb.getWidth (), tb.getHeight ());

    Shape intersection = Shape.intersect (text, stencil);

    Bounds ib = intersection.getBoundsInLocal ();
    width = (int) (ib.getWidth () + 0.9);
    ascent = (int) (ib.getHeight () * 1.4);
    descent = ascent / 3;
    height = ascent + descent;
  }

  @Override
  public String toString ()
  {
    return String.format ("[%-18s %d w=%2d, h=%2d, a=%2d, d=%2d]", font.getName (),
        (int) font.getSize (), width, height, ascent, descent);
  }
}