package com.bytezone.dm3270.display;

public class ScreenDimensions
{
  public final int rows;
  public final int columns;
  public final int size;

  public ScreenDimensions (int rows, int columns)
  {
    this.rows = rows;
    this.columns = columns;
    size = rows * columns;
  }
}