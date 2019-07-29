package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.buffers.Buffer;

public class DistributedDataManagement extends QueryReplyField {

  private int flags;
  private int limitIn;
  private int limitOut;
  private int subsets;
  private byte ddmSubset;

  public DistributedDataManagement(byte[] buffer) {
    super(buffer);
    assert data[1] == DISTRIBUTED_DATA_MANAGEMENT_REPLY;

    flags = Buffer.unsignedShort(data, 2);
    limitIn = Buffer.unsignedShort(data, 4);
    limitOut = Buffer.unsignedShort(data, 6);
    subsets = data[8] & 0xFF;
    ddmSubset = data[9];

    int ptr = 10;
    while (ptr < data.length) {
      int len = data[ptr] & 0xFF;
      ptr += len;
    }
  }

  @Override
  public String toString() {
    return super.toString() + String.format("%n  flags      : %04X", flags)
        + String.format("%n  limit in   : %s", limitIn)
        + String.format("%n  limit out  : %s", limitOut)
        + String.format("%n  subsets    : %s", subsets)
        + String.format("%n  DDMSS      : %s", ddmSubset);
  }

}
