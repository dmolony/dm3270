package com.bytezone.dm3270.assistant;

import java.util.ArrayList;
import java.util.List;

public class ConsoleMessage
{
  int hours;
  int minutes;
  int seconds;
  List<String> lines = new ArrayList<> ();

  public void add (String line)
  {
    if (lines.size () == 0)
    {
      hours = Integer.parseInt (line.substring (5, 7));
      minutes = Integer.parseInt (line.substring (8, 10));
      seconds = Integer.parseInt (line.substring (11, 13));
    }
    lines.add (line);
  }

  public boolean matches (ConsoleMessage message)
  {
    if (lines.size () != message.lines.size ())
      return false;

    if (hours != message.hours || minutes != message.minutes
        || seconds != message.seconds)
      return false;

    for (int i = 0; i < lines.size (); i++)
      if (!lines.get (i).equals (message.lines.get (i)))
        return false;

    return true;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    if (lines.size () == 2)
    {
      String joinedLine = lines.get (0) + lines.get (1).substring (5).trim ();
      text.append (String.format ("%s%n", joinedLine));
    }
    else
      for (String line : lines)
        text.append (String.format ("%s%n", line));

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}