package com.bytezone.dm3270.plugins;

import java.util.ArrayList;
import java.util.List;

public class PluginScreen
{
  public final int sequence;
  public final long md5;
  public final List<ScreenField> screenFields = new ArrayList<> ();
  public final int cursorRow;
  public final int cursorColumn;

  public PluginScreen (int sequence, long md5, int row, int column)
  {
    this.sequence = sequence;
    this.md5 = md5;
    cursorRow = row;
    cursorColumn = column;
  }

  public void add (ScreenField field)
  {
    screenFields.add (field);
  }
}