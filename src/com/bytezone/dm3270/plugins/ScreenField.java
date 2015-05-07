package com.bytezone.dm3270.plugins;

public class ScreenField
{
  private PluginData pluginData;

  public final int location;
  public final int row;
  public final int column;
  public final int length;
  public final boolean isProtected;
  public final boolean isAlpha;
  public final String data;

  public String newData;

  public ScreenField (int location, int row, int column, int length, boolean isProtected,
      boolean isAlpha, String data)
  {
    this.location = location;
    this.row = row;
    this.column = column;
    this.length = length;
    this.isProtected = isProtected;
    this.isAlpha = isAlpha;
    this.data = data;
  }

  public boolean isModifiableLength (int length)
  {
    return !isProtected && this.length == length;
  }

  public void change (String newValue, PluginData data)
  {
    newData = newValue;
    data.addChangedField (this);
  }

  public void change (String newValue)
  {
    assert pluginData != null;
    change (newValue, pluginData);
  }

  public void setData (PluginData pluginData)
  {
    this.pluginData = pluginData;
  }

  @Override
  public String toString ()
  {
    return String.format ("%2d  %2d  %4d  %-50.50s", row, column, length, data);
  }
}