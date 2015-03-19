package com.bytezone.dm3270.telnet;

public interface TelnetCommandProcessor
{
  public void processData (byte[] buffer, int length);

  public void processRecord (byte[] buffer, int length);

  public void processTelnetCommand (byte[] buffer, int length);

  public void processTelnetSubcommand (byte[] buffer, int length);
}