package com.bytezone.dm3270.orders;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.display.Screen;

public class ModifyFieldOrder extends Order
{
  private final List<Attribute> attributes = new ArrayList<> ();

  public ModifyFieldOrder (byte[] buffer, int offset)
  {
    assert buffer[offset] == Order.MODIFY_FIELD;

    int totalAttributePairs = buffer[offset + 1] & 0xFF;

    this.buffer = new byte[totalAttributePairs * 2 + 2];
    this.buffer[0] = buffer[offset];
    this.buffer[1] = buffer[offset + 1];

    int ptr = offset + 2;
    int bptr = 2;
    for (int i = 0; i < totalAttributePairs; i++)
    {
      Attribute attribute = Attribute.getAttribute (buffer[ptr], buffer[ptr + 1]);
      attributes.add (attribute);

      this.buffer[bptr++] = buffer[ptr++];
      this.buffer[bptr++] = buffer[ptr++];
    }
  }

  @Override
  public void process (ScreenHandler screenHandler, Screen screen)
  {
    System.out.println ("What happens now?");
  }
}