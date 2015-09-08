package com.bytezone.dm3270.orders;

import java.io.UnsupportedEncodingException;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.display.DisplayScreen;
import com.bytezone.dm3270.display.Pen;

public class TextOrder extends Order
{
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
    while (ptr < max)
    {
      byte value = buffer[ptr++];
      if (value >= 0 && value <= 0x3C) // must be a new command
        //      if (value == START_FIELD || value == START_FIELD_EXTENDED
        //          || value == SET_BUFFER_ADDRESS || value == INSERT_CURSOR
        //          || value == GRAPHICS_ESCAPE || value == REPEAT_TO_ADDRESS
        //          || value == ERASE_UNPROTECTED || value == PROGRAM_TAB 
        //          || value == SET_ATTRIBUTE
        //          || value == MODIFY_FIELD)
        break;
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