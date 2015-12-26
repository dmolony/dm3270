package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.utilities.Dm3270Utility;

public class AlphanumericPartitions extends QueryReplyField
{
  int maxPartitions;
  int totalAvailableStorage;
  byte flags;
  boolean vertWin, horWin;
  boolean allPointsAddressability;
  boolean partitionProtection;
  boolean localCopy;
  boolean modifyPartition;

  public AlphanumericPartitions ()
  {
    super (ALPHANUMERIC_PARTITIONS_REPLY);

    int ptr = createReply (4);
    checkDataLength (ptr + 4);
  }

  public AlphanumericPartitions (byte[] buffer)
  {
    super (buffer);
    assert data[1] == ALPHANUMERIC_PARTITIONS_REPLY;

    maxPartitions = data[2] & 0xFF;
    totalAvailableStorage = Dm3270Utility.unsignedShort (data, 3);

    flags = data[5];
    vertWin = (flags & 0x80) != 0;
    horWin = (flags & 0x40) != 0;
    allPointsAddressability = (flags & 0x10) != 0;
    partitionProtection = (flags & 0x08) != 0;
    localCopy = (flags & 0x04) != 0;
    modifyPartition = (flags & 0x02) != 0;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (super.toString ());

    text.append (String.format ("%n  max        : %d", maxPartitions));
    text.append (String.format ("%n  storage    : %d", totalAvailableStorage));
    text.append (String.format ("%n  flags      : %02X", flags));
    text.append (String.format ("%n  vert win   : %s", vertWin));
    text.append (String.format ("%n  hor win    : %s", horWin));
    text.append (String.format ("%n  APA        : %s", allPointsAddressability));
    text.append (String.format ("%n  protect    : %s", partitionProtection));
    text.append (String.format ("%n  lcopy      : %s", localCopy));
    text.append (String.format ("%n  modpart    : %s", modifyPartition));

    return text.toString ();
  }
}