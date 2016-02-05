package com.bytezone.dm3270.session;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.bytezone.dm3270.application.Console.Function;
import com.bytezone.dm3270.buffers.ReplyBuffer;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.commands.ReadStructuredFieldCommand;
import com.bytezone.dm3270.commands.WriteCommand;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.extended.TN3270ExtendedCommand;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.orders.TextOrder;
import com.bytezone.dm3270.session.SessionRecord.SessionRecordType;
import com.bytezone.dm3270.streams.TelnetListener;
import com.bytezone.dm3270.streams.TelnetSocket.Source;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.utilities.Dm3270Utility;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;

public class Session implements Iterable<SessionRecord>
{
  private final ObservableList<SessionRecord> sessionRecords =
      FXCollections.observableArrayList ();
  private final Function function;
  private final TelnetState telnetState;

  private String clientName = null;
  private String serverName = null;
  private final List<String> labels = new ArrayList<> ();
  private boolean safeFlag;
  private final Label headerLabel = new Label ();
  private ScreenDimensions screenDimensions;

  // called by SpyPane constructor
  public Session (TelnetState telnetState)
  {
    this.function = Function.SPY;
    this.telnetState = telnetState;
  }

  // called by MainframeStage constructor
  public Session (TelnetState telnetState, List<String> lines) throws Exception
  {
    function = Function.TEST;
    this.telnetState = telnetState;

    SessionReader server = new SessionReader (Source.SERVER, lines);
    SessionReader client = new SessionReader (Source.CLIENT, lines);

    init (client, server);
  }

  // called by Console.startSelectedFunction()
  public Session (TelnetState telnetState, Path path) throws Exception
  {
    function = Function.REPLAY;
    this.telnetState = telnetState;

    SessionReader server = new SessionReader (Source.SERVER, path);
    SessionReader client = new SessionReader (Source.CLIENT, path);

    init (client, server);
  }

  private void init (SessionReader client, SessionReader server) throws Exception
  {
    TelnetListener clientTelnetListener =
        new TelnetListener (Source.CLIENT, this, function, null, telnetState);
    TelnetListener serverTelnetListener =
        new TelnetListener (Source.SERVER, this, function, null, telnetState);

    while (client.nextLineNo () != server.nextLineNo ())
      if (client.nextLineNo () < server.nextLineNo ())
        while (client.nextLineNo () < server.nextLineNo ())
          clientTelnetListener.listen (Source.CLIENT, client.nextBuffer (),
                                       client.getDateTime (), client.isGenuine ());
      else
        while (client.nextLineNo () > server.nextLineNo ())
        {
          byte[] buffer = server.nextBuffer ();
          serverTelnetListener.listen (Source.SERVER, buffer, server.getDateTime (),
                                       server.isGenuine ());
          if (buffer[buffer.length - 2] == (byte) 0xFF
              && buffer[buffer.length - 1] == (byte) 0xEF)
            labels.add (server.getLabel ());
        }
  }

  public List<String> getLabels ()
  {
    return labels;
  }

  public int size ()
  {
    return sessionRecords.size ();
  }

  public String getClientName ()
  {
    return clientName == null ? "Unknown" : clientName;
  }

  public String getServerName ()
  {
    return serverName == null ? "Unknown" : serverName;
  }

  public ScreenDimensions getScreenDimensions ()
  {
    return screenDimensions;
  }

  public Label getHeaderLabel ()
  {
    return headerLabel;
  }

  // called from MainframeStage.prepareButtons()
  // called from TelnetListener.addDataRecord()
  public synchronized void add (SessionRecord sessionRecord)
  {
    if (sessionRecord == null)
      throw new IllegalArgumentException ("DataRecord is null");

    sessionRecords.add (sessionRecord);       // should this be concurrent?

    // this code checks to see whether it can identify the client and/or server
    if (function != Function.TERMINAL && sessionRecord.isCommand ())
    {
      switch (sessionRecord.getSource ())
      {
        case CLIENT:
          if (clientName == null)
            checkClientName (sessionRecord.getCommand ());
          if (screenDimensions == null)
            checkScreenDimensions (sessionRecord.getCommand ());
          break;

        case SERVER:
          if (serverName == null)
            checkServerName (sessionRecord.getCommand ());
          break;
      }
    }
  }

  private void checkScreenDimensions (Command command)
  {
    if ((command instanceof ReadStructuredFieldCommand))
      screenDimensions = ((ReadStructuredFieldCommand) command).getScreenDimensions ();
  }

  private void checkClientName (Command command)
  {
    if (!(command instanceof ReadStructuredFieldCommand))
      return;

    Optional<String> optionalName =
        ((ReadStructuredFieldCommand) command).getClientName ();
    if (optionalName.isPresent ())
    {
      clientName = optionalName.get ();
      setHeaderText ();
    }
  }

  private void checkServerName (Command command)
  {
    if (!(command instanceof WriteCommand))
      return;

    for (Order order : ((WriteCommand) command).getOrdersList ())
      if (order instanceof TextOrder)
      {
        String text = ((TextOrder) order).getTextString ();
        if (text.contains ("Welcome to Fan DeZhi Mainframe System!"))
        {
          serverName = "FanDeZhi";
          break;
        }
        else if (text.contains ("Hercules Version  :"))
        {
          serverName = "Hercules";
          break;
        }
        else if (text.equals ("[(03) 97974300 ]"))
        {
          serverName = "Nissan";
          break;
        }
        else if (text.startsWith ("[InterSession ---"))
        {
          serverName = "InterSession";
          break;
        }
      }

    if (serverName != null)
      setHeaderText ();
  }

  private void setHeaderText ()
  {
    Platform.runLater ( () -> headerLabel
        .setText (String.format ("%s : %s", getServerName (), getClientName ())));
  }

  public ObservableList<SessionRecord> getDataRecords ()
  {
    return sessionRecords;
  }

  public SessionRecord getNext (SessionRecordType dataRecordType)
  {
    for (SessionRecord dataRecord : sessionRecords)
      if (dataRecord.getDataRecordType () == dataRecordType)
        return dataRecord;
    return null;
  }

  public SessionRecord getBySize (int size)
  {
    for (SessionRecord dataRecord : sessionRecords)
      if (dataRecord.size () == size)
        return dataRecord;
    return null;
  }

  public void save (File file)
  {
    try
    {
      PrintWriter writer = new PrintWriter (file, "UTF-8");
      for (SessionRecord dataRecord : sessionRecords)
      {
        writer.printf ("%s %s %s%n",
                       dataRecord.getSource () == Source.CLIENT ? "Client" : "Server",
                       dataRecord.isGenuine () ? " " : "*", dataRecord.getDateTime ());

        // scramble user input
        if (safeFlag)
        {
          ReplyBuffer message = dataRecord.getMessage ();

          if (message instanceof TN3270ExtendedCommand)
            message = ((TN3270ExtendedCommand) message).getCommand ();

          if (message instanceof AIDCommand)
            ((AIDCommand) message).scramble ();
        }

        // write the data buffer after adding back the double-FF bytes
        writer.println (Dm3270Utility.toHex (dataRecord.getMessage ().getTelnetData ()));
      }
      writer.close ();
    }
    catch (FileNotFoundException | UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }
  }

  public void safeSave (File file)
  {
    safeFlag = true;
    save (file);
    safeFlag = false;
  }

  @Override
  public Iterator<SessionRecord> iterator ()
  {
    return sessionRecords.iterator ();
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    for (SessionRecord record : sessionRecords)
    {
      text.append (record);
      text.append ("\n");
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);
    else
      text.append ("Empty session");

    return text.toString ();
  }
}