package com.bytezone.dm3270.display;

import javafx.scene.text.Font;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

public class CharacterSize
{
  public Font font;
  private float height;
  private float width;
  private float ascent;

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

      if (true)
      {
        width = (float) (((int) fm.computeStringWidth ("W")) + 1.0);
        height = (float) (((int) fm.getLineHeight ()) + 1.0);
        ascent = (float) (((int) fm.getAscent ()) + 1.0);
        float ratio = height / width;
        System.out.printf ("Height %f, Width %f, ratio %f%n", height, width, ratio);
        if (ratio < 1.8)
        {
          ++height;
          System.out.println ("bumping height");
        }
        else if (ratio > 2.4)
        {
          ++width;
          System.out.println ("bumping width");
        }
      }
      else
      {
        width = fm.computeStringWidth ("w");
        height = fm.getLineHeight ();
        ascent = fm.getAscent ();
      }
    }
  }

  public float getWidth ()
  {
    return width;
  }

  public float getHeight ()
  {
    return height;
  }

  public float getAscent ()
  {
    return ascent;
  }

  @Override
  public String toString ()
  {
    return String.format ("[Font=%s, height=%2.2f, width=%2.2f, ascent=%2.2f]",
                          font.getName (), height, width, ascent);
  }
}