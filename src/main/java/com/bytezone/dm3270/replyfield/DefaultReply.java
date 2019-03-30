package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.utilities.Dm3270Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultReply extends QueryReplyField {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultReply.class);

  public DefaultReply(byte[] buffer) {
    super(buffer);
    LOG.warn("Unknown reply field: {}\n{}", String.format("%02X", buffer[0]),
        Dm3270Utility.toHex(buffer));
  }

  @Override
  public String toString() {
    return super.toString() + String.format("%n%n%s", Dm3270Utility.toHex(data));
  }

}
