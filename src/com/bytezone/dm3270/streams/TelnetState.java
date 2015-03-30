package com.bytezone.dm3270.streams;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TelnetState implements Runnable
{
  private static final DateTimeFormatter formatter = DateTimeFormatter
      .ofPattern ("dd MMM uuuu HH:mm:ss.S");
  private static byte[] noOp = { (byte) 0xFF, (byte) 0xF1 };

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
  private TerminalServer terminalServer;
  private final boolean debug = false;

  // IO
  private volatile long lastAccess;
  private volatile boolean running = false;
  private Thread thread;

  private int totalReads;
  private int totalWrites;
  private int totalBytesRead;
  private int totalBytesWritten;

  public void setTerminalServer (TerminalServer terminalServer)
  {
    this.terminalServer = terminalServer;
    thread = new Thread (this);
    thread.start ();
  }

  public void setLastAccess (LocalDateTime dateTime, int bytes)
  {
    lastAccess = System.currentTimeMillis ();
    ++totalReads;
    totalBytesRead += bytes;

    if (debug)
      System.out.printf ("Read  : %,6d %s%n", bytes, formatter.format (dateTime));
  }

  public void write (byte[] buffer)
  {
    if (terminalServer != null)
      terminalServer.write (buffer);

    lastAccess = System.currentTimeMillis ();
    ++totalWrites;
    totalBytesWritten += buffer.length;

    if (debug)
      System.out.printf ("Write : %,6d %s%n", buffer.length,
                         formatter.format (LocalDateTime.now ()));
  }

  // This thread exists simply to keep the connection alive. It sleeps for a
  // certain period, and when it wakes it issues a NOOP if nothing else has
  // communicated with the server.

  @Override
  public void run ()
  {
    long lastTimeIChecked;
    lastAccess = System.currentTimeMillis ();
    running = true;
    long limit = 120;      // seconds to wait

    while (running)
    {
      try
      {
        lastTimeIChecked = lastAccess;
        long delay = (System.currentTimeMillis () - lastAccess) / 1000;
        long sleep = limit - delay;

        Thread.sleep (sleep * 1000);
        if (lastTimeIChecked == lastAccess)
          write (noOp);
      }
      catch (InterruptedException e)
      {
        if (running)
          e.printStackTrace ();
        return;
      }
    }
  }

  public void close ()
  {
    if (thread != null)
    {
      running = false;
      thread.interrupt ();
    }
  }

  public String getSummary ()
  {
    if (totalReads == 0 || totalWrites == 0)
      return "No data";

    int averageReads = totalBytesRead / totalReads;
    int averageWrites = totalBytesWritten / totalWrites;
    int totalIOBytes = totalBytesRead + totalBytesWritten;
    int totalIO = totalReads + totalWrites;
    int averageIO = totalIOBytes / totalIO;

    StringBuilder text = new StringBuilder ();

    text.append (String.format ("          Total        Bytes    Average%n"));
    text.append (String.format ("         -------   ----------   -------%n"));
    text.append (String.format ("Reads     %,5d       %,7d     %,4d %n", totalReads,
                                totalBytesRead, averageReads));
    text.append (String.format ("Writes    %,5d       %,7d     %,4d %n", totalWrites,
                                totalBytesWritten, averageWrites));
    text.append (String.format ("         -------   ----------   -------%n"));
    text.append (String.format ("          %,5d       %,7d     %,4d %n", totalIO,
                                totalIOBytes, averageIO));

    return text.toString ();
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