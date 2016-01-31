package com.bytezone.dm3270.display;

import com.bytezone.dm3270.orders.BufferAddress;

public class ScreenDimensions
{
  public final int rows;
  public final int columns;
  public final int size;

  public final int xOffset;
  public final int yOffset;

  public ScreenDimensions (int rows, int columns)
  {
    this (rows, columns, 4, 4);       // enforce the 4-pixel border
  }

  private ScreenDimensions (int rows, int columns, int xOffset, int yOffset)
  {
    this.rows = rows;
    this.columns = columns;

    this.xOffset = xOffset;
    this.yOffset = yOffset;

    size = rows * columns;

    BufferAddress.setScreenWidth (columns);       // for debugging output
  }

  @Override
  public String toString ()
  {
    return String.format ("[Rows:%d, Columns:%d, Size:%d]", rows, columns, size);
  }
}