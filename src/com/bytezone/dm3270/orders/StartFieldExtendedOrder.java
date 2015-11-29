package com.bytezone.dm3270.orders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.Attribute.AttributeType;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.display.DisplayScreen;
import com.bytezone.dm3270.display.Pen;

public class StartFieldExtendedOrder extends Order
{
  private StartFieldAttribute startFieldAttribute;
  private final List<Attribute> attributes = new ArrayList<Attribute> ();
  private int location = -1;

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
      Optional<Attribute> opt = Attribute.getAttribute (buffer[ptr], buffer[ptr + 1]);
      assert opt.isPresent ();
      Attribute attribute = opt.get ();

      // There has to be a StartFieldAttribute, but it could be anywhere in the list
      if (attribute.getAttributeType () == AttributeType.START_FIELD)
        startFieldAttribute = (StartFieldAttribute) attribute;
      else
        attributes.add (attribute);

      this.buffer[bptr++] = buffer[ptr++];
      this.buffer[bptr++] = buffer[ptr++];
    }

    assert startFieldAttribute != null;
    startFieldAttribute.setExtended ();
  }

  public StartFieldExtendedOrder (StartFieldAttribute startFieldAttribute,
      List<Attribute> attributes)
  {
    this.startFieldAttribute = startFieldAttribute;
    this.attributes.addAll (attributes);

    buffer = new byte[4 + attributes.size () * 2];
    int ptr = 0;

    buffer[ptr++] = Order.START_FIELD_EXTENDED;
    buffer[ptr++] = (byte) (attributes.size () + 1);      // sfa + other attributes

    ptr = startFieldAttribute.pack (buffer, ptr);
    for (Attribute attribute : attributes)
      ptr = attribute.pack (buffer, ptr);
  }

  public StartFieldExtendedOrder (StartFieldAttribute startFieldAttribute)
  {
    this.startFieldAttribute = startFieldAttribute;
    buffer = new byte[4];
    buffer[0] = Order.START_FIELD_EXTENDED;
    buffer[1] = 0x01;                                     // just one attribute
    startFieldAttribute.pack (buffer, 2);
  }

  @Override
  public void process (DisplayScreen screen)
  {
    Pen pen = screen.getPen ();
    location = pen.getPosition ();
    pen.startField (startFieldAttribute);

    for (Attribute attribute : attributes)
      pen.addAttribute (attribute);

    pen.moveRight ();
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    String locationText = location >= 0 ? String.format ("(%04d)", location) : "";
    text.append (String.format ("SFE : %s %s", startFieldAttribute, locationText));

    for (Attribute attr : attributes)
      text.append (String.format ("\n      %-34s", attr));

    return text.toString ();
  }
}