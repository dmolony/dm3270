package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.utilities.Dm3270Utility;

public class AlphanumericPartitions extends QueryReplyField {

  private int maxPartitions;
  private int totalAvailableStorage;
  private byte flags;
  private boolean vertWin;
  private boolean horWin;
  private boolean allPointsAddressability;
  private boolean partitionProtection;
  private boolean localCopy;
  private boolean modifyPartition;

  public AlphanumericPartitions(byte[] buffer) {
    super(buffer);
    assert data[1] == ALPHANUMERIC_PARTITIONS_REPLY;

    maxPartitions = data[2] & 0xFF;
    totalAvailableStorage = Dm3270Utility.unsignedShort(data, 3);

    flags = data[5];
    vertWin = (flags & 0x80) != 0;
    horWin = (flags & 0x40) != 0;
    allPointsAddressability = (flags & 0x10) != 0;
    partitionProtection = (flags & 0x08) != 0;
    localCopy = (flags & 0x04) != 0;
    modifyPartition = (flags & 0x02) != 0;
  }

  @Override
  public String toString() {
    return super.toString() + String.format("%n  max        : %d", maxPartitions)
        + String.format("%n  storage    : %d", totalAvailableStorage)
        + String.format("%n  flags      : %02X", flags)
        + String.format("%n  vert win   : %s", vertWin)
        + String.format("%n  hor win    : %s", horWin)
        + String.format("%n  APA        : %s", allPointsAddressability)
        + String.format("%n  protect    : %s", partitionProtection)
        + String.format("%n  lcopy      : %s", localCopy)
        + String.format("%n  modpart    : %s", modifyPartition);
  }

}
