package com.bytezone.dm3270.display;

import javafx.scene.text.Font;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

public class CharacterSize
{
  public Font font;
  public float height;
  public float width;
  public float ascent;

  public CharacterSize (Font font)
  {
    this.font = font;
    changeFont (font);
  }

  public void changeFont (Font font)
  {
    FontMetrics fm = Toolkit.getToolkit ().getFontLoader ().getFontMetrics (font);

    width = fm.computeStringWidth ("w");
    height = fm.getLineHeight ();
    ascent = fm.getAscent ();
  }
}