package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.structuredfields.StructuredField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadPartitionQuery extends Command {

  private static final Logger LOG = LoggerFactory.getLogger(ReadPartitionQuery.class);

  private String typeName;

  public ReadPartitionQuery(byte[] buffer, int offset, int length) {
    super(buffer, offset, length);

    assert data[0] == StructuredField.READ_PARTITION;
    assert data[1] == (byte) 0xFF;
  }

  @Override
  public void process(Screen screen) {
    if (getReply().isPresent()) {
      return;
    }

    switch (data[2]) {
      case (byte) 0x02:
        setReply(
            new ReadStructuredFieldCommand(screen.getTelnetState()));      // build a QueryReply
        typeName = "Read Partition (Query)";
        break;

      case (byte) 0x03:
        switch (data[3]) {
          case 0:
            LOG.warn("QCode List not written yet");
            break;

          case 1:
            LOG.warn("Equivalent + QCode List not written yet");
            break;

          case 2:
            setReply(
                new ReadStructuredFieldCommand(screen.getTelnetState()));      // build a QueryReply
            typeName = "Read Partition (QueryList)";
            break;

          default:
            LOG.warn("Unknown query type: {}", String.format("%02X", data[3]));
        }
        break;

      default:
        LOG.warn("Unknown ReadStructuredField type: {}", String.format("%02X", data[2]));
    }
  }

  @Override
  public String getName() {
    return typeName;
  }

  @Override
  public String toString() {
    return String.format("%s", typeName);
  }

}
