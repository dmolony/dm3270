package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.commands.ReadPartitionQuery;
import com.bytezone.dm3270.display.Screen;

public class ReadPartitionSF extends StructuredField
{
  private final byte partitionID;
  private final Command command;

  public ReadPartitionSF (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);

    assert data[0] == StructuredField.READ_PARTITION;
    partitionID = data[1];

    if (partitionID == (byte) 0xFF)
    {
      switch (data[2])
      {
        case (byte) 0x02:
        case (byte) 0x03:
          command = new ReadPartitionQuery (buffer, offset, length);
          break;

        default:
          command = null;
      }
    }
    else
    {
      // wrapper for original read commands - RB, RM, RMA
      assert (partitionID & (byte) 0x80) == 0;    // must be 0x00 - 0x7F

      // can only be RB/RM/RMA (i.e. one of the read commands)
      command = Command.getCommand (buffer, offset + 2, length - 2);
      System.out.println (command);
    }
  }

  @Override
  public void process (Screen screen)
  {
    if (reply != null)                // replay mode
      return;

    if (partitionID == (byte) 0xFF)
    {
      command.process (screen);
      reply = command.getReply ();
    }
    else
    {
      command.process (screen);
      reply = command.getReply ();
      System.out.println ("testing read command reply");
    }
  }

  @Override
  public String brief ()
  {
    return String.format ("ReadPT: %s", reply);
  }

  @Override
  public String toString ()
  {
    StringBuilder text =
        new StringBuilder (String.format ("Struct Field : 01 Read Partition\n"));
    text.append (String.format ("   partition : %02X%n", partitionID));
    text.append (command);
    return text.toString ();
  }
}