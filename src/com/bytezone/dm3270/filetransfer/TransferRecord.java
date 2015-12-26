package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.utilities.Dm3270Utility;

public class TransferRecord
{
  final byte[] data;

  public TransferRecord (int size)
  {
    data = new byte[size];
  }

  public TransferRecord (byte[] data, int offset)
  {
    int length = data[offset + 1] & 0xFF;
    this.data = new byte[length];
    System.arraycopy (data, offset, this.data, 0, length);
  }

  protected TransferRecord (byte type, int length)
  {
    this.data = new byte[length];
    data[0] = type;
    data[1] = (byte) data.length;
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
    return String.format ("record    : %s", Dm3270Utility.toHexString (data));
  }
}