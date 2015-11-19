package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.commands.ReadPartitionQuery;
import com.bytezone.dm3270.display.Screen;

public class ReadPartitionSF extends StructuredField
{
  private final byte partitionID;
  private String typeName;
  private Command command;

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
          typeName = "Read Partition (Query)";
          command = new ReadPartitionQuery (buffer, offset, length);
          break;

        case (byte) 0x03:
          typeName = "Read Partition (QueryList)";
          command = new ReadPartitionQuery (buffer, offset, length);
          break;

        default:
          typeName = String.format ("Unknown READ PARTITION (Query) type: %02X", data[2]);
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
      System.out.println ("testing query reply");
    }
    else
    {
      command.process (screen);
      reply = command.getReply ();
      System.out.println ("testing command reply");
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
    text.append (String.format ("   type      : %02X %s", data[2], typeName));
    text.append (command);
    return text.toString ();
  }
}