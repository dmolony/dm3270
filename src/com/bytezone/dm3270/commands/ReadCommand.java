package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.display.Screen;

// Inbound only command - creates a Reply of AID 

public class ReadCommand extends Command
{
  String name;

  public ReadCommand (Screen screen, byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length, screen);

    if (data[0] == READ_BUFFER_F2)
      name = "Read Buffer";
    else if (data[0] == READ_MODIFIED_F6)
      name = "Read Modified";
    else if (data[0] == READ_MODIFIED_ALL_6E)
      name = "Read Modified All";
    else
      name = "Unknown Command";
  }

  @Override
  public String getName ()
  {
    return name;
  }

  @Override
  public void process ()
  {
    if (data[0] == READ_BUFFER_F2)
      reply = screen.readBuffer ();
    else if (data[0] == READ_MODIFIED_F6)
    {
      screen.setAID (AIDCommand.NO_AID_SPECIFIED);
      reply = screen.readModifiedFields ((byte) 0xF6);
    }
    else if (data[0] == READ_MODIFIED_ALL_6E)
    {
      System.out.println ("Read Modified All");
      reply = screen.readModifiedFields ((byte) 0x6E);
    }
    else
      System.out.printf ("Unknown READ command: %02X%n", data[0]);
  }

  @Override
  public String toString ()
  {
    return getName ();
  }
}