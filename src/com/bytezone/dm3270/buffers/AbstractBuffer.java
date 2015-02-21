package com.bytezone.dm3270.buffers;

public abstract class AbstractBuffer implements Buffer
{
  protected byte[] data;

  public AbstractBuffer ()
  {
    data = new byte[0];
  }

  public AbstractBuffer (byte[] buffer)
  {
    this (buffer, 0, buffer.length);
  }

  public AbstractBuffer (byte[] buffer, int offset, int length)
  {
    data = new byte[length];
    System.arraycopy (buffer, offset, data, 0, length);
  }

  @Override
  public byte[] getData ()
  {
    return data;
  }

  @Override
  public int size ()
  {
    return data.length;
  }

  @Override
  public byte[] getTelnetData ()
  {
    int length = data.length + countFF (data) + 2; // allow for expanded 0xFF and IAC/EOR
    byte[] buffer = new byte[length];
    copyAndExpand (data, buffer, 0);
    buffer[--length] = (byte) 0xEF;     // EOR
    buffer[--length] = (byte) 0xFF;     // IAC
    return buffer;
  }

  protected int countFF (byte[] buffer)
  {
    int count = 0;
    for (byte b : buffer)
      if (b == (byte) 0xFF)
        count++;
    return count;
  }

  protected int copyAndExpand (byte[] source, byte[] dest, int ptr)
  {
    for (byte b : source)
    {
      dest[ptr++] = b;
      if (b == (byte) 0xFF)
        dest[ptr++] = b;
    }
    return ptr;
  }
}