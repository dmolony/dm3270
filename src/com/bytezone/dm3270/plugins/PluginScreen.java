package com.bytezone.dm3270.plugins;

import java.util.ArrayList;
import java.util.List;

public class PluginScreen
{
  public final int sequence;
  public final long md5;
  public List<ScreenField> screenFields = new ArrayList<> ();

  public PluginScreen (int sequence, long md5)
  {
    this.sequence = sequence;
    this.md5 = md5;
  }

  public void add (ScreenField field)
  {
    screenFields.add (field);
  }
}