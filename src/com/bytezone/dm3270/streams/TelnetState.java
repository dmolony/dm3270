package com.bytezone.dm3270.streams;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TelnetState
{
  private static final DateTimeFormatter formatter = DateTimeFormatter
      .ofPattern ("dd MMM uuuu HH:mm:ss.S");

  // preferences
  private boolean do3270Extended = false;
  private boolean doBinary = true;
  private boolean doEOR = true;
  private boolean doTerminalType = true;

  // current status
  private boolean does3270Extended = false;
  private boolean doesEOR = false;
  private boolean doesBinary = false;
  private boolean doesTerminalType = false;

  private String terminal = "";

  // Socket
  private TerminalServer terminalServer;

  private volatile LocalDateTime lastAccess;
  private final boolean debug = false;
  private int totalReads;
  private int totalWrites;
  private int totalBytesRead;
  private int totalBytesWritten;

  public void setTerminalServer (TerminalServer terminalServer)
  {
    this.terminalServer = terminalServer;
  }

  public synchronized void setLastAccess (LocalDateTime dateTime, int bytes)
  {
    lastAccess = dateTime;
    ++totalReads;
    totalBytesRead += bytes;

    if (debug)
      System.out.printf ("Read  : %,6d %s%n", bytes, formatter.format (lastAccess));
  }

  public synchronized void write (byte[] buffer)
  {
    if (terminalServer != null)
    {
      terminalServer.write (buffer);

      lastAccess = LocalDateTime.now ();
      ++totalWrites;
      totalBytesWritten += buffer.length;

      if (debug)
        System.out.printf ("Write : %,6d %s%n", buffer.length,
                           formatter.format (lastAccess));
    }
  }

  public void printSummary ()
  {
    if (totalReads == 0 || totalWrites == 0)
    {
      System.out.println ("No data");
      return;
    }

    int averageReads = totalBytesRead / totalReads;
    int averageWrites = totalBytesWritten / totalWrites;
    int totalIOBytes = totalBytesRead + totalBytesWritten;
    int totalIO = totalReads + totalWrites;
    int averageIO = totalIOBytes / totalIO;

    System.out.printf ("          Total       Bytes     Average");
    System.out.printf ("         -------   ----------   -------");
    System.out.printf ("Reads    %,5d         %,7d        %,4d %n", totalReads,
                       totalBytesRead, averageReads);
    System.out.printf ("Writes   %,5d         %,7d        %,4d %n", totalWrites,
                       totalBytesWritten, averageWrites);
    System.out.printf ("         =======   ==========   =======");
    System.out.printf ("         %,5d         %,7d        %,4d %n", totalIO,
                       totalIOBytes, averageIO);
  }

  // ---------------------------------------------------------------------------------//
  // Set actual (what was communicated during negotiations)
  // ---------------------------------------------------------------------------------//

  public void setDoes3270Extended (boolean state)
  {
    does3270Extended = state;
  }

  public void setDoesEOR (boolean state)
  {
    doesEOR = state;
  }

  public void setDoesBinary (boolean state)
  {
    doesBinary = state;
  }

  public void setDoesTerminalType (boolean state)
  {
    doesTerminalType = state;
  }

  public void setTerminal (String terminal)
  {
    this.terminal = terminal;
  }

  // ---------------------------------------------------------------------------------//
  // Ask actual
  // ---------------------------------------------------------------------------------//

  public boolean does3270Extended ()
  {
    return does3270Extended;
  }

  public boolean doesEOR ()
  {
    return doesEOR || does3270Extended;
  }

  public boolean doesBinary ()
  {
    return doesBinary || does3270Extended;
  }

  public boolean doesTerminalType ()
  {
    return doesTerminalType || does3270Extended;
  }

  public String getTerminal ()
  {
    return terminal;
  }

  // ---------------------------------------------------------------------------------//
  // Ask preferences
  // ---------------------------------------------------------------------------------//

  public boolean do3270Extended ()
  {
    return do3270Extended;
  }

  public boolean doEOR ()
  {
    return doEOR;
  }

  public boolean doBinary ()
  {
    return doBinary;
  }

  public boolean doTerminalType ()
  {
    return doTerminalType;
  }

  // ---------------------------------------------------------------------------------//
  // Set preferences
  // ---------------------------------------------------------------------------------//

  public void setDo3270Extended (boolean state)
  {
    do3270Extended = state;
  }

  public void setDoBinary (boolean state)
  {
    doBinary = state;
  }

  public void setDoEOR (boolean state)
  {
    doEOR = state;
  }

  public void setDoTerminalType (boolean state)
  {
    doTerminalType = state;
  }
}