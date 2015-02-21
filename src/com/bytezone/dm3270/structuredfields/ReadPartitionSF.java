package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.commands.ReadStructuredFieldCommand;

public class ReadPartitionSF extends StructuredField
{
  private final byte partitionID;
  private String typeName;

  public ReadPartitionSF (byte[] buffer, int offset, int length,
      ScreenHandler screenHandler)
  {
    super (buffer, offset, length, screenHandler);

    assert data[0] == StructuredField.READ_PARTITION;
    partitionID = data[1];

    switch (data[2])
    {
      case (byte) 0x02:
        typeName = "Read Partition (Query)";
        break;
      case (byte) 0x03:
        typeName = "Read Partition (QueryList)";
        break;
      case Command.READ_BUFFER_F2:        // NB 0x02 would conflict with RPQ above
        typeName = "Read Partition (ReadBuffer)";
        break;
      case Command.READ_MODIFIED_F6:
        typeName = "Read Partition (ReadModified)";
        break;
      case Command.READ_MODIFIED_ALL_6E:
        typeName = "Read Partition (ReadModifiedAll)";
        break;
      default:
        typeName = String.format ("Unknown READ PARTITION type: %02X", data[2]);
    }
  }

  @Override
  public void process ()
  {
    switch (data[2])
    {
      case (byte) 0x02:
        if (partitionID == (byte) 0xFF)           // query operation
          reply = new ReadStructuredFieldCommand (screenHandler);
        else
          System.out.printf ("Unknown %s pid: %02X%n", type, partitionID);
        break;

      case (byte) 0x03:
        if (partitionID == (byte) 0xFF)          // query operation
          switch (data[3])
          {
            case 0:
              System.out.println ("QCode List not written yet");
              break;

            case 1:
              System.out.println ("Equivalent + QCode List not written yet");
              break;

            case 2:
              reply = new ReadStructuredFieldCommand (screenHandler);
              break;

            default:
              System.out.printf ("Unknown %s: %02X%n", type, data[3]);
          }
        else
          System.out.printf ("Unknown %s pid: %02X%n", type, partitionID);
        break;

      case Command.READ_BUFFER_F2:        // NB 0x02 would conflict with RPQ above
        reply = new AIDCommand (screenHandler);
        break;

      case Command.READ_MODIFIED_F6:
        reply = new AIDCommand (screenHandler, AIDCommand.NO_AID_SPECIFIED);
        break;

      case Command.READ_MODIFIED_ALL_6E:
        reply = new AIDCommand (screenHandler, AIDCommand.NO_AID_SPECIFIED);
        break;

      default:
        System.out.printf ("Unknown ReadStructuredField type: %02X%n", data[2]);
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