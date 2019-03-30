package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.utilities.Dm3270Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultStructuredField extends StructuredField {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultStructuredField.class);

  public DefaultStructuredField(byte[] buffer, int offset, int length) {
    super(buffer, offset, length);
    LOG.debug("Default Structured Field !!");
  }

  @Override
  public void process(Screen screen) {
    LOG.debug("Processing a DefaultStructuredField: {}", String.format("%02x", type));
  }

  @Override
  public String toString() {
    return String.format("Unknown SF   : %02X%n", data[0])
        + Dm3270Utility.toHex(data);
  }

}
