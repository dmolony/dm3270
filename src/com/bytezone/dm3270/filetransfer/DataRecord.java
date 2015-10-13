package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;

public class DataRecord extends TransferRecord
{
  static final byte HEADER_LENGTH = 5;

  private final boolean compressed;
  private final byte[] buffer;

  public DataRecord (byte[] data, int offset)
  {
    super (HEADER_LENGTH);

    compressed = data[offset + 2] != (byte) 0x61;
    int bufferLength = Utility.unsignedShort (data, offset + 3) - HEADER_LENGTH;

    System.arraycopy (data, offset, this.data, 0, HEADER_LENGTH);

    buffer = new byte[bufferLength];
    System.arraycopy (data, offset + HEADER_LENGTH, buffer, 0, buffer.length);
  }

  public DataRecord (byte[] buffer, int offset, int length, boolean compressed)
  {
    super (HEADER_LENGTH);

    this.compressed = compressed;

    this.buffer = new byte[length];
    System.arraycopy (buffer, offset, this.buffer, 0, length);

    data[0] = (byte) 0xC0;
    data[1] = (byte) 0x80;

    data[2] = compressed ? (byte) 0x00 : (byte) 0x61;// guess
    Utility.packUnsignedShort (HEADER_LENGTH + this.buffer.length, data, 3);
  }

  @Override
  public int length ()
  {
    return data.length + buffer.length;
  }

  public int getBufferLength ()
  {
    return buffer.length;
  }

  void checkAscii ()
  {
    for (int i = 0; i < buffer.length; i++)
    {
      int b = buffer[i] & 0xFF;
      if ((b < 0x20 || b >= 0xC0) && (b != 0x0D && b != 0x0A && b != 0x1A))
        System.out.printf ("Not ascii: %02X%n", b);
    }
  }

  void checkEbcdic ()
  {
    for (int i = 0; i < buffer.length; i++)
    {
      int b = buffer[i] & 0xFF;
      if (b != 0x40 && (b < 0x4B || b == 0xFF))
        System.out.printf ("Not ebcdic: %02X%n", b);
    }
  }

  @Override
  public int pack (byte[] buffer, int ptr)
  {
    System.arraycopy (data, 0, buffer, ptr, HEADER_LENGTH);
    ptr += HEADER_LENGTH;

    return packBuffer (buffer, ptr);
  }

  public int packBuffer (byte[] buffer, int ptr)
  {
    System.arraycopy (this.buffer, 0, buffer, ptr, this.buffer.length);
    ptr += this.buffer.length;
    return ptr;
  }

  public String getHexBuffer ()
  {
    boolean ebcdic = checkEbcdic (buffer, 0, buffer.length);
    return Utility.toHex (buffer, ebcdic);
  }

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
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("header    : %s (%scompressed, %,d + %d)%n%n", //
                                Utility.toHexString (data), (compressed ? "" : "un"),
                                HEADER_LENGTH, buffer.length));
    text.append (getHexBuffer ());

    return text.toString ();
  }
}