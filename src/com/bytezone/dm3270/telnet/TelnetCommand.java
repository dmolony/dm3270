package com.bytezone.dm3270.telnet;

import java.security.InvalidParameterException;

import com.bytezone.dm3270.buffers.AbstractTelnetCommand;

public class TelnetCommand extends AbstractTelnetCommand
{
  public static final byte IAC = (byte) 0xFF;   // Is a telnet command

  // double-byte commands
  public static final byte WILL = (byte) 0xFB;
  public static final byte WONT = (byte) 0xFC;
  public static final byte DO = (byte) 0xFD;
  public static final byte DONT = (byte) 0xFE;

  // single-byte commands
  public static final byte EOR = (byte) 0xEF;   // End of record
  public static final byte SE = (byte) 0xF0;    // End of subcommmand
  public static final byte NOP = (byte) 0xF1;   // No Operation
  public static final byte IP = (byte) 0xF4;    // Interrupt process
  public static final byte SB = (byte) 0xFA;    // Begin subcommand

  private final CommandName commandName;
  private final CommandType commandType;

  public enum CommandName
  {
    DO, DONT, WILL, WONT, SUBCOMMAND, NO_OP, INTERRUPT_PROCESS, END_SUBCOMMAND,
    END_RECORD
  }

  public enum CommandType
  {
    TERMINAL_TYPE, EOR, BINARY, TN3270_EXTENDED
  }

  public TelnetCommand (TelnetState state, byte[] buffer)
  {
    this (state, buffer, buffer.length);
  }

  public TelnetCommand (TelnetState state, byte[] buffer, int length)
  {
    super (buffer, 0, length, state);

    byte command = buffer[1];

    if (length == 2)
    {
      if (command == NOP)
        this.commandName = CommandName.NO_OP;
      else if (command == IP)
        this.commandName = CommandName.INTERRUPT_PROCESS;
      else
        throw new InvalidParameterException (
            String.format ("Unknown telnet command: %02X%n", command));

      commandType = null;
    }
    else if (length == 3)
    {
      byte type = buffer[2];

      if (command == DO)
        this.commandName = CommandName.DO;
      else if (command == DONT)
        this.commandName = CommandName.DONT;
      else if (command == WILL)
        this.commandName = CommandName.WILL;
      else if (command == WONT)
        this.commandName = CommandName.WONT;
      else
        throw new InvalidParameterException (
            String.format ("Unknown telnet command: %02X %02X%n", command, type));

      if (type == TelnetSubcommand.TERMINAL_TYPE)
        commandType = CommandType.TERMINAL_TYPE;
      else if (type == TelnetSubcommand.EOR)
        commandType = CommandType.EOR;
      else if (type == TelnetSubcommand.BINARY)
        commandType = CommandType.BINARY;
      else if (type == TelnetSubcommand.TN3270E)
        commandType = CommandType.TN3270_EXTENDED;
      else
        throw new InvalidParameterException (
            String.format ("Unknown telnet command type: %02X %02X%n", command, type));
    }
    else
      throw new InvalidParameterException ("Buffer incorrect length");
  }

  public CommandName commandName ()
  {
    return commandName;
  }

  public CommandType commandType ()
  {
    return commandType;
  }

  @Override
  public void process ()
  {
    if (commandName == CommandName.DO)
    {
      byte[] reply = new byte[3];

      reply[0] = IAC;
      if (commandType == CommandType.TERMINAL_TYPE || commandType == CommandType.EOR
          || commandType == CommandType.BINARY
          || commandType == CommandType.TN3270_EXTENDED)
      {
        reply[1] = WILL;

        if (commandType == CommandType.BINARY)
          telnetState.setDoesBinary (true);
        if (commandType == CommandType.TN3270_EXTENDED)
          telnetState.setDoes3270Extended (true);
        if (commandType == CommandType.EOR)
          telnetState.setDoesEOR (true);
      }
      else
        reply[1] = WONT;

      reply[2] = data[2];
      this.reply = new TelnetCommand (telnetState, reply);
    }
    else if (commandName == CommandName.WILL)
    {
      byte[] reply = new byte[3];
      reply[0] = IAC;
      if (commandType == CommandType.TERMINAL_TYPE || commandType == CommandType.EOR
          || commandType == CommandType.BINARY
          || commandType == CommandType.TN3270_EXTENDED)
        reply[1] = DO;
      else
        reply[1] = DONT;

      reply[2] = data[2];
      this.reply = new TelnetCommand (telnetState, reply);
    }
    else if (commandName == CommandName.DONT || commandName == CommandName.WONT)
    {
      if (commandType == CommandType.BINARY)
        telnetState.setDoesBinary (false);
      if (commandType == CommandType.TN3270_EXTENDED)
        telnetState.setDoes3270Extended (false);
      if (commandType == CommandType.EOR)
        telnetState.setDoesEOR (false);
    }
  }

  public String getName ()
  {
    if (commandName == CommandName.NO_OP)
      return "NoOp";
    return toString ();
  }

  @Override
  public String toString ()
  {
    return String.format ("%s %s", commandName, (commandType == null ? "" : commandType));
  }
}