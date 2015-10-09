package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.display.DisplayScreen;
import com.bytezone.dm3270.display.Pen;

public class ProgramTabOrder extends Order
{
  public ProgramTabOrder (byte[] buffer, int offset)
  {
    assert buffer[offset] == Order.PROGRAM_TAB;

    this.buffer = new byte[1];
    this.buffer[0] = buffer[offset];
  }

  @Override
  public void process (DisplayScreen screen)
  {
    Pen pen = screen.getPen ();
    pen.tab ();
  }

  @Override
  public String toString ()
  {
    return String.format ("PT  :");
  }
}