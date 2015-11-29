package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.display.DisplayScreen;
import com.bytezone.dm3270.display.Pen;

public class StartFieldOrder extends Order
{
  private final StartFieldAttribute startFieldAttribute;
  private int location = -1;

  public StartFieldOrder (byte[] buffer, int offset)
  {
    assert buffer[offset] == Order.START_FIELD;

    startFieldAttribute = new StartFieldAttribute (buffer[offset + 1]);

    this.buffer = new byte[2];
    this.buffer[0] = buffer[offset];
    this.buffer[1] = buffer[offset + 1];
  }

  public StartFieldOrder (StartFieldAttribute startFieldAttribute)
  {
    this.startFieldAttribute = startFieldAttribute;
    this.buffer = new byte[2];
    this.buffer[0] = Order.START_FIELD;
    this.buffer[1] = startFieldAttribute.getAttributeValue ();
  }

  @Override
  public void process (DisplayScreen screen)
  {
    Pen pen = screen.getPen ();
    location = pen.getPosition ();
    //    startFieldAttribute.process (pen);          // calls pen.startField()
    pen.startField (startFieldAttribute);
    pen.moveRight ();
  }

  @Override
  public String toString ()
  {
    return String.format ("SF  : %s (%04d)", startFieldAttribute, location);
  }
}