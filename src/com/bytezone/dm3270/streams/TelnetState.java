package com.bytezone.dm3270.streams;

public class TelnetState
{
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