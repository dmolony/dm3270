package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.display.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Outbound3270DS extends StructuredField {

  private static final Logger LOG = LoggerFactory.getLogger(Outbound3270DS.class);

  private final byte partitionID;
  private final Command command;

  // wrapper for original write commands - W. EW, EWA, EAU
  public Outbound3270DS(byte[] buffer, int offset, int length) {
    super(buffer, offset, length);             // copies buffer -> data

    assert data[0] == StructuredField.OUTBOUND_3270DS;
    partitionID = data[1];
    assert (partitionID & (byte) 0x80) == 0;    // must be 0x00 - 0x7F

    // can only be W/EW/EWA/EAU (i.e. one of the write commands)
    command = Command.getCommand(buffer, offset + 2, length - 2);
  }

  @Override
  public void process(Screen screen) {
    command.process(screen);
    if (command.getReply().isPresent()) {
      LOG.debug("Non-null reply: {}, {}", command,
          command.getReply().get()); // reply should always be null
    }
  }

  @Override
  public String toString() {
    return String.format("Struct Field : %02X Outbound3270DS\n", type)
        + String.format("   partition : %02X%n", partitionID)
        + command;
  }

}
