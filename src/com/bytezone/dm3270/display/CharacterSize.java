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
        width = (float) ((int) fm.computeStringWidth ("w") + 0.5);
        height = (float) ((int) fm.getLineHeight () + 0.5);
        ascent = (float) ((int) fm.getAscent () + 0.5);
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