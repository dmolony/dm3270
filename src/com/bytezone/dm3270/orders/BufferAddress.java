package com.bytezone.dm3270.orders;

public class BufferAddress
{
  public static final byte[] address = new byte[64];
  private static int columns = 80;                          // default value

  private int location;
  private boolean isValid;
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
  }

  public static void setScreenWidth (int width)
  {
    columns = width;
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
    return String.format ("%04d %03d/%03d : %02X %02X", location, location / columns,
                          location % columns, b1, b2);
  }
}