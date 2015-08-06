package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;

public class DataHeader
{
  public static final byte HEADER_LENGTH = 5;

  private final boolean compressed;
  private final byte[] header;
  private final byte[] buffer;

  public DataHeader (byte[] data, int offset)
  {
    compressed = data[offset] != (byte) 0xC0 || data[offset + 1] != (byte) 0x80;
    assert data[offset + 2] == 0x61;
    int bufferLength = Utility.unsignedShort (data, offset + 3) - HEADER_LENGTH;

    header = new byte[HEADER_LENGTH];
    System.arraycopy (data, offset, header, 0, header.length);

    buffer = new byte[bufferLength];
    System.arraycopy (data, offset + HEADER_LENGTH, buffer, 0, buffer.length);
  }

  public DataHeader (byte[] buffer, int offset, int length, boolean compressed)
  {
    this.compressed = compressed;
    header = new byte[HEADER_LENGTH];

    this.buffer = new byte[length];
    System.arraycopy (buffer, offset, this.buffer, 0, length);

    if (compressed)
    {
      header[0] = (byte) 0x00;// no idea
      header[1] = (byte) 0x00;// no idea
    }
    else
    {
      header[0] = (byte) 0xC0;
      header[1] = (byte) 0x80;
    }

    header[2] = 0x61;
    Utility.packUnsignedShort (header.length + this.buffer.length, header, 3);
  }

  public int getBufferLength ()
  {
    return buffer.length;
  }

  public int pack (byte[] buffer, int ptr)
  {
    System.arraycopy (header, 0, buffer, ptr, header.length);
    ptr += header.length;

    return packBuffer (buffer, ptr);
  }

  public int packBuffer (byte[] buffer, int ptr)
  {
    System.arraycopy (this.buffer, 0, buffer, ptr, this.buffer.length);
    ptr += this.buffer.length;
    return ptr;
  }

  public String getHexBuffer (boolean ebcdic)
  {
    return Utility.toHex (buffer, ebcdic);
  }

  protected boolean checkEbcdic ()
  {
    return checkEbcdic (buffer, 0, buffer.length);
  }

  //  protected boolean checkEbcdic (byte[] data)
  //  {
  //    return checkEbcdic (data, 0, data.length);
  //  }

  protected boolean checkEbcdic (byte[] data, int offset, int length)
  {
    int ascii = 0;
    int ebcdic = 0;

    while (length > 0)
    {
      if (data[offset] == 0x20)
        ascii++;
      else if (data[offset] == 0x40)
        ebcdic++;
      length--;
      offset++;
    }
    return ebcdic > ascii;
  }

  @Override
  public String toString ()
  {
    return String.format ("header    : %s (%scompressed, %,d + %d)", //
                          Utility.toHexString (header), (compressed ? "" : "un"),
                          buffer.length, header.length);
  }
}