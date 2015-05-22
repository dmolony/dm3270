package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;

public class DataHeader extends DataRecord
{
  final int bufferLength;
  final boolean compressed;

  public DataHeader (byte[] data, int offset)
  {
    super (new byte[5]);
    System.arraycopy (data, offset, this.data, 0, 5);
    bufferLength = Utility.unsignedShort (this.data, 3) - 5;
    compressed = data[offset] != (byte) 0xC0 || data[offset + 1] != (byte) 0x80;
  }

  @Override
  public String toString ()
  {
    return String.format ("header    : %s (%scompressed, %,d + 5)",
                          Utility.toHexString (data), (compressed ? "" : "un"),
                          bufferLength);
  }
}