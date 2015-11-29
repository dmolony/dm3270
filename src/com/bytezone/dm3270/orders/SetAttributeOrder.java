package com.bytezone.dm3270.orders;

import java.util.Optional;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.display.DisplayScreen;
import com.bytezone.dm3270.display.Pen;

public class SetAttributeOrder extends Order
{
  private final Attribute attribute;

  public SetAttributeOrder (byte[] buffer, int offset)
  {
    assert buffer[offset] == Order.SET_ATTRIBUTE;

    Optional<Attribute> opt =
        Attribute.getAttribute (buffer[offset + 1], buffer[offset + 2]);
    assert opt.isPresent ();
    attribute = opt.get ();

    this.buffer = new byte[3];
    System.arraycopy (buffer, offset, this.buffer, 0, this.buffer.length);
  }

  public Attribute getAttribute ()
  {
    return attribute;
  }

  @Override
  public void process (DisplayScreen screen)
  {
    Pen pen = screen.getPen ();
    pen.addAttribute (attribute);
  }

  @Override
  public String toString ()
  {
    return String.format ("SA  : %s", attribute);
  }
}