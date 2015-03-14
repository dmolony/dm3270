package com.bytezone.dm3270.session;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.bytezone.dm3270.application.TelnetState;
import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.commands.ReadStructuredFieldCommand;
import com.bytezone.dm3270.commands.WriteCommand;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.orders.TextOrder;
import com.bytezone.dm3270.replyfield.OEMAuxilliaryDevice;
import com.bytezone.dm3270.replyfield.QueryReplyField;
import com.bytezone.dm3270.replyfield.RPQNames;
import com.bytezone.dm3270.replyfield.Summary;
import com.bytezone.dm3270.session.SessionRecord.SessionRecordType;
import com.bytezone.dm3270.streams.TelnetListener;
import com.bytezone.dm3270.streams.TelnetSocket.Source;
import com.bytezone.dm3270.structuredfields.QueryReplySF;
import com.bytezone.dm3270.structuredfields.StructuredField;

public class Session implements Iterable<SessionRecord>
{
  private final ObservableList<SessionRecord> dataRecords = FXCollections
      .observableArrayList ();
  private final SessionMode sessionMode;
  private final Screen screen;
  private final TelnetState telnetState;

  private String clientName = "Unknown";
  private String serverName = null;
  private final List<String> labels = new ArrayList<> ();

  public enum SessionMode
  {
    SPY, REPLAY, TERMINAL
  }

  /**
   * Creates a new, empty session in either Spy or Terminal mode.
   * 
   * @param mode
   */
  public Session (Screen screen, TelnetState telnetState, SessionMode mode)
  {
    this.screen = screen;
    this.telnetState = telnetState;
    sessionMode = mode;
  }

  /**
   * Recreates a previous session from a file. Mode will be Replay.
   * Each buffer is passed to a TelnetListener which converts it to a Message
   * and passes it back here via the Session.add (DataRecord) function.
   * 
   * @param filename
   */
  public Session (Screen screen, Path path)
  {
    sessionMode = SessionMode.REPLAY;

    SessionReader server = new SessionReader (Source.SERVER, path);
    SessionReader client = new SessionReader (Source.CLIENT, path);

    this.telnetState = new TelnetState ();
    this.screen = screen;

    TelnetListener clientTelnetListener = new TelnetListener (Source.CLIENT, this);
    TelnetListener serverTelnetListener = new TelnetListener (Source.SERVER, this);

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

  public Screen getScreen ()
  {
    return screen;
  }

  public TelnetState getTelnetState ()
  {
    return telnetState;
  }

  public List<String> getLabels ()
  {
    return labels;
  }

  public int size ()
  {
    return dataRecords.size ();
  }

  public String getClientName ()
  {
    return clientName == null ? "Unknown" : clientName;
  }

  public String getServerName ()
  {
    return serverName == null ? "Unknown" : serverName;
  }

  /**
   * Returns one of Spy, Replay or Terminal.
   * 
   * @return SessionMode
   */

  public SessionMode getSessionMode ()
  {
    return sessionMode;
  }

  /**
   * Called by the TelnetListener after it has converted the command into
   * a DataRecord. Traffic could be from either source (Client or Server).
   * 
   * @param sessionRecord
   */
  public synchronized void add (SessionRecord sessionRecord)
  {
    if (sessionRecord == null)
      throw new IllegalArgumentException ("DataRecord is null");

    dataRecords.add (sessionRecord);       // should be concurrent?

    if (sessionMode == SessionMode.REPLAY && sessionRecord.isCommand ())
      if (sessionRecord.getSource () == Source.CLIENT)
        checkClientName (sessionRecord.getCommand ());
      else if (serverName == null)
        checkServerName (sessionRecord.getCommand ());
  }

  private void checkClientName (Command command)
  {
    if (!(command instanceof ReadStructuredFieldCommand))
      return;

    for (StructuredField sf : ((ReadStructuredFieldCommand) command).getFieldList ())
      if (sf instanceof QueryReplySF)
      {
        QueryReplyField rf = ((QueryReplySF) sf).getReplyField ();
        byte type = rf.getReplyType ().type;
        if (type == QueryReplyField.OEM_AUXILLIARY_DEVICE_REPLY)
        {
          OEMAuxilliaryDevice oem = (OEMAuxilliaryDevice) rf;
          clientName = oem.getUserName ();
          if (clientName.equals ("VISTA"))
            clientName = "Vista";
        }
        else if (type == QueryReplyField.RPQ_NAMES_REPLY)
        {
          RPQNames rpqNames = (RPQNames) rf;
          clientName = rpqNames.getRPQName ();
        }
        else if (type == QueryReplyField.SUMMARY_QUERY_REPLY)
        {
          Summary summary = (Summary) rf;
          int size = summary.size ();
          if (size == 4)
            clientName = "FreeHost";
        }
      }
  }

  private void checkServerName (Command command)
  {
    if (!(command instanceof WriteCommand))
      return;

    for (Order order : ((WriteCommand) command).getOrdersList ())
    {
      if (order instanceof TextOrder)
      {
        TextOrder textOrder = (TextOrder) order;
        if (textOrder.getTextString ()
            .contains ("Welcome to Fan DeZhi Mainframe System!"))
        {
          serverName = "FanDeZhi";
          break;
        }
        else if (textOrder.getTextString ().contains ("Hercules Version  :"))
        {
          serverName = "Hercules";
          break;
        }
      }
    }
    if (serverName == null)
      serverName = "Unknown";
  }

  public ObservableList<SessionRecord> getDataRecords ()
  {
    return dataRecords;
  }

  public SessionRecord getNext (SessionRecordType dataRecordType)
  {
    for (SessionRecord dataRecord : dataRecords)
      if (dataRecord.getDataRecordType () == dataRecordType)
        return dataRecord;
    return null;
  }

  /**
   * Save the session to a file, for later use as a Replay session.
   * 
   * @param file
   */

  public void save (File file)
  {
    try
    {
      PrintWriter writer = new PrintWriter (file, "UTF-8");
      for (SessionRecord dataRecord : dataRecords)
      {
        writer.printf ("%s %s %s%n", dataRecord.getSource () == Source.CLIENT ? "Client"
            : "Server", dataRecord.isGenuine () ? " " : "*", dataRecord.getDateTime ());

        // write the data buffer after adding back the double-FF bytes
        writer.println (Utility.toHex (dataRecord.getMessage ().getTelnetData ()));
      }
      writer.close ();
    }
    catch (FileNotFoundException | UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }
  }

  /**
   * Provides the Iterable<DataRecord> interface.
   */

  @Override
  public Iterator<SessionRecord> iterator ()
  {
    return dataRecords.iterator ();
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    for (SessionRecord record : dataRecords)
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