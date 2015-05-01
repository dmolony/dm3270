package com.bytezone.dm3270.plugins;

import java.util.ArrayList;
import java.util.List;

public class PluginResult
{
  List<ScreenField> fieldsChanged = new ArrayList<> ();

  public void add (ScreenField field)
  {
    fieldsChanged.add (field);
  }
}