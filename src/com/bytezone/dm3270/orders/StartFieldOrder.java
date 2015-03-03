package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.display.Cursor2;
import com.bytezone.dm3270.display.Cursor2.Direction;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenPosition2;

public class StartFieldOrder extends Order
{
  private final StartFieldAttribute startFieldAttribute;

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
    //    Cursor cursor = screenHandler.getCursor ();
    //    ScreenPosition sp = cursor.getScreenPosition ();
    //    sp.reset ();
    //    sp.clearAttributes ();
    //    sp.addAttribute (startFieldAttribute);
    //
    //    cursor.moveRight ();

    Cursor2 cursor2 = screen.getScreenCursor ();
    ScreenPosition2 sp2 = cursor2.getScreenPosition ();
    sp2.reset ();
    sp2.addAttribute (startFieldAttribute);
    cursor2.move (Direction.RIGHT);
  }

  @Override
  public String toString ()
  {
    return String.format ("SF  : %s", startFieldAttribute);
  }
}