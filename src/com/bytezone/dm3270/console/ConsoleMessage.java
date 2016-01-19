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
  boolean flag;         // indent ?

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
      if (line.length () != 79)
        flag = true;
    }
    lines.add (line);
  }

  boolean getFlag ()
  {
    return flag;
  }

  public boolean matches (ConsoleMessage message)
  {
    if (lines.size () != message.lines.size ())
      return false;

    if (hours != message.hours || minutes != message.minutes
        || seconds != message.seconds)
      return false;

    if (!system.equals (message.system))
      return false;

    if (!subsystem.equals (message.subsystem))
      return false;

    if (!rest.equals (message.rest))
      return false;

    for (int i = 1; i < lines.size (); i++)
    {
      String line1 = lines.get (i);
      String line2 = message.lines.get (i);
      if (line1.length () != line2.length ())
        return false;
      if (!line1.substring (5).equals (line2.substring (5)))
        return false;
    }

    return true;
  }

  public boolean contains (String searchLine)
  {
    for (String line : lines)
      if (line.equals (searchLine))
        return true;
    return false;
  }

  public String firstLine ()
  {
    return String.format ("%02d.%02d.%02d %-7s %-8s %s%s", hours, minutes, seconds,
                          system, subsystem, prefix2, rest);
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    String line1 = firstLine ();
    text.append (line1);
    int length = line1.length ();

    boolean joining = !flag;
    for (int i = 1; i < lines.size (); i++)
    {
      String line = lines.get (i).substring (5);
      String trimmedLine = line.trim ();
      if (joining && length + trimmedLine.length () + 1 < 140)
      {
        text.append (" ");
        text.append (trimmedLine);
        length += trimmedLine.length () + 1;
      }
      else
      {
        text.append (String.format ("%n                           %s",
                                    joining ? trimmedLine : line));
        length = trimmedLine.length () + 27;
      }
      if (joining && trimmedLine.endsWith (":"))
        joining = false;
    }

    return text.toString ();
  }
}