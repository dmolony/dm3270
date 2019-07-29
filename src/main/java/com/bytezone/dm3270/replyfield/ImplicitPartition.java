package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.buffers.Buffer;

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

    ptr = Buffer.packUnsignedShort(0x50, reply, ptr);         // width
    ptr = Buffer.packUnsignedShort(0x18, reply, ptr);         // height

    ptr = Buffer.packUnsignedShort(columns, reply, ptr);      // alt width
    ptr = Buffer.packUnsignedShort(rows, reply, ptr);         // alt height

    checkDataLength(ptr);
  }

  public ImplicitPartition(byte[] buffer) {
    super(buffer);

    assert data[1] == IMP_PART_QUERY_REPLY;

    width = Buffer.unsignedShort(data, 7);
    height = Buffer.unsignedShort(data, 9);
    alternateWidth = Buffer.unsignedShort(data, 11);
    alternateHeight = Buffer.unsignedShort(data, 13);
  }

  @Override
  public String toString() {
    return super.toString() + String.format("%n  width      : %d", width)
        + String.format("%n  height     : %d", height)
        + String.format("%n  alt width  : %d", alternateWidth)
        + String.format("%n  alt height : %d", alternateHeight);
  }

}
