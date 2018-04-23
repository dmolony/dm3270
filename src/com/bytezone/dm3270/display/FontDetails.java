package com.bytezone.dm3270.display;

import javafx.geometry.Bounds;
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

    Text text = new Text ("W");
    text.setFont (font);
    Bounds bounds = text.getLayoutBounds ();
    height = (int) (bounds.getHeight () + 0.5);
    width = (int) (bounds.getWidth () + 0.5);
    ascent = (int) (-bounds.getMinY () + 0.5);
    descent = height - ascent;
  }

  @Override
  public String toString ()
  {
    return String.format ("[%-18s %d w=%2d, h=%2d, a=%2d, d=%2d]", font.getName (),
        (int) font.getSize (), width, height, ascent, descent);
  }
}