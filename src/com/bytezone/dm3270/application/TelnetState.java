package com.bytezone.dm3270.application;

import com.bytezone.dm3270.streams.TerminalServer;

public class TelnetState
{
  // preferences
  private boolean do3270Extended = false;
  private boolean doBinary = false;
  private boolean doEOR = false;

  // current status
  private boolean does3270Extended = false;
  private boolean doesEOR = true;
  private boolean doesBinary = true;
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

  // ---------------------------------------------------------------------------------//
  // Set current status (what was actually communicated during negotiations)
  // ---------------------------------------------------------------------------------//

  public void setDoes3270Extended (boolean state)
  {
    does3270Extended = state;
    //    System.out.println ("set actual " + state);
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

  // ---------------------------------------------------------------------------------//
  // Ask actual
  // ---------------------------------------------------------------------------------//

  public boolean does3270Extended ()
  {
    //    System.out.println ("ask actual " + does3270Extended);
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

  // ---------------------------------------------------------------------------------//
  // Ask preference
  // ---------------------------------------------------------------------------------//

  public boolean do3270Extended ()
  {
    //    System.out.println ("ask prefer " + do3270Extended);
    return do3270Extended;
  }

  public boolean doEOR ()
  {
    return doEOR || does3270Extended;
  }

  public boolean doBinary ()
  {
    return doBinary || does3270Extended;
  }

  // ---------------------------------------------------------------------------------//
  // Set preferences
  // ---------------------------------------------------------------------------------//

  public void setDo3270Extended (boolean state)
  {
    //    System.out.println ("set prefer " + state);
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
}