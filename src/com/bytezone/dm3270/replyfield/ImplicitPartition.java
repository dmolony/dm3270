package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.utilities.Dm3270Utility;

public class ImplicitPartition extends QueryReplyField
{
  int width, height;
  int alternateWidth, alternateHeight;
  private ScreenDimensions implicitScreenDimensions;
  private ScreenDimensions alternateScreenDimensions;

  public ImplicitPartition (int rows, int columns)
  {
    super (IMP_PART_QUERY_REPLY);

    int ptr = createReply (13);

    reply[ptr++] = 0x00;
    reply[ptr++] = 0x00;

    reply[ptr++] = 0x0B;
    reply[ptr++] = 0x01;
    reply[ptr++] = 0x00;

    ptr = Dm3270Utility.packUnsignedShort (0x50, reply, ptr);         // width
    ptr = Dm3270Utility.packUnsignedShort (0x18, reply, ptr);         // height

    ptr = Dm3270Utility.packUnsignedShort (columns, reply, ptr);      // alt width
    ptr = Dm3270Utility.packUnsignedShort (rows, reply, ptr);         // alt height

    checkDataLength (ptr);
  }

  public ImplicitPartition (byte[] buffer)
  {
    super (buffer);

    assert data[1] == IMP_PART_QUERY_REPLY;

    width = Dm3270Utility.unsignedShort (data, 7);
    height = Dm3270Utility.unsignedShort (data, 9);
    alternateWidth = Dm3270Utility.unsignedShort (data, 11);
    alternateHeight = Dm3270Utility.unsignedShort (data, 13);

    implicitScreenDimensions = new ScreenDimensions (height, width);
    alternateScreenDimensions = new ScreenDimensions (alternateHeight, alternateWidth);
  }

  public ScreenDimensions getScreenDimensions ()
  {
    return implicitScreenDimensions;
  }

  public ScreenDimensions getAlternateScreenDimensions ()
  {
    return alternateScreenDimensions;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (super.toString ());

    text.append (String.format ("%n  width      : %d", width));
    text.append (String.format ("%n  height     : %d", height));
    text.append (String.format ("%n  alt width  : %d", alternateWidth));
    text.append (String.format ("%n  alt height : %d", alternateHeight));
    return text.toString ();
  }
}