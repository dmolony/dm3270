package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.display.Screen;

// Inbound only command - creates a Reply of AID 

public class ReadCommand extends Command
{
  public ReadCommand (ScreenHandler screenHandler, Screen screen, byte[] buffer,
      int offset, int length)
  {
    super (buffer, offset, length, screenHandler, screen);
  }

  @Override
  public String getName ()
  {
    return "Read Command";
  }

  @Override
  public void process ()
  {
    // the AID command will decide how to pack the RB, RM or RMA request
    reply = new AIDCommand (screenHandler, screen, data[0]);
  }

  @Override
  public String toString ()
  {
    return getName ();
  }
}