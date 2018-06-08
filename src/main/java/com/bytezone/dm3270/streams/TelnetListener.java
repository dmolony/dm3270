package com.bytezone.dm3270.streams;

import com.bytezone.dm3270.buffers.Buffer;
import com.bytezone.dm3270.buffers.ReplyBuffer;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.extended.BindCommand;
import com.bytezone.dm3270.extended.CommandHeader;
import com.bytezone.dm3270.extended.ResponseCommand;
import com.bytezone.dm3270.extended.TN3270ExtendedCommand;
import com.bytezone.dm3270.extended.UnbindCommand;
import com.bytezone.dm3270.session.SessionRecord;
import com.bytezone.dm3270.telnet.TN3270ExtendedSubcommand;
import com.bytezone.dm3270.telnet.TelnetCommand;
import com.bytezone.dm3270.telnet.TelnetCommandProcessor;
import com.bytezone.dm3270.telnet.TelnetProcessor;
import com.bytezone.dm3270.telnet.TelnetSubcommand;
import com.bytezone.dm3270.telnet.TerminalTypeSubcommand;
import com.bytezone.dm3270.utilities.Dm3270Utility;

import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelnetListener implements BufferListener, TelnetCommandProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(TelnetListener.class);

  private final TelnetSocket.Source source;

  private final TelnetState telnetState;
  private final Screen screen;

  private final TelnetProcessor telnetProcessor = new TelnetProcessor(this);

  // Use this when not recording the session and running in TERMINAL mode.
  public TelnetListener(Screen screen, TelnetState telnetState) {
    this.screen = screen;
    this.telnetState = telnetState;

    this.source = TelnetSocket.Source.SERVER;                  // listening to a server
  }

  // This method is always called with a copy of the original buffer. It can be
  // called from a background thread, so any GUI calls must be placed on the EDT.
  // Converts buffer arrays to Messages.

  @Override
  public synchronized void listen(TelnetSocket.Source source, byte[] buffer, LocalDateTime dateTime,
      boolean genuine) {
    assert source == this.source : "Incorrect source: " + source + ", expecting: "
        + this.source;

    telnetProcessor.listen(buffer);     // will call one of the processXXX routines

    telnetState.setLastAccess();
  }

  @Override
  public void close() {
  }

  @Override
  public void processData(byte[] buffer, int length) {
    LOG.warn("Unknown telnet data received: {}", Dm3270Utility.toHex(buffer, 0, length, false));
  }

  @Override
  public void processRecord(byte[] data, int dataPtr) {
    int offset;
    int length;
    CommandHeader.DataType dataType;

    CommandHeader currentCommandHeader;
    if (telnetState.does3270Extended()) {
      offset = 5;
      length = dataPtr - 7;         // exclude IAC/EOR and header
      currentCommandHeader = new CommandHeader(data, 0, 5);
      dataType = currentCommandHeader.getDataType();
    } else {
      offset = 0;
      length = dataPtr - 2;         // exclude IAC/EOR
      currentCommandHeader = null;
      dataType = CommandHeader.DataType.TN3270_DATA;
    }

    switch (dataType) {
      case TN3270_DATA:
        ReplyBuffer command;
        if (source == TelnetSocket.Source.SERVER) {
          command = Command.getCommand(data, offset, length);
        } else {
          command = Command.getReply(data, offset, length);
        }

        if (currentCommandHeader != null) {
          command = new TN3270ExtendedCommand(currentCommandHeader, (Command) command);
        }
        addDataRecord(command, SessionRecord.SessionRecordType.TN3270);
        break;

      case BIND_IMAGE:
        BindCommand bindCommand =
            new BindCommand(currentCommandHeader, data, offset, length);
        addDataRecord(bindCommand, SessionRecord.SessionRecordType.TN3270E);
        break;

      case UNBIND:
        UnbindCommand unbindCommand =
            new UnbindCommand(currentCommandHeader, data, offset, length);
        addDataRecord(unbindCommand, SessionRecord.SessionRecordType.TN3270E);
        break;

      case RESPONSE:
        ResponseCommand responseCommand =
            new ResponseCommand(currentCommandHeader, data, offset, length);
        addDataRecord(responseCommand, SessionRecord.SessionRecordType.TN3270E);
        break;

      default:
        LOG.warn("Data type not written: {}, {}", dataType,
            Dm3270Utility.toHex(data, offset, length));
    }
  }

  @Override
  public void processTelnetCommand(byte[] data, int dataPtr) {
    TelnetCommand telnetCommand = new TelnetCommand(telnetState, data, dataPtr);
    addDataRecord(telnetCommand, SessionRecord.SessionRecordType.TELNET);
    telnetCommand.process(screen);       // updates TelnetState
  }

  @Override
  public void processTelnetSubcommand(byte[] data, int dataPtr) {
    TelnetSubcommand subcommand = null;

    if (data[2] == TelnetSubcommand.TERMINAL_TYPE) {
      subcommand = new TerminalTypeSubcommand(data, 0, dataPtr, telnetState);
    } else if (data[2] == TelnetSubcommand.TN3270E) {
      subcommand = new TN3270ExtendedSubcommand(data, 0, dataPtr, telnetState);
    } else {
      LOG.warn("Unknown command type : {}", Dm3270Utility.toHex(data, 2, 1, false));
    }

    addDataRecord(subcommand, SessionRecord.SessionRecordType.TELNET);
  }

  private void addDataRecord(ReplyBuffer message,
      SessionRecord.SessionRecordType sessionRecordType) {
    // no gui involved
    if (sessionRecordType == SessionRecord.SessionRecordType.TELNET) {
      processMessage(message);
    } else {
      //TODO should we run this in a separate thread?
      processMessage(message);
    }
  }

  private void processMessage(ReplyBuffer message) {
    message.process(screen);
    Optional<Buffer> reply = message.getReply();
    reply.ifPresent(buffer -> telnetState.write(buffer.getTelnetData()));
  }

}
