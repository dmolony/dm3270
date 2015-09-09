package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.display.DisplayScreen;
import com.bytezone.dm3270.display.Pen;

public class FormatControlOrder extends Order
{
  byte value;

  public FormatControlOrder (byte[] buffer, int offset)
  {
    value = buffer[offset];
    this.buffer = new byte[1];
  }

  @Override
  public void process (DisplayScreen screen)
  {
    Pen pen = screen.getPen ();
    pen.write ((byte) 0x40);
  }

  @Override
  public String toString ()
  {
    return String.format ("FCO :              : %02X", value);
  }
}