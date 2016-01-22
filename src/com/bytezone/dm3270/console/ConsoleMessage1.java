package com.bytezone.dm3270.console;

import java.util.ArrayList;
import java.util.List;

public class ConsoleMessage1
{
  private final String code;
  private final List<String> lines = new ArrayList<> ();

  public ConsoleMessage1 (String code, String message)
  {
    this.code = code;
    lines.add (message);
  }

  public void add (String line)
  {
    lines.add (line);
  }
}