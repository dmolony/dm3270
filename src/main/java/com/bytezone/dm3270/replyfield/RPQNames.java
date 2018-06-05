package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.utilities.Dm3270Utility;

import java.io.UnsupportedEncodingException;

public class RPQNames extends QueryReplyField {

  private String deviceType;
  private long model;
  private String rpqName;

  public RPQNames(byte[] buffer) {
    super(buffer);
    assert data[1] == RPQ_NAMES_REPLY;

    try {
      deviceType = new String(data, 2, 4, "CP1047");
      model = Dm3270Utility.unsignedLong(data, 6);
      int len = (data[10] & 0xFF) - 1;
      if (len > 0) {
        rpqName = new String(data, 11, len, "CP1047");
      } else {
        rpqName = "";
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String toString() {
    return super.toString() + String.format("%n  device     : %s", deviceType)
        + String.format("%n  model      : %d", model)
        + String.format("%n  RPQ name   : %s", rpqName);
  }

}
