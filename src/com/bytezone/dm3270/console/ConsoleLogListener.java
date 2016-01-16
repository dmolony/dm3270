package com.bytezone.dm3270.console;

public interface ConsoleLogListener
{
  public void addScreenText (String message);

  public int size ();
}