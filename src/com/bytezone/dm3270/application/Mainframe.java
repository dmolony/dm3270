package com.bytezone.dm3270.application;

import com.bytezone.dm3270.commands.Command;

public interface Mainframe
{
  public void receiveCommand (Command command);
}