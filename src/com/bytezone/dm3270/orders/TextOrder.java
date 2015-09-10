package com.bytezone.dm3270.orders;

import java.io.UnsupportedEncodingException;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.display.DisplayScreen;
import com.bytezone.dm3270.display.Pen;

public class TextOrder extends Order
{
  private static byte[] orderValues =
      { START_FIELD, START_FIELD_EXTENDED, SET_BUFFER_ADDRESS, INSERT_CURSOR,
        GRAPHICS_ESCAPE, REPEAT_TO_ADDRESS, ERASE_UNPROTECTED, PROGRAM_TAB, SET_ATTRIBUTE,
        MODIFY_FIELD, FCO_NULL, FCO_SUBSTITUTE, FCO_DUPLICATE, FCO_FIELD_MARK,
        FCO_FORM_FEED, FCO_CARRIAGE_RETURN, FCO_NEWLINE, FCO_END_OF_MEDIUM,
        FCO_EIGHT_ONES };
  private int bufferOffset;
  byte[] originalBuffer;

  public TextOrder (byte[] buffer, int ptr, int max)
  {
    bufferOffset = ptr;// save for later scrambling
    originalBuffer = buffer;

    int dataLength = getDataLength (buffer, ptr, max);
    this.buffer = new byte[dataLength];
    System.arraycopy (buffer, ptr, this.buffer, 0, dataLength);
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
    outer_loop: while (ptr < max)
    {
      byte value = buffer[ptr++];
      if (value == GRAPHICS_ESCAPE)
      {
        if (GraphicsEscapeOrder.isValid (buffer[ptr]))
          break;
      }
      else if (value >= 0 && value <= 0x3F) // could be a new command
        for (int i = 0; i < orderValues.length; i++)
          if (value == orderValues[i])
            break outer_loop;

      length++;
    }
    return length;
  }

  public void scramble ()
  {
    for (int ptr = 0; ptr < buffer.length; ptr++)
      originalBuffer[bufferOffset + ptr] = 0x7B;
  }

  public String getTextString ()
  {
    return buffer.length == 0 ? "" : "[" + Utility.getString (buffer) + "]";
  }

  @Override
  public void process (DisplayScreen screen)
  {
    Pen pen = screen.getPen ();
    for (byte b : buffer)
      pen.write (b);
  }

  @Override
  public String toString ()
  {
    return getTextString ();
  }
}