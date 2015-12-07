package com.bytezone.dm3270.display;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

import javafx.scene.text.Font;

public class FontData
{
  private Font font;
  private String name;
  private int size;

  private FontMetrics fontMetrics;
  private int height;
  private int width;
  private int ascent;
  private int descent;
  private int leading;

  public Font getFont ()
  {
    return font;
  }

  // called from ConsolePane.setFontData()
  public Font getFont (int sizeChange)
  {
    return Font.font (name, size + sizeChange);
  }

  public String getName ()
  {
    return name;
  }

  public int getSize ()
  {
    return size;
  }

  public void changeFont (String name, int size)
  {
    Font font = Font.font (name, size);
    if (font != this.font)
    {
      this.font = font;
      this.name = name;
      this.size = size;

      this.fontMetrics = Toolkit.getToolkit ().getFontLoader ().getFontMetrics (font);

      width = (int) (fontMetrics.computeStringWidth ("W") + 0.99);
      height = (int) (fontMetrics.getLineHeight () + 0.99);
      ascent = (int) (fontMetrics.getMaxAscent () + 0.99);
      descent = (int) (fontMetrics.getMaxDescent () + 0.99);
      leading = height - ascent - descent;

      if (leading < 1)
      {
        leading = 1;
        height = ascent + descent + leading;
      }
    }
  }

  public boolean matches (String name, int size)
  {
    return this.name.equals (name) && this.size == size;
  }

  public int getWidth ()
  {
    return width;
  }

  public int getHeight ()
  {
    return height;
  }

  public int getAscent ()
  {
    return ascent;
  }

  public int getDescent ()
  {
    return descent;
  }

  public int getLeading ()
  {
    return leading;
  }

  @Override
  public String toString ()
  {
    return String.format (
                          "[Font=%s, height=%d, width=%d, ascent=%d, descent=%d, "
                              + "leading=%d, ratio=%2.2f]",
                          font.getName (), height, width, ascent, descent, leading,
                          (float) height / width);
  }
}