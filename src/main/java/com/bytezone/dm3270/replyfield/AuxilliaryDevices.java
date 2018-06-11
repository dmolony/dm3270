package com.bytezone.dm3270.replyfield;

public class AuxilliaryDevices extends QueryReplyField {

  private byte flags1;
  private byte flags2;

  public AuxilliaryDevices(byte[] buffer) {
    super(buffer);

    assert data[1] == AUXILLIARY_DEVICE_REPLY;

    flags1 = data[2];
    flags2 = data[3];
  }

  @Override
  public String toString() {
    return super.toString() + String.format("%n  flags1     : %02X", flags1)
        + String.format("%n  flags2     : %02X", flags2);
  }

}
