package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.application.ScreenHandler;

public class ProgramTabOrder extends Order
{
  public ProgramTabOrder (byte[] buffer, int offset)
  {
    assert buffer[offset] == Order.PROGRAM_TAB;

    this.buffer = new byte[1];
    this.buffer[0] = buffer[offset];
  }

  @Override
  public void process (ScreenHandler screenHandler)
  {
    // This makes no sense - why would a tab command be part of a Write? The fields
    // haven't even been built yet.
  }

  @Override
  public String toString ()
  {
    return String.format ("PT  :");
  }
}