package com.bytezone.dm3270.orders;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.application.Cursor;
import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.application.ScreenPosition;
import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.Attribute.AttributeType;
import com.bytezone.dm3270.attributes.StartFieldAttribute;

public class StartFieldExtendedOrder extends Order
{
  private StartFieldAttribute startFieldAttribute;
  private final List<Attribute> attributes = new ArrayList<Attribute> ();

  public StartFieldExtendedOrder (byte[] buffer, int offset)
  {
    assert buffer[offset] == Order.START_FIELD_EXTENDED;

    int totalAttributePairs = buffer[offset + 1] & 0xFF;
    this.buffer = new byte[totalAttributePairs * 2 + 2];
    this.buffer[0] = buffer[offset];
    this.buffer[1] = buffer[offset + 1];

    int bptr = 2;
    int ptr = offset + 2;

    while (totalAttributePairs-- > 0)
    {
      Attribute attribute = Attribute.getAttribute (buffer[ptr], buffer[ptr + 1]);

      // There has to be a StartFieldAttribute, but it could be anywhere in the list
      if (attribute.getAttributeType () == AttributeType.START_FIELD)
        startFieldAttribute = (StartFieldAttribute) attribute;
      else
        attributes.add (attribute);

      this.buffer[bptr++] = buffer[ptr++];
      this.buffer[bptr++] = buffer[ptr++];
    }
  }

  public StartFieldExtendedOrder (StartFieldAttribute startFieldAttribute,
      List<Attribute> attributes)
  {
    this.startFieldAttribute = startFieldAttribute;
    this.attributes.addAll (attributes);

    buffer = new byte[4 + attributes.size () * 2];
    buffer[0] = Order.START_FIELD_EXTENDED;
    buffer[1] = (byte) (attributes.size () + 1);    // sfa + other attributes
    int ptr = 2;
    ptr = startFieldAttribute.pack (buffer, ptr);
    for (Attribute attribute : attributes)
      ptr = attribute.pack (buffer, ptr);
  }

  public StartFieldExtendedOrder (StartFieldAttribute startFieldAttribute)
  {
    this.startFieldAttribute = startFieldAttribute;
    buffer = new byte[4];
    buffer[0] = Order.START_FIELD_EXTENDED;
    buffer[1] = 0x01;    // just one attribute
    startFieldAttribute.pack (buffer, 2);
  }

  @Override
  public void process (ScreenHandler screenHandler)
  {
    Cursor cursor = screenHandler.getCursor ();
    cursor.clearAttributes ();        // remove any unapplied character attributes
    ScreenPosition sp = cursor.getScreenPosition ();

    sp.reset ();
    sp.clearAttributes ();
    sp.addAttribute (startFieldAttribute);
    for (Attribute attribute : attributes)
      sp.addAttribute (attribute);

    cursor.moveRight ();
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ("SFE : ");
    text.append (startFieldAttribute);

    for (Attribute attr : attributes)
      text.append (String.format ("\n      %-34s", attr));

    return text.toString ();
  }
}