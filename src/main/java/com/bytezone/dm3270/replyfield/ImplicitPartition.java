package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.utilities.Dm3270Utility;

public class ImplicitPartition extends QueryReplyField {

  private int width;
  private int height;
  private int alternateWidth;
  private int alternateHeight;

  public ImplicitPartition(int rows, int columns) {
    super(IMP_PART_QUERY_REPLY);

    int ptr = createReply(13);

    reply[ptr++] = 0x00;
    reply[ptr++] = 0x00;

    reply[ptr++] = 0x0B;
    reply[ptr++] = 0x01;
    reply[ptr++] = 0x00;

    ptr = Dm3270Utility.packUnsignedShort(0x50, reply, ptr);         // width
    ptr = Dm3270Utility.packUnsignedShort(0x18, reply, ptr);         // height

    ptr = Dm3270Utility.packUnsignedShort(columns, reply, ptr);      // alt width
    ptr = Dm3270Utility.packUnsignedShort(rows, reply, ptr);         // alt height

    checkDataLength(ptr);
  }

  public ImplicitPartition(byte[] buffer) {
    super(buffer);

    assert data[1] == IMP_PART_QUERY_REPLY;

    width = Dm3270Utility.unsignedShort(data, 7);
    height = Dm3270Utility.unsignedShort(data, 9);
    alternateWidth = Dm3270Utility.unsignedShort(data, 11);
    alternateHeight = Dm3270Utility.unsignedShort(data, 13);
  }

  @Override
  public String toString() {
    return super.toString() + String.format("%n  width      : %d", width)
        + String.format("%n  height     : %d", height)
        + String.format("%n  alt width  : %d", alternateWidth)
        + String.format("%n  alt height : %d", alternateHeight);
  }

}
