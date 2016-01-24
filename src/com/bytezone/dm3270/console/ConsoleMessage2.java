package com.bytezone.dm3270.console;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ConsoleMessage2
{
  private static final Pattern codePattern =
      Pattern.compile ("(\\+?[A-Z]{3,5}[0-9]{2,5}[A-Z]?|\\$HASP[0-9]{3,4}) (.*)");
  private static final Pattern timePattern = Pattern.compile ("\\d{2}(\\.\\d{2}){2}");

  private StringProperty timeProperty;
  private StringProperty systemProperty;
  private StringProperty subsystemProperty;
  private StringProperty messageCodeProperty;
  private StringProperty respondProperty;
  private StringProperty firstLineProperty;
  private StringProperty firstLineRestProperty;

  private final List<String> lines = new ArrayList<> ();
  private boolean formatted;

  public ConsoleMessage2 (String line)
  {
    String time = line.substring (5, 13);
    if (timePattern.matcher (time).matches ())
      setTime (time);
    else
      setTime ("");
    setSystem (line.substring (14, 22).trim ());
    setSubsystem (line.substring (22, 31).trim ());
    setRespond (line.substring (31, 32));
    setFirstLineRest (line.substring (32).trim ());

    String text = line.substring (32).trim ();

    Matcher matcher = codePattern.matcher (text);
    if (matcher.matches ())
    {
      setMessageCode (matcher.group (1));
      setFirstLine (matcher.group (2));
    }
    else
    {
      setMessageCode ("");
      setFirstLine (text);
    }

    if (line.length () != 79)
      formatted = true;

    lines.add (line);
  }

  public ConsoleMessage2 (String code, String message)
  {
    setMessageCode (code);
    setFirstLine (message);
    setFirstLineRest (code + " " + message);
    lines.add (code + " " + message);
    setTime ("");
    setSystem ("");
    setSubsystem ("");
    setRespond ("");
    formatted = true;
  }

  public void add (String line)
  {
    lines.add (line);

    if (lines.size () == 1)
    {
      //      String time = line.substring (5, 13);
      //      if (timePattern.matcher (time).matches ())
      //        setTime (time);
      //      setSystem (line.substring (14, 22).trim ());
      //      setSubsystem (line.substring (22, 31).trim ());
      //      setRespond (line.substring (31, 32));
      //      setFirstLineRest (line.substring (32).trim ());
      //
      //      String text = line.substring (32).trim ();
      //
      //      Matcher matcher = codePattern.matcher (text);
      //      if (matcher.matches ())
      //      {
      //        setMessageCode (matcher.group (1));
      //        setFirstLine (matcher.group (2));
      //      }
      //      else
      //      {
      //        setMessageCode ("");
      //        setFirstLine (text);
      //      }
      //
      //      if (line.length () != 79)
      //        formatted = true;
      System.out.println ("impossible");
    }
    else if (lines.size () == 2)
    {
      if (!formatted)
        setFirstLine (getFirstLine () + " " + line.trim ());
    }
  }

  boolean getFormattedFlag ()
  {
    return formatted;
  }

  public boolean matches (ConsoleMessage2 message)
  {
    if (lines.size () != message.lines.size ())
      return false;

    if (!getTime ().equals (message.getTime ()))
      return false;

    if (!getSystem ().equals (message.getSystem ()))
      return false;

    if (!getSubsystem ().equals (message.getSubsystem ()))
      return false;

    if (!getFirstLine ().equals (message.getFirstLine ()))
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

  // ---------------------------------------------------------------------------------//
  // Time
  // ---------------------------------------------------------------------------------//

  public final void setTime (String value)
  {
    timeProperty ().set (value);
  }

  public final String getTime ()
  {
    return timeProperty ().get ();
  }

  public final StringProperty timeProperty ()
  {
    if (timeProperty == null)
      timeProperty = new SimpleStringProperty ();
    return timeProperty;
  }

  // ---------------------------------------------------------------------------------//
  // System
  // ---------------------------------------------------------------------------------//

  public final void setSystem (String value)
  {
    systemProperty ().set (value);
  }

  public final String getSystem ()
  {
    return systemProperty ().get ();
  }

  public final StringProperty systemProperty ()
  {
    if (systemProperty == null)
      systemProperty = new SimpleStringProperty ();
    return systemProperty;
  }

  // ---------------------------------------------------------------------------------//
  // Subsystem
  // ---------------------------------------------------------------------------------//

  public final void setSubsystem (String value)
  {
    subsystemProperty ().set (value);
  }

  public final String getSubsystem ()
  {
    return subsystemProperty ().get ();
  }

  public final StringProperty subsystemProperty ()
  {
    if (subsystemProperty == null)
      subsystemProperty = new SimpleStringProperty ();
    return subsystemProperty;
  }

  // ---------------------------------------------------------------------------------//
  // Respond
  // ---------------------------------------------------------------------------------//

  public final void setRespond (String value)
  {
    respondProperty ().set (value);
  }

  public final String getRespond ()
  {
    return respondProperty ().get ();
  }

  public final StringProperty respondProperty ()
  {
    if (respondProperty == null)
      respondProperty = new SimpleStringProperty ();
    return respondProperty;
  }

  // ---------------------------------------------------------------------------------//
  // FirstLineRest
  // ---------------------------------------------------------------------------------//

  public final void setFirstLineRest (String value)
  {
    firstLineRestProperty ().set (value);
  }

  public final String getFirstLineRest ()
  {
    return firstLineRestProperty ().get ();
  }

  public final StringProperty firstLineRestProperty ()
  {
    if (firstLineRestProperty == null)
      firstLineRestProperty = new SimpleStringProperty ();
    return firstLineRestProperty;
  }

  // ---------------------------------------------------------------------------------//
  // MessageCode
  // ---------------------------------------------------------------------------------//

  public final void setMessageCode (String value)
  {
    messageCodeProperty ().set (value);
  }

  public final String getMessageCode ()
  {
    return messageCodeProperty ().get ();
  }

  public final StringProperty messageCodeProperty ()
  {
    if (messageCodeProperty == null)
      messageCodeProperty = new SimpleStringProperty ();
    return messageCodeProperty;
  }

  // ---------------------------------------------------------------------------------//
  // FirstLine
  // ---------------------------------------------------------------------------------//

  public final void setFirstLine (String value)
  {
    firstLineProperty ().set (value);
  }

  public final String getFirstLine ()
  {
    return firstLineProperty ().get ();
  }

  public final StringProperty firstLineProperty ()
  {
    if (firstLineProperty == null)
      firstLineProperty = new SimpleStringProperty ();
    return firstLineProperty;
  }

  public String firstLine ()
  {
    return String.format ("%-8s %-7s %-8s %1s%s", getTime (), getSystem (),
                          getSubsystem (), getRespond (), getFirstLineRest ());
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    String line1 = firstLine ();
    text.append (line1);
    int length = line1.length ();

    boolean joining = !formatted;
    for (int i = 1; i < lines.size (); i++)
    {
      //      String line = lines.get (i).substring (5);
      String line = lines.get (i);
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