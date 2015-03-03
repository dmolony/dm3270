package com.bytezone.dm3270.orders;

import java.io.UnsupportedEncodingException;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.display.Cursor2;
import com.bytezone.dm3270.display.Cursor2.Direction;
import com.bytezone.dm3270.display.Screen;

public class TextOrder extends Order
{
  public TextOrder (byte[] buffer, int ptr, int max)
  {
    int dataLength = getDataLength (buffer, ptr, max);
    this.buffer = new byte[dataLength];
    System.arraycopy (buffer, ptr, this.buffer, 0, dataLength);
    //    ptr += dataLength;
  }

  public TextOrder (String text)
  {
    try
    {
      buffer = text.getBytes ("CP1047");
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }
  }

  private int getDataLength (byte[] buffer, int offset, int max)
  {
    int ptr = offset + 1;
    int length = 1;
    while (ptr < max)
    {
      int value = buffer[ptr++] & 0xFF;
      if (value <= 0x3C && value > 0)       // must be a new command
        break;
      length++;
    }

    return length;
  }

  public String getTextString ()
  {
    return buffer.length == 0 ? "" : "[" + Utility.getString (buffer) + "]";
  }

  @Override
  public void process (Screen screen)
  {
    //    Cursor cursor = screenHandler.getCursor ();
    //    for (byte b : buffer)
    //    {
    //      ScreenPosition sp = cursor.getScreenPosition ();
    //      sp.reset ();
    //      sp.clearAttributes ();
    //      sp.setCharacter (b);
    //      cursor.moveRight ();      // processes unapplied character attributes
    //    }

    Cursor2 cursor2 = screen.getScreenCursor ();
    for (byte b : buffer)
    {
      //      System.out.printf ("setting %02X at %d%n", b, cursor2.getLocation ());
      cursor2.setChar (b);
      cursor2.move (Direction.RIGHT);
    }
  }

  @Override
  public String toString ()
  {
    return getTextString ();
  }
}