package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.display.Screen;

public class Outbound3270DS extends StructuredField
{
  private final byte partition;
  private final Command command;

  public Outbound3270DS (byte[] buffer, int offset, int length,
      ScreenHandler screenHandler, Screen screen)
  {
    super (buffer, offset, length, screenHandler, screen);   // copies buffer -> data

    assert data[0] == StructuredField.OUTBOUND_3270DS;
    partition = data[1];

    // can only be W/EW/EWA/EAU (i.e. one of the write commands)
    command = Command.getCommand (buffer, offset + 2, length - 2, screenHandler, screen);
  }

  @Override
  public void process ()
  {
    command.process ();
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
    text.append (String.format ("   partition : %02X%n", partition));
    text.append (command);
    return text.toString ();
  }
}