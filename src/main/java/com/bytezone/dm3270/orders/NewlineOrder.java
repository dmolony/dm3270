package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.display.DisplayScreen;
import com.bytezone.dm3270.display.Pen;

public class NewlineOrder extends Order {

  public NewlineOrder(byte[] buffer, int offset) {
    this.buffer = new byte[1];
    this.buffer[0] = buffer[offset];
  }

  @Override
  public void process(DisplayScreen screen) {
    Pen pen = screen.getPen();
    pen.moveToNextLine();
    for (int i = 0; i < duplicates; i++) {
      pen.moveToNextLine();
    }
  }

  @Override
  public boolean matchesPreviousOrder(Order order) {
    return order instanceof NewlineOrder;
  }

  @Override
  public String toString() {
    String duplicateText = duplicates == 0 ? "" : "x " + (duplicates + 1);
    return String.format("FCO : %-12s : %02X %s", "FCO_NEWLINE", buffer[0], duplicateText);
  }

}
