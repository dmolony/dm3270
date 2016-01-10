package com.bytezone.dm3270.display;

import java.util.prefs.Preferences;

import javafx.scene.control.Menu;
import javafx.scene.text.Font;

public interface FontManager
{
  public static FontManager getInstance (Screen screen, Preferences prefs)
  {
    return new FontManagerType1 (screen, prefs);
  }

  public Menu getFontMenu ();

  public void smaller ();

  public void bigger ();

  public String getFontName ();

  public int getFontSize ();

  public Font getStatusBarFont ();

  public FontDetails getFontDetails ();
}