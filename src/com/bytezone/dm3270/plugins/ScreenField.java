package com.bytezone.dm3270.plugins;

public class ScreenField
{
  public final int row;
  public final int column;
  public final int length;
  public final String data;

  public ScreenField (int row, int column, int length, String data)
  {
    this.row = row;
    this.column = column;
    this.length = length;
    this.data = data;
  }

  @Override
  public String toString ()
  {
    return String.format ("%2d  %2d  %4d  %s", row, column, length, data);
  }
}