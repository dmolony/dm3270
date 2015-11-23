package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.display.Screen;

public class Outbound3270DS extends StructuredField
{
  private final byte partitionID;
  private final Command command;

  // wrapper for original write commands - W. EW, EWA, EAU
  public Outbound3270DS (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);             // copies buffer -> data

    assert data[0] == StructuredField.OUTBOUND_3270DS;
    partitionID = data[1];
    assert (partitionID & (byte) 0x80) == 0;    // must be 0x00 - 0x7F

    // can only be W/EW/EWA/EAU (i.e. one of the write commands)
    command = Command.getCommand (buffer, offset + 2, length - 2);
  }

  @Override
  public void process (Screen screen)
  {
    command.process (screen);
    if (command.getReply ().isPresent ())
    {
      System.out.println ("Non-null reply:");
      System.out.println (command);
      System.out.println (command.getReply ().get ());     // should always be null
    }
  }

  @Override
  public String brief ()
  {
    return String.format ("Out3270: %s", command);
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("Struct Field : %02X Outbound3270DS\n", type));
    text.append (String.format ("   partition : %02X%n", partitionID));
    text.append (command);
    return text.toString ();
  }
}