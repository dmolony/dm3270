package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.application.Cursor;
import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.application.ScreenPosition;

public class GraphicsEscapeOrder extends Order
{
  private final byte code;

  public GraphicsEscapeOrder (byte[] buffer, int offset)
  {
    assert buffer[offset] == Order.GRAPHICS_ESCAPE;
    code = buffer[offset + 1];

    this.buffer = new byte[2];
    this.buffer[0] = buffer[offset];
    this.buffer[1] = buffer[offset + 1];
  }

  @Override
  public void process (ScreenHandler screenHandler)
  {
    Cursor cursor = screenHandler.getCursor ();

    int max = duplicates + 1;
    for (int i = 0; i < max; i++)
    {
      ScreenPosition sp = cursor.getScreenPosition ();
      sp.reset ();
      sp.clearAttributes ();
      sp.setGraphicsCharacter (code);
      cursor.moveRight ();      // will add any unapplied attributes
    }
  }

  @Override
  public boolean matches (Order order)
  {
    if (order instanceof GraphicsEscapeOrder
        && this.code == ((GraphicsEscapeOrder) order).code)
      return true;
    return false;
  }

  @Override
  public String toString ()
  {
    String duplicateText = duplicates == 0 ? "" : "x" + (duplicates + 1);
    return String.format ("GE  : %02X  %s", code, duplicateText);
  }
}