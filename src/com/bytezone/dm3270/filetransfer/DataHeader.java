package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;

public class DataHeader extends DataRecord
{
  public static final byte RECORD_LENGTH = 5;

  final int bufferLength;
  final boolean compressed;

  public DataHeader (int bufferLength, boolean compressed)
  {
    super ((byte) 0x00, RECORD_LENGTH);
    this.compressed = compressed;
    this.bufferLength = bufferLength;

    if (compressed)
    {
      data[0] = (byte) 0x00;    // no idea
      data[1] = (byte) 0x00;    // no idea
    }
    else
    {
      data[0] = (byte) 0xC0;
      data[1] = (byte) 0x80;
    }

    data[2] = 0x61;
    Utility.packUnsignedShort (bufferLength + RECORD_LENGTH, data, 3);
  }

  public DataHeader (byte[] data, int offset)
  {
    super ((byte) 0x00, RECORD_LENGTH);
    System.arraycopy (data, offset, this.data, 0, RECORD_LENGTH);
    bufferLength = Utility.unsignedShort (this.data, 3);
    compressed = data[offset] != (byte) 0xC0 || data[offset + 1] != (byte) 0x80;
  }

  @Override
  public String toString ()
  {
    return String.format ("header    : %s (%scompressed, %,d + %d)",
                          Utility.toHexString (data), (compressed ? "" : "un"),
                          (bufferLength - RECORD_LENGTH), RECORD_LENGTH);
  }
}