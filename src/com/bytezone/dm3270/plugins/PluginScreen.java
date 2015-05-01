package com.bytezone.dm3270.plugins;

import java.util.ArrayList;
import java.util.List;

public class PluginScreen
{
  public final int sequence;
  public List<ScreenField> screenFields = new ArrayList<> ();

  public PluginScreen (int sequence)
  {
    this.sequence = sequence;
  }

  public void add (ScreenField field)
  {
    screenFields.add (field);
  }
}