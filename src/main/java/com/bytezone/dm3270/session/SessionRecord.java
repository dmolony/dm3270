package com.bytezone.dm3270.session;

import com.bytezone.dm3270.buffers.ReplyBuffer;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.extended.TN3270ExtendedCommand;
import com.bytezone.dm3270.streams.TelnetSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SessionRecord {

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("dd MMM uuuu HH:mm:ss.S");
  private final ReplyBuffer message;

  private final TelnetSocket.Source source;
  private final LocalDateTime dateTime;

  public enum SessionRecordType {
    TELNET, TN3270, TN3270E
  }

  public SessionRecord(ReplyBuffer message, TelnetSocket.Source source, LocalDateTime dateTime) {
    this.message = message;
    this.source = source;
    this.dateTime = dateTime;
  }

  public boolean isCommand() {
    return message instanceof Command || message instanceof TN3270ExtendedCommand;
  }

  public Command getCommand() {
    if (message instanceof Command) {
      return (Command) message;
    }
    if (message instanceof TN3270ExtendedCommand) {
      return ((TN3270ExtendedCommand) message).getCommand();
    }
    return null;
  }

  public byte[] getBuffer() {
    return message.getData();
  }

  public int size() {
    return message.size();
  }

  @Override
  public String toString() {
    return String.format("%s : %s", source, FORMATTER.format(dateTime));
  }

}
