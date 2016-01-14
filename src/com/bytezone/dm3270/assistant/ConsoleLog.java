package com.bytezone.dm3270.assistant;

import java.util.ArrayList;
import java.util.List;

public class ConsoleLog implements ConsoleLogListener
{
  List<String> log = new ArrayList<> ();

  @Override
  public void consoleMessage (String message)
  {
    for (int ptr = 0; ptr < message.length (); ptr += 80)
    {
      int max = Math.min (ptr + 80, message.length ());
      String line = message.substring (ptr, max);
      if (!line.trim ().isEmpty ())
        System.out.println (line);
    }
    System.out.println ();
  }
}