package com.bytezone.dm3270.application;

import com.bytezone.dm3270.streams.TerminalServer;
import com.bytezone.dm3270.telnet.TelnetCommand;

public class TelnetState
{
  // preferences
  private int do3270Extended = TelnetCommand.DO;
  private int doBinary = TelnetCommand.DO;
  private int doEOR = TelnetCommand.DO;

  // current status
  private boolean does3270Extended;
  private boolean doesEOR;
  private boolean doesBinary;
  private String terminal = "";

  // Socket
  private TerminalServer terminalServer;

  public void setTerminalServer (TerminalServer terminalServer)
  {
    this.terminalServer = terminalServer;
  }

  public void write (byte[] buffer)
  {
    if (terminalServer != null)
      terminalServer.write (buffer);
  }

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

  public void setTerminal (String terminal)
  {
    this.terminal = terminal;
  }

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

  public String getTerminal ()
  {
    return terminal;
  }

  public void setDo3270Extended (boolean state)
  {
    do3270Extended = state ? TelnetCommand.DO : TelnetCommand.DONT;
  }

  public void setDoBinary (boolean state)
  {
    doBinary = state ? TelnetCommand.DO : TelnetCommand.DONT;
  }

  public void setDoEOR (boolean state)
  {
    doEOR = state ? TelnetCommand.DO : TelnetCommand.DONT;
  }
}