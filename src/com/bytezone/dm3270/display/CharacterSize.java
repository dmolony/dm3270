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

      //      width = fm.computeStringWidth ("W");
      //      height = fm.getLineHeight ();
      //      ascent = fm.getAscent ();
      //
      //      float ratio = height / width;
      //      System.out.printf ("%-10.10s h %f, w %f, r %f%n", font.getFamily (), height, width,
      //                         ratio);
      width = (int) (fm.computeStringWidth ("W") + 0.99);
      height = (int) (fm.getLineHeight () + 0.99);
      ascent = (int) (fm.getAscent () + 0.99);

      float ratio = height / width;
      System.out.printf ("%-10.10s h %f, w %f, r %f%n", font.getFamily (), height, width,
                         ratio);
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