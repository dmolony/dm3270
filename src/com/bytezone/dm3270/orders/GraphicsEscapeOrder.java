package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.Cursor.Direction;
import com.bytezone.dm3270.display.Pen;
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
    if (oldWay)
    {
      Cursor cursor = screen.getScreenCursor ();
      int max = duplicates;
      while (max-- >= 0)                    // always do at least one
      {
        cursor.setGraphicsChar (code);
        cursor.move (Direction.RIGHT);
      }
    }
    else
    {
      Pen pen = screen.getPen ();
      int max = duplicates;
      while (max-- >= 0)
        // always do at least one
        pen.writeGraphics (code);
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
    String duplicateText = duplicates == 0 ? "" : "x " + (duplicates + 1);
    return String.format ("GE  : %02X %s", code, duplicateText);
  }
}