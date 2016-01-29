package com.bytezone.dm3270.console;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ConsoleMessage
{
  private static final Pattern codePattern =
      Pattern.compile ("(\\+?[A-Z]{3,5}[0-9]{2,5}[A-Z]?|\\$HASP[0-9]{3,4}|------) (.*)");
  private static final Pattern timePattern = Pattern.compile ("\\d{2}(\\.\\d{2}){2}");
  private static final Pattern actionPattern = Pattern.compile ("\\d\\d .*");

  private final StringProperty time = new SimpleStringProperty ();
  private final StringProperty system = new SimpleStringProperty ();
  private final StringProperty task = new SimpleStringProperty ();
  private final StringProperty messageCode = new SimpleStringProperty ();
  private final StringProperty respond = new SimpleStringProperty ();
  private final StringProperty message = new SimpleStringProperty ();

  private final List<String> lines = new ArrayList<> ();
  private boolean formatted;

  // IPL messages
  public ConsoleMessage (String code, String message)
  {
    setTime ("--");
    setSystem ("IPL");
    setTask ("");
    setRespond ("");

    setMessageCode (code);
    setMessage (message);

    lines.add (message);
  }

  // Console messages
  public ConsoleMessage (String line)
  {
    String time = line.substring (5, 13);
    setTime (timePattern.matcher (time).matches () ? time : "");

    setSystem (line.substring (14, 22).trim ());
    setTask (line.substring (22, 31).trim ());
    setRespond (line.substring (31, 32).trim ());

    String text = line.substring (32).trim ();

    if (!getRespond ().isEmpty () && actionPattern.matcher (text).matches ())
    {
      setRespond (text.substring (0, 2));
      text = text.substring (3);
    }

    Matcher matcher = codePattern.matcher (text);
    if (matcher.matches ())
    {
      setMessageCode (matcher.group (1));
      setMessage (matcher.group (2));
    }
    else
    {
      setMessageCode ("");
      setMessage (text);
    }

    lines.add (getMessage ());

    if (line.length () != 79)
      formatted = true;
  }

  public void setFormatted ()
  {
    formatted = true;
  }

  public void add (String line)
  {
    lines.add (line);
    if (lines.size () == 2 && !formatted)
      setMessage (getMessage () + " " + line.trim ());
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    String line1 =
        String.format ("%-8s %-7s %-8s %2s %-9s %s", getTime (), getSystem (), getTask (),
                       getRespond (), getMessageCode (), lines.get (0));
    text.append (line1);
    int length = line1.length ();
    boolean joining = !formatted;

    for (int i = 1; i < lines.size (); i++)
    {
      String line = lines.get (i);
      String trimmedLine = line.trim ();

      if (joining && length + trimmedLine.length () + 1 < 150)
      {
        text.append (" ");
        text.append (trimmedLine);
        length += trimmedLine.length () + 1;
      }
      else
      {
        String chunk = String.format ("%n%37.37s%s", "", joining ? trimmedLine : line);
        text.append (chunk);
        length = chunk.length ();
      }

      if (joining && trimmedLine.endsWith (":"))
        joining = false;
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // Time
  // ---------------------------------------------------------------------------------//

  public final void setTime (String value)
  {
    time.set (value);
  }

  public final String getTime ()
  {
    return time.get ();
  }

  // ---------------------------------------------------------------------------------//
  // System
  // ---------------------------------------------------------------------------------//

  public final void setSystem (String value)
  {
    system.set (value);
  }

  public final String getSystem ()
  {
    return system.get ();
  }

  // ---------------------------------------------------------------------------------//
  // Task
  // ---------------------------------------------------------------------------------//

  public final void setTask (String value)
  {
    task.set (value);
  }

  public final String getTask ()
  {
    return task.get ();
  }

  // ---------------------------------------------------------------------------------//
  // MessageCode
  // ---------------------------------------------------------------------------------//

  public final void setMessageCode (String value)
  {
    messageCode.set (value);
  }

  public final String getMessageCode ()
  {
    return messageCode.get ();
  }

  // ---------------------------------------------------------------------------------//
  // Respond
  // ---------------------------------------------------------------------------------//

  public final void setRespond (String value)
  {
    respond.set (value);
  }

  public final String getRespond ()
  {
    return respond.get ();
  }

  // ---------------------------------------------------------------------------------//
  // FirstLine
  // ---------------------------------------------------------------------------------//

  public final void setMessage (String value)
  {
    message.set (value);
  }

  public final String getMessage ()
  {
    return message.get ();
  }
}