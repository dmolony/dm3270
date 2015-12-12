package com.bytezone.dm3270.display;

public class ScreenDimensions
{
  public final int rows;
  public final int columns;
  public final int size;

  public final int xOffset;
  public final int yOffset;

  public ScreenDimensions (int rows, int columns)
  {
    this (rows, columns, 0, 0);
  }

  public ScreenDimensions (int rows, int columns, int xOffset, int yOffset)
  {
    this.rows = rows;
    this.columns = columns;

    this.xOffset = xOffset;
    this.yOffset = yOffset;

    size = rows * columns;
  }
}