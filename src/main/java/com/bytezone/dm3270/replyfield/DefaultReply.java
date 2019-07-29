package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultReply extends QueryReplyField {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultReply.class);
  private final Charset charset;

  public DefaultReply(byte[] buffer, Charset charset) {
    super(buffer);
    this.charset = charset;
    LOG.warn("Unknown reply field: {}\n{}", String.format("%02X", buffer[0]),
        charset.toHex(buffer));
  }

  @Override
  public String toString() {
    return super.toString() + String.format("%n%n%s", charset.toHex(data));
  }

}
