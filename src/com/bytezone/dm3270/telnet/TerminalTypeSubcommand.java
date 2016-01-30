package com.bytezone.dm3270.telnet;

import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.streams.TelnetState;

public class TerminalTypeSubcommand extends TelnetSubcommand
{
  public static final byte OPTION_IS = 0;
  public static final byte OPTION_SEND = 1;

  private final String terminalType2 = "IBM-3278-2-E";
  private final String terminalType4 = "IBM-3278-4-E";

  public TerminalTypeSubcommand (byte[] buffer, int offset, int length,
      TelnetState telnetState)
  {
    super (buffer, offset, length, telnetState);

    if (buffer[3] == OPTION_IS)
    {
      type = SubcommandType.IS;
      value = new String (buffer, 4, length - 6);
    }
    else if (buffer[3] == OPTION_SEND)
    {
      type = SubcommandType.SEND;
      value = "";
    }
    else
      throw new InvalidParameterException (
          String.format ("Unknown subcommand type: %02X%n", buffer[3]));
  }

  @Override
  public void process (Screen screen)
  {
    if (type == SubcommandType.SEND)
    {
      try
      {
        byte[] header = { TelnetCommand.IAC, TelnetCommand.SB, TERMINAL_TYPE, OPTION_IS };
        byte[] terminal = terminalType2.getBytes ("ASCII");
        byte[] reply = new byte[header.length + terminal.length + 2];

        System.arraycopy (header, 0, reply, 0, header.length);
        System.arraycopy (terminal, 0, reply, header.length, terminal.length);

        reply[reply.length - 2] = TelnetCommand.IAC;
        reply[reply.length - 1] = TelnetCommand.SE;

        telnetState.setTerminal (terminalType2);

        setReply (new TerminalTypeSubcommand (reply, 0, reply.length, telnetState));
      }
      catch (UnsupportedEncodingException e)
      {
        e.printStackTrace ();
      }
    }
    else if (type == SubcommandType.IS)
    {
      //      System.out.println ("not written");
    }
  }

  @Override
  public String toString ()
  {
    switch (type)
    {
      case SEND:
        return type + " TerminalType";
      case IS:
        return type + " TerminalType " + terminalType2;
      default:
        return "SUB: " + "Unknown";
    }
  }
}