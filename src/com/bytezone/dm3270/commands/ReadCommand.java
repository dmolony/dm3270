package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.application.ScreenHandler;

// Inbound only command - creates a Reply of AID 

public class ReadCommand extends Command
{
  public ReadCommand (ScreenHandler screenHandler, byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length, screenHandler);
  }

  @Override
  public String getName ()
  {
    return "Read Command";
  }

  @Override
  public void process ()
  {
    reply = new AIDCommand (screenHandler, data[0]);
    //    switch (data[0])
    //    {
    //      case Command.READ_BUFFER_F2:
    //      case Command.READ_BUFFER_02:
    //        reply = new AIDCommand (screenHandler);
    //        break;
    //
    //      case Command.READ_MODIFIED_F6:
    //      case Command.READ_MODIFIED_06:
    //        reply = new AIDCommand (screenHandler, AIDCommand.NO_AID_SPECIFIED);
    //        break;
    //
    //      case Command.READ_MODIFIED_ALL_6E:
    //      case Command.READ_MODIFIED_ALL_0E:
    //        reply = new AIDCommand (screenHandler, AIDCommand.NO_AID_SPECIFIED);
    //        break;
    //
    //      default:
    //        System.out.printf ("Unknown value: %02X%n", data[0]);
    //    }
  }

  @Override
  public String toString ()
  {
    return getName ();
  }
}