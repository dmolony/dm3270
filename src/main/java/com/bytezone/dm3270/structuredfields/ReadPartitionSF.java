package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.Charset;
import com.bytezone.dm3270.buffers.Buffer;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.commands.ReadPartitionQuery;
import com.bytezone.dm3270.display.Screen;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadPartitionSF extends StructuredField {

  private static final Logger LOG = LoggerFactory.getLogger(ReadPartitionSF.class);

  private final byte partitionID;
  private final Command command;

  public ReadPartitionSF(byte[] buffer, int offset, int length, Charset charset) {
    super(buffer, offset, length, charset);

    assert data[0] == StructuredField.READ_PARTITION;
    partitionID = data[1];

    if (partitionID == (byte) 0xFF) {
      switch (data[2]) {
        case (byte) 0x02:
        case (byte) 0x03:
          command = new ReadPartitionQuery(buffer, offset, length, charset);
          break;

        default:
          command = null;
      }
    } else {
      // wrapper for original read commands - RB, RM, RMA
      assert (partitionID & (byte) 0x80) == 0;    // must be 0x00 - 0x7F

      // can only be RB/RM/RMA (i.e. one of the read commands)
      command = Command.getCommand(buffer, offset + 2, length - 2, charset);
      LOG.debug("RB/RM/RMA: {}", command);
    }
  }

  @Override
  public void process(Screen screen) {
    // replay mode
    if (getReply().isPresent()) {
      return;
    }

    if (partitionID == (byte) 0xFF) {
      command.process(screen);
      Optional<Buffer> opt = command.getReply();
      if (opt.isPresent()) {
        setReply(opt.get());
      } else {
        setReply(null);
      }
    } else {
      command.process(screen);
      Optional<Buffer> opt = command.getReply();
      if (opt.isPresent()) {
        setReply(opt.get());
      } else {
        setReply(null);
      }
      LOG.debug("testing read command reply");
    }
  }

  @Override
  public String toString() {
    return "Struct Field : 01 Read Partition\n" + String
        .format("   partition : %02X%n", partitionID)
        + command;
  }

}
