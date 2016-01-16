package com.bytezone.dm3270.console;

import java.util.ArrayList;
import java.util.List;

public class ConsoleMessage
{
  int hours;
  int minutes;
  int seconds;
  String system;
  String subsystem;
  String prefix1;
  String prefix2;
  String rest;
  List<String> lines = new ArrayList<> ();

  public void add (String line)
  {
    if (lines.size () == 0)
    {
      prefix1 = line.substring (0, 5);
      hours = Integer.parseInt (line.substring (5, 7));
      minutes = Integer.parseInt (line.substring (8, 10));
      seconds = Integer.parseInt (line.substring (11, 13));
      system = line.substring (14, 22).trim ();
      subsystem = line.substring (22, 31).trim ();
      prefix2 = line.substring (31, 32);
      rest = line.substring (32).trim ();
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

  public String firstLine ()
  {
    return String.format ("%s%02d.%02d.%02d %-7s %-8s %s%s", prefix1, hours, minutes,
                          seconds, system, subsystem, prefix2, rest);
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String
        .format ("%s %s%n", firstLine (),
                 lines.size () >= 2 ? lines.get (1).substring (5).trim () : ""));
    for (int i = 2; i < lines.size (); i++)
      text.append (String.format ("%s%n", lines.get (i)));

    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}