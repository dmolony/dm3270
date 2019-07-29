package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.Charset;
import com.bytezone.dm3270.buffers.Buffer;

public class RPQNames extends QueryReplyField {

  private String deviceType;
  private long model;
  private String rpqName;

  public RPQNames(byte[] buffer, Charset charset) {
    super(buffer);
    assert data[1] == RPQ_NAMES_REPLY;

    deviceType = charset.getString(data, 2, 4);
    model = Buffer.unsignedLong(data, 6);
    int len = (data[10] & 0xFF) - 1;
    if (len > 0) {
      rpqName = charset.getString(data, 11, len);
    } else {
      rpqName = "";
    }
  }

  @Override
  public String toString() {
    return super.toString() + String.format("%n  device     : %s", deviceType)
        + String.format("%n  model      : %d", model)
        + String.format("%n  RPQ name   : %s", rpqName);
  }

}
