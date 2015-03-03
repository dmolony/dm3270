package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.display.Cursor2;
import com.bytezone.dm3270.display.Cursor2.Direction;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenPosition2;

public class RepeatToAddressOrder extends Order
{
  private final BufferAddress stopAddress;
  private char repeatCharacter;
  private byte rptChar;           // only kept for toString()

  public RepeatToAddressOrder (byte[] buffer, int offset)
  {
    assert buffer[offset] == Order.REPEAT_TO_ADDRESS;

    stopAddress = new BufferAddress (buffer[offset + 1], buffer[offset + 2]);

    if (buffer[offset + 3] == Order.GRAPHICS_ESCAPE)
    {
      repeatCharacter = (char) Utility.ebc2asc[buffer[offset + 4] & 0xFF];
      // offset + 5 can be used, but I haven't seen one yet
      rptChar = buffer[offset + 4];

      this.buffer = new byte[6];
    }
    else
    {
      repeatCharacter = (char) Utility.ebc2asc[buffer[offset + 3] & 0xFF];
      rptChar = buffer[offset + 3];

      this.buffer = new byte[4];
    }

    System.arraycopy (buffer, offset, this.buffer, 0, this.buffer.length);

    if (rptChar == 0)
      repeatCharacter = ' ';
  }

  @Override
  public void process (Screen screen)
  {
    int stopLocation = stopAddress.getLocation ();

    //    Cursor cursor = screenHandler.getCursor ();
    //    if (cursor.getLocation () == stopLocation && rptChar == 0x40)
    //      screenHandler.eraseScreen (true);
    //    else
    //    {
    //      while (cursor.getLocation () != stopLocation)
    //      {
    //        ScreenPosition screenPosition = cursor.getScreenPosition ();
    //        screenPosition.reset ();
    //        screenPosition.clearAttributes ();
    //        screenPosition.setCharacter (rptChar);
    //        cursor.moveRight ();
    //      }
    //    }

    int stopLocation2 = stopAddress.getLocation ();
    Cursor2 cursor2 = screen.getScreenCursor ();
    if (cursor2.getLocation () == stopLocation2 && (rptChar == 0x40 || rptChar == 0x00))
      screen.clearScreen ();
    else
    {
      while (cursor2.getLocation () != stopLocation)
      {
        ScreenPosition2 sp2 = cursor2.getScreenPosition ();
        sp2.reset ();
        sp2.setChar (rptChar);
        cursor2.move (Direction.RIGHT);
      }
    }
  }

  @Override
  public String toString ()
  {
    return String.format ("RTA : %-12s : %02X [%1.1s]", stopAddress, rptChar,
                          repeatCharacter);
  }
}