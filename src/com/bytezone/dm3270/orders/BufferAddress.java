package com.bytezone.dm3270.orders;

public class BufferAddress
{
  //  public static final byte[] table = { // screen locations [64]
  //      0x40, (byte) 0xC1, (byte) 0xC2, (byte) 0xC3, (byte) 0xC4, (byte) 0xC5, (byte) 0xC6,
  //          (byte) 0xC7, (byte) 0xC8, (byte) 0xC9, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F,
  //          0x50, (byte) 0xD1, (byte) 0xD2, (byte) 0xD3, (byte) 0xD4, (byte) 0xD5,
  //          (byte) 0xD6, (byte) 0xD7, (byte) 0xD8, (byte) 0xD9, 0x5A, 0x5B, 0x5C, 0x5D,
  //          0x5E, 0x5F, 0x60, 0x61, (byte) 0xE2, (byte) 0xE3, (byte) 0xE4, (byte) 0xE5,
  //          (byte) 0xE6, (byte) 0xE7, (byte) 0xE8, (byte) 0xE9, 0x6A, 0x6B, 0x6C, 0x6D,
  //          0x6E, 0x6F, (byte) 0xF0, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4,
  //          (byte) 0xF5, (byte) 0xF6, (byte) 0xF7, (byte) 0xF8, (byte) 0xF9, 0x7A, 0x7B,
  //          0x7C, 0x7D, 0x7E, 0x7F };
  public static final byte[] address = new byte[64];

  private int location;
  private boolean isValid;
  private final int columns = 80;             // fix this
  private final byte b1, b2;

  static
  {
    int value = 0x40;
    int ptr = 0;

    for (int i = 0; i < 4; i++)
    {
      address[ptr++] = (byte) value++;
      for (int j = 0; j < 9; j++)
        address[ptr++] = (byte) (value++ | 0x80);
      for (int j = 0; j < 6; j++)
        address[ptr++] = (byte) value++;
    }

    address[33] &= 0x7F;             // = 0x61;             // was 0xE1
    address[48] |= (byte) 0x80;      // = (byte) 0xF0;      // was 0x70

    //    for (int i = 0; i < 64; i++)
    //      if (table[i] != address[i])
    //        System.out.printf ("***");
  }

  public BufferAddress (byte b1, byte b2)
  {
    this.b1 = b1;
    this.b2 = b2;
    int flag = b1 & 0xC0;       // top two bits
    isValid = (flag != 0x80);

    if (flag == 0)              // using 14-bit method
    {
      location = (b1 & 0x3F) << 8;
      location |= (b2 & 0xFF);
    }
    else
    {
      location = (b1 & 0x3F) << 6;
      location |= (b2 & 0x3F);
    }
  }

  public boolean isValid ()
  {
    return isValid;
  }

  public BufferAddress (int location)
  {
    this.location = location;
    b1 = address[location >> 6];
    b2 = address[location & 0x3F];
  }

  public int getLocation ()
  {
    return location;
  }

  public int packAddress (byte[] buffer, int offset)
  {
    buffer[offset++] = address[location >> 6];
    buffer[offset++] = address[location & 0x3F];

    return offset;
  }

  @Override
  public String toString ()
  {
    return String.format ("%04d %02d/%02d   : %02X %02X", location, location / columns,
                          location % columns, b1, b2);
  }
}