package com.bytezone.dm3270.plugins;

public class ScreenLocation
{
  static final int screenRows = 24;
  static final int screenColumns = 80;
  static final int screenLocations = 1920;

  public final int row;
  public final int column;
  public final int location;

  public ScreenLocation (int location)
  {
    row = location / screenColumns;
    column = location % screenColumns;
    this.location = location;
  }

  public ScreenLocation (int row, int column)
  {
    this.row = row;
    this.column = column;
    location = row * column;
  }
}