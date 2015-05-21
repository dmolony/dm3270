package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;

public class DataRecord
{
  final byte[] data;

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

  @Override
  public String toString ()
  {
    return String.format ("record    : %s", Utility.toHexString (data));
  }
}