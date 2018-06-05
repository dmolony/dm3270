package com.bytezone.dm3270.telnet;

public interface TelnetCommandProcessor {

  void processData(byte[] buffer, int length);

  void processRecord(byte[] buffer, int length);

  void processTelnetCommand(byte[] buffer, int length);

  void processTelnetSubcommand(byte[] buffer, int length);

}
