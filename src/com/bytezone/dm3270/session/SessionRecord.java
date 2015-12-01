package com.bytezone.dm3270.session;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.bytezone.dm3270.buffers.ReplyBuffer;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.extended.AbstractExtendedCommand;
import com.bytezone.dm3270.extended.TN3270ExtendedCommand;
import com.bytezone.dm3270.streams.TelnetSocket.Source;
import com.bytezone.dm3270.telnet.TelnetCommand;
import com.bytezone.dm3270.telnet.TelnetSubcommand;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SessionRecord
{
  private static final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern ("dd MMM uuuu HH:mm:ss.S");
  private static final DateTimeFormatter timeFormatter =
      DateTimeFormatter.ofPattern ("mm:ss");
  private final ReplyBuffer message;

  private final Source source;
  private final boolean genuine;
  private final SessionRecordType sessionRecordType;
  private final LocalDateTime dateTime;

  private StringProperty sourceName;
  private StringProperty commandType;
  private StringProperty commandName;
  private IntegerProperty bufferSize;
  private StringProperty time;

  public enum SessionRecordType
  {
    TELNET, TN3270, TN3270E
  }

  public SessionRecord (SessionRecordType sessionRecordType, ReplyBuffer message,
      Source source, LocalDateTime dateTime, boolean genuine)
  {
    this.sessionRecordType = sessionRecordType;
    this.message = message;
    this.source = source;
    this.dateTime = dateTime;
    this.genuine = genuine;

    if (genuine)
      setSourceName (source == Source.CLIENT ? "Client" : "Server");
    else
      setSourceName ("MITM-" + (source == Source.CLIENT ? "C" : "S"));

    switch (sessionRecordType)
    {
      case TELNET:
        setCommandType ("Telnet");
        break;
      case TN3270:
        setCommandType ("TN3270");
        break;
      case TN3270E:
        setCommandType ("Extended");
        break;
    }

    // create Interface (Identifiable?) with getName()
    if (message instanceof TelnetCommand)
      setCommandName (((TelnetCommand) message).getName ());
    else if (message instanceof TelnetSubcommand)
      setCommandName (((TelnetSubcommand) message).getName ());
    else if (message instanceof Command)
      setCommandName (((Command) message).getName ());
    else if (message instanceof TN3270ExtendedCommand)
      setCommandName (((TN3270ExtendedCommand) message).getName ());
    else if (message instanceof AbstractExtendedCommand)
      setCommandName (((AbstractExtendedCommand) message).getName ());

    setBufferSize (message.size ());
    if (dateTime != null)
      setTime (timeFormatter.format (dateTime));
  }

  public boolean isTelnet ()
  {
    return sessionRecordType == SessionRecordType.TELNET;
  }

  public boolean isTN3270 ()
  {
    return sessionRecordType == SessionRecordType.TN3270;
  }

  public boolean isTN3270Extended ()
  {
    return sessionRecordType == SessionRecordType.TN3270E;
  }

  public boolean isCommand ()
  {
    return message instanceof Command || message instanceof TN3270ExtendedCommand;
  }

  public Command getCommand ()
  {
    if (message instanceof Command)
      return (Command) message;
    if (message instanceof TN3270ExtendedCommand)
      return ((TN3270ExtendedCommand) message).getCommand ();
    return null;
  }

  public ReplyBuffer getMessage ()
  {
    return message;
  }

  public byte[] getBuffer ()
  {
    return message.getData ();
  }

  public int size ()
  {
    return message.size ();
  }

  public SessionRecordType getDataRecordType ()
  {
    return sessionRecordType;
  }

  public LocalDateTime getDateTime ()
  {
    return dateTime;
  }

  public boolean isGenuine ()
  {
    return genuine;
  }

  public Source getSource ()
  {
    return source;
  }

  // ---------------------------------------------------------------------------------//
  // Time
  // ---------------------------------------------------------------------------------//

  public void setTime (String value)
  {
    timeProperty ().set (value);
  }

  public String getTime ()
  {
    return commandNameProperty ().get ();
  }

  public StringProperty timeProperty ()
  {
    if (time == null)
      time = new SimpleStringProperty ();
    return time;
  }

  // ---------------------------------------------------------------------------------//
  // SourceName
  // ---------------------------------------------------------------------------------//

  public void setSourceName (String value)
  {
    sourceNameProperty ().set (value);
  }

  public String getSourceName ()
  {
    return sourceNameProperty ().get ();
  }

  public StringProperty sourceNameProperty ()
  {
    if (sourceName == null)
      sourceName = new SimpleStringProperty ();
    return sourceName;
  }

  // ---------------------------------------------------------------------------------//
  // CommandType
  // ---------------------------------------------------------------------------------//

  public void setCommandType (String value)
  {
    commandTypeProperty ().set (value);
  }

  public String getCommandType ()
  {
    return commandTypeProperty ().get ();
  }

  public StringProperty commandTypeProperty ()
  {
    if (commandType == null)
      commandType = new SimpleStringProperty ();
    return commandType;
  }

  // ---------------------------------------------------------------------------------//
  // CommandName
  // ---------------------------------------------------------------------------------//

  public void setCommandName (String value)
  {
    commandNameProperty ().set (value);
  }

  public String getCommandName ()
  {
    return commandNameProperty ().get ();
  }

  public StringProperty commandNameProperty ()
  {
    if (commandName == null)
      commandName = new SimpleStringProperty ();
    return commandName;
  }

  // ---------------------------------------------------------------------------------//
  // BufferSize
  // ---------------------------------------------------------------------------------//

  public void setBufferSize (int value)
  {
    bufferSizeProperty ().set (value);
  }

  public int getBufferSize ()
  {
    return bufferSizeProperty ().get ();
  }

  public IntegerProperty bufferSizeProperty ()
  {
    if (bufferSize == null)
      bufferSize = new SimpleIntegerProperty ();
    return bufferSize;
  }

  @Override
  public String toString ()
  {
    return String.format ("%s : %s", source, formatter.format (dateTime));
  }
}