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
      Pattern.compile ("(\\+?[A-Z]{3,5}[0-9]{2,5}[A-Z]?|\\$HASP[0-9]{3,4}) (.*)");
  private static final Pattern timePattern = Pattern.compile ("\\d{2}(\\.\\d{2}){2}");

  private StringProperty timeProperty;
  private StringProperty systemProperty;
  private StringProperty taskProperty;
  private StringProperty messageCodeProperty;
  private StringProperty respondProperty;
  private StringProperty firstLineProperty;
  //  private StringProperty firstLineRestProperty;

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
    setFirstLine (message);
    lines.add (code + " " + message);

    formatted = true;           // don't join lines
  }

  // Console messages
  public ConsoleMessage (String line)
  {
    String time = line.substring (5, 13);
    setTime (timePattern.matcher (time).matches () ? time : "");

    setSystem (line.substring (14, 22).trim ());
    setTask (line.substring (22, 31).trim ());
    setRespond (line.substring (31, 32));

    String text = line.substring (32).trim ();
    //    setFirstLineRest (text);

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

  public void add (String line)
  {
    lines.add (line);
    if (lines.size () == 2 && !formatted)
      setFirstLine (getFirstLine () + " " + line.trim ());
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
  // Task
  // ---------------------------------------------------------------------------------//

  public final void setTask (String value)
  {
    taskProperty ().set (value);
  }

  public final String getTask ()
  {
    return taskProperty ().get ();
  }

  public final StringProperty taskProperty ()
  {
    if (taskProperty == null)
      taskProperty = new SimpleStringProperty ();
    return taskProperty;
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

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    String line1 = String.format ("%-8s %-7s %-8s %1s%s", getTime (), getSystem (),
                                  getTask (), getRespond (), getFirstLine ());
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