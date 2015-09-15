package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.display.DisplayScreen;
import com.bytezone.dm3270.display.Pen;

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
  public void process (DisplayScreen screen)
  {
    Pen pen = screen.getPen ();
    int max = duplicates;
    while (max-- >= 0)
      // always do at least one
      pen.writeGraphics (code);
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

  //  public static boolean isValid (byte value)
  //  {
  //    int v = value & 0xFF;
  //    return v >= 0x40 && v != 0xFF;
  //    //    if (value == ScreenPosition.HORIZONTAL_LINE || value == ScreenPosition.VERTICAL_LINE
  //    //        || value == ScreenPosition.TOP_LEFT || value == ScreenPosition.TOP_RIGHT
  //    //        || value == ScreenPosition.BOTTOM_LEFT || value == ScreenPosition.BOTTOM_RIGHT)
  //    //      return true;
  //    //    return false;
  //  }
}