package com.bytezone.dm3270.streams;

import java.time.LocalDateTime;

import javafx.application.Platform;

import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.application.TelnetState;
import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.buffers.Buffer;
import com.bytezone.dm3270.buffers.ReplyBuffer;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.extended.BindCommand;
import com.bytezone.dm3270.extended.CommandHeader;
import com.bytezone.dm3270.extended.CommandHeader.DataType;
import com.bytezone.dm3270.extended.ResponseCommand;
import com.bytezone.dm3270.extended.TN3270ExtendedCommand;
import com.bytezone.dm3270.extended.UnbindCommand;
import com.bytezone.dm3270.session.Session;
import com.bytezone.dm3270.session.Session.SessionMode;
import com.bytezone.dm3270.session.SessionRecord;
import com.bytezone.dm3270.session.SessionRecord.SessionRecordType;
import com.bytezone.dm3270.streams.TelnetSocket.Source;
import com.bytezone.dm3270.telnet.TN3270ExtendedSubcommand;
import com.bytezone.dm3270.telnet.TelnetCommand;
import com.bytezone.dm3270.telnet.TelnetSubcommand;
import com.bytezone.dm3270.telnet.TerminalTypeSubcommand;

public class TelnetListener implements BufferListener
{
  private final Session session;
  private final Source source;

  // convenience variables obtained from the Session parameter
  private final TelnetState telnetState;
  private final ScreenHandler screenHandler;
  private final Screen screen;
  private final SessionMode sessionMode;

  // current state of the transmission
  private final byte[] data = new byte[4096];
  private int dataPtr;
  private byte lastByte;

  private CommandHeader currentCommandHeader;
  private LocalDateTime currentDateTime;
  private boolean currentGenuine;

  public TelnetListener (Source source, Session session)
  {
    this.session = session;
    this.source = source;

    this.screenHandler = session.getScreenHandler ();
    this.screen = session.getScreen ();
    this.telnetState = session.getTelnetState ();
    this.sessionMode = session.getSessionMode ();
  }

  // This method is always called with a copy of the original buffer. It can be
  // called from a background thread, so any GUI calls must be placed on the EDT.
  // Converts buffer arrays to Messages.

  // Can be called from SessionBuilder when recreating a session from a file.
  // Can be called from a SocketListener thread whilst eavesdropping.
  // Probably from a Console thread too (or does it use a SocketListener?)

  @Override
  public synchronized void listen (Source source, byte[] buffer, LocalDateTime dateTime,
      boolean genuine)
  {
    assert source == this.source : "Incorrect source: " + source + ", expecting: "
        + this.source;

    currentDateTime = dateTime;
    currentGenuine = genuine;

    int ptr = 0;

    while (ptr < buffer.length)
    {
      byte thisByte = buffer[ptr++];
      data[dataPtr++] = thisByte;

      // Check for double-IAC
      if (lastByte == TelnetCommand.IAC && thisByte == TelnetCommand.IAC)
      {
        lastByte = 0;     // ignore the second 0xFF, and put dataPtr back
        --dataPtr;
      }

      // Check for end of record - signifies a 3270 data stream
      else if (lastByte == TelnetCommand.IAC && thisByte == TelnetCommand.EOR)
        do3270Command ();

      // Check for end of subcommand
      else if (lastByte == TelnetCommand.IAC && thisByte == TelnetCommand.SE)
        doTelnetSubcommand ();

      // Check for any 3-byte telnet command that is not a subcommand
      else if (dataPtr == 3                 //
          && data[0] == TelnetCommand.IAC   //
          && data[1] != TelnetCommand.SB)
        doTelnetCommand ();

      // check for a known 2-byte telnet command
      else if (dataPtr == 2                 //
          && data[0] == TelnetCommand.IAC
          && (thisByte == TelnetCommand.IP || thisByte == TelnetCommand.NOP))
        doTelnetCommand ();

      else
        lastByte = thisByte;
    }
  }

  private void doTelnetCommand ()
  {
    TelnetCommand telnetCommand = new TelnetCommand (telnetState, data, dataPtr);
    telnetCommand.process ();       // updates TelnetState

    if (telnetCommand.commandName () != TelnetCommand.CommandName.SUBCOMMAND)
      addDataRecord (telnetCommand, SessionRecordType.TELNET);
    else
      System.out.println ("Unexpected telnet command: " + telnetCommand);

    lastByte = 0;
    dataPtr = 0;
  }

  private void doTelnetSubcommand ()
  {
    TelnetSubcommand subcommand;

    switch (data[2])
    {
      case TelnetSubcommand.TERMINAL_TYPE:
        subcommand = new TerminalTypeSubcommand (data, 0, dataPtr, telnetState);
        subcommand.process ();
        addDataRecord (subcommand, SessionRecordType.TELNET);
        break;

      case TelnetSubcommand.TN3270E:
        subcommand = new TN3270ExtendedSubcommand (data, 0, dataPtr, telnetState);
        subcommand.process ();
        addDataRecord (subcommand, SessionRecordType.TELNET);
        break;

      default:
        System.out.printf ("Unknown command type : %02X%n" + data[2]);
    }

    lastByte = 0;
    dataPtr = 0;
  }

  private void do3270Command ()
  {
    int offset;
    int length;
    DataType dataType;

    if (telnetState.does3270Extended ())
    {
      offset = 5;
      length = dataPtr - 7;         // exclude IAC/EOR and header
      currentCommandHeader = new CommandHeader (data, 0, 5);
      dataType = currentCommandHeader.getDataType ();
    }
    else
    {
      offset = 0;
      length = dataPtr - 2;         // exclude IAC/EOR
      currentCommandHeader = null;
      dataType = DataType.TN3270_DATA;
    }

    switch (dataType)
    {
      case TN3270_DATA:
        ReplyBuffer command =
            source == Source.SERVER ? Command.getCommand (data, offset, length,
                                                          screenHandler, screen)
                : Command.getReply (screenHandler, screen, data, offset, length);
        if (currentCommandHeader != null)
          command = new TN3270ExtendedCommand (currentCommandHeader, (Command) command);
        addDataRecord (command, SessionRecordType.TN3270);
        break;

      case BIND_IMAGE:
        BindCommand bindCommand =
            new BindCommand (currentCommandHeader, data, offset, length);
        addDataRecord (bindCommand, SessionRecordType.TN3270E);
        break;

      case UNBIND:
        UnbindCommand unbindCommand =
            new UnbindCommand (currentCommandHeader, data, offset, length);
        addDataRecord (unbindCommand, SessionRecordType.TN3270E);
        break;

      case RESPONSE:
        ResponseCommand responseCommand =
            new ResponseCommand (currentCommandHeader, data, offset, length);
        addDataRecord (responseCommand, SessionRecordType.TN3270E);
        break;

      default:
        System.out.println ("Data type not written: " + dataType);
        System.out.println (Utility.toHex (data, offset, length));
    }

    lastByte = 0;
    dataPtr = 0;
  }

  private void addDataRecord (ReplyBuffer message, SessionRecordType sessionRecordType)
  {
    SessionRecord sessionRecord =
        new SessionRecord (sessionRecordType, message, source, currentDateTime,
            currentGenuine);

    // add the DataRecord to the Session - is it OK to do this from a non-EDT?
    session.add (sessionRecord);

    if (sessionMode == SessionMode.TERMINAL)     // if not replying then we're done
    {
      Platform.runLater ( () -> {
        message.process ();
        Buffer reply = message.getReply ();
        if (reply != null)
          telnetState.write (reply.getTelnetData ());
        //        else
        //          System.out.println ("no reply");
        });
    }
  }
}