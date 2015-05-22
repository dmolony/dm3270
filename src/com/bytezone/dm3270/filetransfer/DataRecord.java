package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;

public class DataRecord
{
  final byte[] data;

  protected DataRecord (byte[] data)
  {
    this.data = data;
  }

  public DataRecord (byte[] data, int offset)
  {
    int length = data[offset + 1] & 0xFF;
    this.data = new byte[length];
    System.arraycopy (data, offset, this.data, 0, length);
  }

  public int length ()
  {
    return data.length;
  }

  public int pack (byte[] buffer, int ptr)
  {
    System.arraycopy (data, 0, buffer, ptr, data.length);
    return ptr + data.length;
  }

  @Override
  public String toString ()
  {
    return String.format ("record    : %s", Utility.toHexString (data));
  }
}