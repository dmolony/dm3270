package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.Screen;

public class SetAttributeOrder extends Order
{
  private final Attribute attribute;

  public SetAttributeOrder (byte[] buffer, int offset)
  {
    assert buffer[offset] == Order.SET_ATTRIBUTE;

    attribute = Attribute.getAttribute (buffer[offset + 1], buffer[offset + 2]);

    this.buffer = new byte[3];
    System.arraycopy (buffer, offset, this.buffer, 0, this.buffer.length);
  }

  public Attribute getAttribute ()
  {
    return attribute;
  }

  @Override
  public void process (Screen screen)
  {
    //    Cursor cursor = screenHandler.getCursor ();
    //    cursor.addAttribute (attribute);

    Cursor cursor2 = screen.getScreenCursor ();
    cursor2.add (attribute);
  }

  @Override
  public String toString ()
  {
    return String.format ("SA  : %s", attribute);
  }
}