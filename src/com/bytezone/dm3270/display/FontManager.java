package com.bytezone.dm3270.display;

import javafx.scene.control.Menu;
import javafx.scene.text.Font;

public interface FontManager
{
  public Menu getFontMenu ();

  public void smaller ();

  public void bigger ();

  public String getFontName ();

  public int getFontSize ();

  public Font getDefaultFont ();

  public FontDetails getFontDetails ();
}