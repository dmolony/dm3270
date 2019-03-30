package com.bytezone.dm3270.extended;

import com.bytezone.dm3270.utilities.Dm3270Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseCommand extends AbstractExtendedCommand {

  private static final Logger LOG = LoggerFactory.getLogger(ResponseCommand.class);

  public ResponseCommand(CommandHeader commandHeader, byte[] buffer, int offset,
      int length) {
    super(commandHeader, buffer, offset, length);

    if (length != 1) {
      LOG.debug(Dm3270Utility.toHex(buffer, offset, length));
    }
  }

  @Override
  public String getName() {
    return "Response";
  }

  @Override
  public String toString() {
    return String.format("Rsp: %02X", data[0]);
  }

}
