package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.display.Screen;

// Inbound only command - creates a Reply of AID

public class ReadCommand extends Command
{
  private final String name;
  private final CommandType type;

  enum CommandType
  {
    READ_BUFFER, READ_MODIFIED, READ_MODIFIED_ALL
  }

  // Called from the static Command.getCommand()
  public ReadCommand (byte[] buffer, int offset, int length, Screen screen)
  {
    super (buffer, offset, length, screen);

    if (data[0] == READ_BUFFER_F2)
    {
      name = "Read Buffer";
      type = CommandType.READ_BUFFER;
    }
    else if (data[0] == READ_MODIFIED_F6)
    {
      name = "Read Modified";
      type = CommandType.READ_MODIFIED;
    }
    else if (data[0] == READ_MODIFIED_ALL_6E)
    {
      name = "Read Modified All";
      type = CommandType.READ_MODIFIED_ALL;
    }
    else
    {
      name = "Unknown Command";
      type = null;
    }
  }

  @Override
  public String getName ()
  {
    return name;
  }

  @Override
  public void process ()
  {
    // Create an AID command
    if (type == CommandType.READ_BUFFER)
      reply = screen.readBuffer ();
    else if (type == CommandType.READ_MODIFIED)
      reply = screen.readModifiedFields (READ_MODIFIED_F6);
    else if (type == CommandType.READ_MODIFIED_ALL)
      reply = screen.readModifiedFields (READ_MODIFIED_ALL_6E);
    else
      System.out.printf ("Unknown READ command: %02X%n", data[0]);
  }

  @Override
  public String toString ()
  {
    return getName ();
  }
}