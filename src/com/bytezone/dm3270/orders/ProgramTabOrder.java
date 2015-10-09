package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.display.DisplayScreen;

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
    System.out.println ("In ProgramTabOrder.process()");
  }

  @Override
  public String toString ()
  {
    return String.format ("PT  :");
  }
}