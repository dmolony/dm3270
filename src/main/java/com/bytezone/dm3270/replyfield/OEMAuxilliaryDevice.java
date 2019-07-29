package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.Charset;
import com.bytezone.dm3270.structuredfields.StructuredField;

public class OEMAuxilliaryDevice extends QueryReplyField {

  private byte flags;
  private byte refID;
  private String deviceType;
  private String userName;

  public OEMAuxilliaryDevice(byte[] buffer, Charset charset) {
    super(buffer);

    assert data[0] == StructuredField.QUERY_REPLY;
    assert data[1] == QueryReplyField.OEM_AUXILLIARY_DEVICE_REPLY;

    flags = data[2];
    refID = data[3];
    deviceType = charset.getString(data, 4, 8).trim();
    userName = charset.getString(data, 12, 8).trim();

  }

  @Override
  public String toString() {
    return super.toString() + String.format("%n  flags1     : %02X", flags)
        + String.format("%n  ref ID     : %02X", refID)
        + String.format("%n  type       : %s", deviceType)
        + String.format("%n  name       : %s", userName);
  }

}
