package com.bytezone.dm3270.display;

import javafx.scene.text.Font;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

public class CharacterSize
{
  private Font font;
  private FontMetrics fontMetrics;
  private int height;
  private int width;
  private int ascent;
  private int descent;
  private int leading;

  public CharacterSize (Font font)
  {
    changeFont (font);
  }

  public void changeFont (Font font)
  {
    if (font != this.font)
    {
      this.font = font;
      this.fontMetrics = Toolkit.getToolkit ().getFontLoader ().getFontMetrics (font);

      width = (int) (fontMetrics.computeStringWidth ("W") + 0.99);
      height = (int) (fontMetrics.getLineHeight () + 0.99);
      ascent = (int) (fontMetrics.getMaxAscent () + 0.99);
      descent = (int) (fontMetrics.getMaxDescent () + 0.99);
      leading = height - ascent - descent;

      System.out.println (this);
      if (leading < 1)
      {
        leading = 1;
        height = ascent + descent + leading;
        System.out.println (this);
      }
    }
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
    return String.format ("[Font=%s, height=%d, width=%d, ascent=%d, descent=%d, "
                              + "leading=%d, ratio=%2.2f]", font.getName (), height,
                          width, ascent, descent,
                          leading, (float) height / width);
  }
}