package com.bytezone.dm3270.application;

import java.util.prefs.Preferences;

import javafx.stage.Stage;

public class WindowSaver
{
  private final Preferences prefs;
  private final Stage stage;
  private final String key;

  public WindowSaver (Preferences prefs, Stage stage, String key)
  {
    this.prefs = prefs;
    this.stage = stage;
    this.key = key;
  }

  public void saveWindow ()
  {
    prefs.putDouble (key + "X", stage.getX ());
    prefs.putDouble (key + "Y", stage.getY ());
    prefs.putDouble (key + "Height", stage.getHeight ());
    prefs.putDouble (key + "Width", stage.getWidth ());
  }

  public boolean restoreWindow ()
  {
    Double x = prefs.getDouble (key + "X", -1.0);
    Double y = prefs.getDouble (key + "Y", -1.0);
    Double height = prefs.getDouble (key + "Height", -1.0);
    Double width = prefs.getDouble (key + "Width", -1.0);

    if (width < 0) // nothing to restore
    {
      stage.centerOnScreen ();
      return false;
    }
    else
    {
      stage.setX (x);
      stage.setY (y);
      stage.setHeight (height);
      stage.setWidth (width);
      return true;
    }
  }
}