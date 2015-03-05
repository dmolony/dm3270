package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.Cursor.Direction;
import com.bytezone.dm3270.display.Screen;

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
  public void process (Screen screen)
  {
    Cursor cursor = screen.getScreenCursor ();
    int max = duplicates + 1;
    for (int i = 0; i < max; i++)
    {
      cursor.setGraphicsChar (code);
      cursor.move (Direction.RIGHT);
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