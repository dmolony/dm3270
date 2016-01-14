package com.bytezone.dm3270.orders;

import java.io.UnsupportedEncodingException;

import com.bytezone.dm3270.display.DisplayScreen;
import com.bytezone.dm3270.display.Pen;
import com.bytezone.dm3270.utilities.Dm3270Utility;

public class TextOrder extends Order
{
  private int bufferOffset;
  byte[] originalBuffer;

  public TextOrder (byte[] buffer, int ptr, int max)
  {
    bufferOffset = ptr;                         // save for later scrambling
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
    return Dm3270Utility.getString (buffer);
  }

  @Override
  public boolean isText ()
  {
    return true;
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
    return buffer.length == 0 ? "" : "Text: [" + Dm3270Utility.getString (buffer) + "]";
  }
}