package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.display.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadCommand extends Command {

  private static final Logger LOG = LoggerFactory.getLogger(ReadCommand.class);

  private final String name;
  private final CommandType type;

  private enum CommandType {
    READ_BUFFER, READ_MODIFIED, READ_MODIFIED_ALL
  }

  public ReadCommand(byte[] buffer, int offset, int length) {
    super(buffer, offset, length);

    assert buffer[offset] == Command.READ_BUFFER_02
        || buffer[offset] == Command.READ_BUFFER_F2
        || buffer[offset] == Command.READ_MODIFIED_06
        || buffer[offset] == Command.READ_MODIFIED_F6
        || buffer[offset] == Command.READ_MODIFIED_ALL_0E
        || buffer[offset] == Command.READ_MODIFIED_ALL_6E;

    switch (data[0]) {
      case READ_BUFFER_F2:
      case READ_BUFFER_02:
        name = "Read Buffer";
        type = CommandType.READ_BUFFER;
        break;

      case READ_MODIFIED_F6:
      case READ_MODIFIED_06:
        name = "Read Modified";
        type = CommandType.READ_MODIFIED;
        break;

      case READ_MODIFIED_ALL_6E:
      case READ_MODIFIED_ALL_0E:
        name = "Read Modified All";
        type = CommandType.READ_MODIFIED_ALL;
        break;

      default:
        name = "Not found";
        type = null;
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void process(Screen screen) {
    // Create an AID command
    if (type == CommandType.READ_BUFFER) {
      setReply(screen.readBuffer());
    } else if (type == CommandType.READ_MODIFIED) {
      setReply(screen.readModifiedFields(READ_MODIFIED_F6));
    } else if (type == CommandType.READ_MODIFIED_ALL) {
      setReply(screen.readModifiedFields(READ_MODIFIED_ALL_6E));
    } else {
      LOG.warn("Unknown READ command: {}", String.format("%02X", data[0]));
    }
  }

  @Override
  public String toString() {
    return name;
  }

}
