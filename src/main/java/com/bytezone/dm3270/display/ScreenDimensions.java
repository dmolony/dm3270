package com.bytezone.dm3270.display;

import com.bytezone.dm3270.orders.BufferAddress;

public class ScreenDimensions {

  public final int rows;
  public final int columns;
  public final int size;

  public ScreenDimensions(int rows, int columns) {
    this.rows = rows;
    this.columns = columns;

    size = rows * columns;

    BufferAddress.setScreenWidth(columns);       // for debugging output
  }

  @Override
  public String toString() {
    return String.format("[Rows:%d, Columns:%d, Size:%d]", rows, columns, size);
  }

}
