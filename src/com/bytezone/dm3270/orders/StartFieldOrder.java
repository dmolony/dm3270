package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.application.Cursor;
import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.application.ScreenPosition;
import com.bytezone.dm3270.attributes.StartFieldAttribute;

public class StartFieldOrder extends Order
{
  protected final StartFieldAttribute startFieldAttribute;

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
    this.buffer[1] = startFieldAttribute.getValue ();
  }

  @Override
  public void process (ScreenHandler screenHandler)
  {
    Cursor cursor = screenHandler.getCursor ();
    ScreenPosition sp = cursor.getScreenPosition ();
    sp.reset ();
    sp.clearAttributes ();
    sp.addAttribute (startFieldAttribute);

    cursor.moveRight ();
  }

  @Override
  public String toString ()
  {
    return String.format ("SF  : %s", startFieldAttribute);
  }
}