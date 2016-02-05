package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.utilities.Dm3270Utility;

public class UsableArea extends QueryReplyField
{
  private static String[] measurementUnits = { "Inches", "Millimetres" };
  private static String[] addressingModes =
      { "Reserved", "12/14 bit", "Reserved", "12/14/16 bit", "Unmapped" };
  byte flags1, flags2;
  int addressingMode;
  int width;
  int height;
  byte unitsOfMeasurement;
  int xUnits, yUnits;
  int xNumerator, xDenominator;
  int yNumerator, yDenominator;
  int bufferSize;

  public UsableArea (int rows, int columns)
  {
    super (USABLE_AREA_REPLY);

    // leave a gap for the screen size fields
    byte[] rest = { 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, (byte) 0xD3, 0x03,
                    0x20, 0x00, (byte) 0x9E, 0x02, 0x58, 0x07, 0x0C, 0x07, (byte) 0x80 };

    int ptr = createReply (rest.length);

    for (byte b : rest)
      reply[ptr++] = b;

    // copy the screen dimensions into the reply (skipping 4 header bytes)
    Dm3270Utility.packUnsignedShort (columns, reply, 6);
    Dm3270Utility.packUnsignedShort (rows, reply, 8);
    //    Dm3270Utility.packUnsignedShort (rows * columns, reply, 21);
    Dm3270Utility.packUnsignedShort (1920, reply, 21);

    checkDataLength (ptr);
  }

  public UsableArea (byte[] buffer)
  {
    super (buffer);

    assert data[1] == USABLE_AREA_REPLY;

    flags1 = data[2];
    flags2 = data[3];
    width = Dm3270Utility.unsignedShort (data, 4);
    height = Dm3270Utility.unsignedShort (data, 6);
    unitsOfMeasurement = data[8];
    xNumerator = Dm3270Utility.unsignedShort (data, 9);
    xDenominator = Dm3270Utility.unsignedShort (data, 11);
    yNumerator = Dm3270Utility.unsignedShort (data, 13);
    yDenominator = Dm3270Utility.unsignedShort (data, 15);
    xUnits = data[17] & 0xFF;
    yUnits = data[18] & 0xFF;

    if (data.length >= 20)
      bufferSize = Dm3270Utility.unsignedShort (data, 19);

    addressingMode = flags1 & 0x0F;
    if (addressingMode == 15)
      addressingMode = 4;
  }

  public ScreenDimensions getScreenDimensions ()
  {
    return new ScreenDimensions (height, width);
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (super.toString ());

    text.append (String.format ("%n  flags1     : %02X", flags1));
    text.append (String.format ("%n  ad mode    : %s", addressingModes[addressingMode]));
    text.append (String.format ("%n  flags2     : %02X", flags2));
    text.append (String.format ("%n  width      : %d", width));
    text.append (String.format ("%n  height     : %d", height));
    text.append (String.format ("%n  units      : %d - %s", unitsOfMeasurement,
                                measurementUnits[unitsOfMeasurement]));
    text.append (String.format ("%n  x ratio    : %d / %d", xNumerator, xDenominator));
    text.append (String.format ("%n  y ratio    : %d / %d", yNumerator, yDenominator));
    text.append (String.format ("%n  x units    : %d", xUnits));
    text.append (String.format ("%n  y units    : %d", yUnits));
    if (bufferSize > 0)
      text.append (String.format ("%n  buffer     : %d", bufferSize));

    return text.toString ();
  }
}