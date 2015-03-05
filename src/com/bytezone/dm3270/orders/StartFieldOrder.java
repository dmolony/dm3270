package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.Cursor.Direction;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenPosition;

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
    this.buffer[1] = startFieldAttribute.getValue ();
  }

  @Override
  public void process (Screen screen)
  {
    Cursor cursor = screen.getScreenCursor ();
    ScreenPosition sp = cursor.getScreenPosition ();
    location = cursor.getLocation ();
    sp.reset ();
    sp.addAttribute (startFieldAttribute);
    cursor.move (Direction.RIGHT);
  }

  @Override
  public String toString ()
  {
    return String.format ("SF  : %s (%04d)", startFieldAttribute, location);
  }
}