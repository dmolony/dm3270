package com.bytezone.dm3270.display;

import javafx.scene.text.Font;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

public class CharacterSize
{
  public Font font;
  private int height;
  private int width;
  private int ascent;

  public CharacterSize (Font font)
  {
    changeFont (font);
  }

  public void changeFont (Font font)
  {
    if (font != this.font)
    {
      this.font = font;
      FontMetrics fm = Toolkit.getToolkit ().getFontLoader ().getFontMetrics (font);

      width = (int) (fm.computeStringWidth ("W") + 0.99);
      height = (int) (fm.getLineHeight () + 0.99);
      ascent = (int) (fm.getAscent () + 0.99);

      float ratio = (float) height / width;
      if (ratio < 1.8)
      {
        ++height;
        System.out.println ("adjusting height");
      }
      else if (ratio > 2.4)
      {
        ++width;
        System.out.println ("adjusting width");
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

  @Override
  public String toString ()
  {
    return String.format ("[Font=%s, height=%d, width=%d, ascent=%d, ratio=%2.2f]",
                          font.getName (), height, width, ascent, (float) height / width);
  }
}