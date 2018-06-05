package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.utilities.Dm3270Utility;

public class DefaultReply extends QueryReplyField {

  public DefaultReply(byte[] buffer) {
    super(buffer);
    System.out.printf("Unknown reply field: %02X%n", buffer[0]);
    System.out.println(Dm3270Utility.toHex(buffer));
  }

  @Override
  public String toString() {
    return super.toString() + String.format("%n%n%s", Dm3270Utility.toHex(data));
  }

}
