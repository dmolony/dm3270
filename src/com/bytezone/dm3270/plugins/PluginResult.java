package com.bytezone.dm3270.plugins;

import java.util.ArrayList;
import java.util.List;

public class PluginResult
{
  List<ScreenAction> actions = new ArrayList<> ();

  public void add (ScreenAction action)
  {
    actions.add (action);
  }
}