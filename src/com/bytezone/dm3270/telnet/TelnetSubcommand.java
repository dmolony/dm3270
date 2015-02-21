package com.bytezone.dm3270.telnet;

import com.bytezone.dm3270.buffers.AbstractTelnetCommand;

public abstract class TelnetSubcommand extends AbstractTelnetCommand
{
  // subcommands
  public static final byte BINARY = 0x00;
  public static final byte TERMINAL_TYPE = 0x18;
  public static final byte EOR = 0x19;
  public static final byte TN3270E = 0x28;

  protected String subcommandName = "??";
  protected SubcommandType type;
  protected String value;

  public enum SubcommandType
  {
    SEND, IS, REPLY, NAME, DEVICE_TYPE, FUNCTIONS, REQUEST
  }

  public TelnetSubcommand (byte[] buffer, int offset, int length, TelnetState telnetState)
  {
    super (buffer, offset, length, telnetState);

    if (buffer[2] == TERMINAL_TYPE)
      subcommandName = "TERMINAL:";
  }

  public SubcommandType getSubcommandType ()
  {
    return type;
  }

  //  @Override
  public String getName ()
  {
    return toString ();
  }

  @Override
  public String toString ()
  {
    return String.format ("%s %s %s", subcommandName, type, (value == null ? "" : value));
  }
}