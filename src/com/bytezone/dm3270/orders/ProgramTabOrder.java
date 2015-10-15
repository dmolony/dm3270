package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.display.DisplayScreen;
import com.bytezone.dm3270.display.Pen;

public class ProgramTabOrder extends Order
{
  Order previousOrder;

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

    // if the previous data was text then erase the remainder of the field
    if (previousOrder instanceof TextOrder)
      pen.eraseEOF ();

    pen.tab ();
  }

  @Override
  public boolean matchesPreviousOrder (Order previousOrder)
  {
    this.previousOrder = previousOrder;
    return false;     // we don't care if it matched, but we want to know what it was
  }

  @Override
  public String toString ()
  {
    return String.format ("PT  :");
  }
}