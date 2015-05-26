package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;

public class DataHeader
{
  public static final byte RECORD_LENGTH = 5;

  private final boolean compressed;
  private final byte[] header;
  private final byte[] buffer;

  public DataHeader (int bufferLength, boolean compressed)
  {
    this.compressed = compressed;
    header = new byte[RECORD_LENGTH];
    buffer = new byte[bufferLength];

    if (compressed)
    {
      header[0] = (byte) 0x00;    // no idea
      header[1] = (byte) 0x00;    // no idea
    }
    else
    {
      header[0] = (byte) 0xC0;
      header[1] = (byte) 0x80;
    }

    header[2] = 0x61;
    Utility.packUnsignedShort (buffer.length + header.length, header, 3);
  }

  public DataHeader (byte[] data, int offset)
  {
    compressed = data[offset] != (byte) 0xC0 || data[offset + 1] != (byte) 0x80;
    assert data[offset + 2] == 0x61;
    int bufferLength = Utility.unsignedShort (data, offset + 3) - RECORD_LENGTH;

    header = new byte[RECORD_LENGTH];
    System.arraycopy (data, offset, header, 0, header.length);

    buffer = new byte[bufferLength];
    System.arraycopy (data, offset + RECORD_LENGTH, buffer, 0, buffer.length);
  }

  public int size ()
  {
    return buffer.length;
  }

  public byte[] getHeader ()
  {
    return header;
  }

  public byte[] getBuffer ()
  {
    return buffer;
  }

  public int pack (byte[] buffer, int ptr)
  {
    System.arraycopy (header, 0, buffer, ptr, this.buffer.length);
    ptr += header.length;
    System.arraycopy (this.buffer, 0, buffer, ptr, this.buffer.length);
    ptr += this.buffer.length;

    return ptr;
  }

  @Override
  public String toString ()
  {
    return String.format ("header    : %s (%scompressed, %,d + %d)", //
                          Utility.toHexString (header), (compressed ? "" : "un"),
                          buffer.length, RECORD_LENGTH);
  }
}