package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.application.Cursor;
import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.attributes.Attribute;

public class SetAttributeOrder extends Order
{
  private final Attribute attribute;

  public SetAttributeOrder (byte[] buffer, int offset)
  {
    assert buffer[offset] == Order.SET_ATTRIBUTE;

    attribute = Attribute.getAttribute (buffer[offset + 1], buffer[offset + 2]);

    this.buffer = new byte[3];
    System.arraycopy (buffer, offset, this.buffer, 0, 3);
  }

  public Attribute getAttribute ()
  {
    return attribute;
  }

  @Override
  public void process (ScreenHandler screenHandler)
  {
    Cursor cursor = screenHandler.getCursor ();
    cursor.addAttribute (attribute);
  }

  @Override
  public String toString ()
  {
    return String.format ("SA  : %s", attribute);
  }
}