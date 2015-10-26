package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.utilities.Utility;

public class ImplicitPartition extends QueryReplyField
{
  int width, height;
  int alternateWidth, alternateHeight;

  public ImplicitPartition ()
  {
    super (IMP_PART_QUERY_REPLY);

    int ptr = createReply (13);

    /* Bytes 4-5 Reserved */
    reply[ptr++] = 0x00;
    reply[ptr++] = 0x00;

    /* 6.31.3 Implicit Partition Sizes for Display Devices Self-Defining Parameter */
    reply[ptr++] = 0x0B;
    reply[ptr++] = 0x01;
    reply[ptr++] = 0x00;

    /* Bytes 9-10   Width of the Implicit Partition default screen size */
    ptr = Utility.packUnsignedShort (0x50, reply, ptr);

    /* Bytes 11-12  Height of the Implicit Partition default screen size */
    ptr = Utility.packUnsignedShort (0x18, reply, ptr);

    /* Bytes 13-14  Width of the Implicit Partition alternate screen size */
    ptr = Utility.packUnsignedShort (0x50, reply, ptr);

    /* Bytes 15-16  Height of the Implicit Partition alternate screen size */
    ptr = Utility.packUnsignedShort (0x18, reply, ptr);

    checkDataLength (ptr);
  }

  public ImplicitPartition (byte[] buffer)
  {
    super (buffer);

    assert data[1] == IMP_PART_QUERY_REPLY;

    width = Utility.unsignedShort (data, 7);
    height = Utility.unsignedShort (data, 9);
    alternateWidth = Utility.unsignedShort (data, 11);
    alternateHeight = Utility.unsignedShort (data, 13);
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