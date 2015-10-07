package com.bytezone.dm3270.plugins;

public class ScreenLocation
{
  static final int screenRows = 24;
  static final int screenColumns = 80;
  static final int screenLocations = screenRows * screenColumns;

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
    location = row * screenColumns + column;
  }

  public boolean matches (ScreenLocation other)
  {
    return this.location == other.location;
  }

  @Override
  public String toString ()
  {
    return String.format ("[Location=%4d, Row=%2d, Column=%2d]", location, row, column);
  }
}