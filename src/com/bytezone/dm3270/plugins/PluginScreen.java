package com.bytezone.dm3270.plugins;

import java.util.ArrayList;
import java.util.List;

public class PluginScreen
{
  public List<ScreenField> screenFields = new ArrayList<> ();

  public void add (ScreenField field)
  {
    screenFields.add (field);
  }
}