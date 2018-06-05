package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.display.DisplayScreen;
import com.bytezone.dm3270.display.Pen;
import com.bytezone.dm3270.utilities.Dm3270Utility;

public class TextOrder extends Order {

  public TextOrder(byte[] buffer, int ptr, int max) {
    int dataLength = getDataLength(buffer, ptr, max);
    this.buffer = new byte[dataLength];
    System.arraycopy(buffer, ptr, this.buffer, 0, dataLength);
  }

  private int getDataLength(byte[] buffer, int offset, int max) {
    int ptr = offset + 1;
    int length = 1;
    while (ptr < max) {
      byte value = buffer[ptr++];
      for (byte orderValue : orderValues) {
        if (value == orderValue) {
          return length;
        }
      }
      length++;
    }

    return length;
  }

  @Override
  public boolean isText() {
    return true;
  }

  @Override
  public void process(DisplayScreen screen) {
    Pen pen = screen.getPen();
    for (byte b : buffer) {
      pen.write(b);
    }
  }

  @Override
  public String toString() {
    return buffer.length == 0 ? "" : "Text: [" + Dm3270Utility.getString(buffer) + "]";
  }

}
