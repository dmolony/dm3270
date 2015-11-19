package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.commands.ReadStructuredFieldCommand;
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
          break;

        case (byte) 0x03:
          typeName = "Read Partition (QueryList)";
          break;

        default:
          typeName = String.format ("Unknown READ PARTITION (Query) type: %02X", data[2]);
      }
    }
    else
    {
      // wrapper for original read commands - RB, RM, RMA
      assert (partitionID & (byte) 0x80) == 0;    // must be 0x00 - 0x7F
      //      switch (data[2])
      //      {
      //        case Command.READ_BUFFER_F2:
      //        case Command.READ_BUFFER_02:
      //          typeName = "Read Partition (ReadBuffer)";
      //          break;
      //
      //        case Command.READ_MODIFIED_F6:
      //        case Command.READ_MODIFIED_06:
      //          typeName = "Read Partition (ReadModified)";
      //          break;
      //
      //        case Command.READ_MODIFIED_ALL_6E:
      //        case Command.READ_MODIFIED_ALL_0E:
      //          typeName = "Read Partition (ReadModifiedAll)";
      //          break;
      //
      //        default:
      //          typeName = String.format ("Unknown READ PARTITION type: %02X", data[2]);
      //      }

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
      switch (data[2])
      {
        case (byte) 0x02:
          if (partitionID == (byte) 0xFF)                   // query operation
            reply = new ReadStructuredFieldCommand ();      // build a QueryReply
          else
            System.out.printf ("Unknown %s pid: %02X%n", type, partitionID);
          break;

        case (byte) 0x03:
          if (partitionID == (byte) 0xFF)                       // query operation
            switch (data[3])
            {
              case 0:
                System.out.println ("QCode List not written yet");
                break;

              case 1:
                System.out.println ("Equivalent + QCode List not written yet");
                break;

              case 2:
                reply = new ReadStructuredFieldCommand ();      // build a QueryReply
                break;

              default:
                System.out.printf ("Unknown %s: %02X%n", type, data[3]);
            }
          else
            System.out.printf ("Unknown %s pid: %02X%n", type, partitionID);
          break;

        default:
          System.out.printf ("Unknown ReadStructuredField type: %02X%n", data[2]);
      }
    }
    else
    {
      //      switch (data[2])
      //      {
      //        case Command.READ_BUFFER_F2:
      //          reply = screen.readBuffer ();                     // AID command
      //          break;
      //
      //        case Command.READ_MODIFIED_F6:
      //        case Command.READ_MODIFIED_ALL_6E:
      //          reply = screen.readModifiedFields (data[2]);      // AID command
      //          break;
      //      }
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
    return text.toString ();
  }
}