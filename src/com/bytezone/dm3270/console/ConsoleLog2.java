package com.bytezone.dm3270.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javafx.scene.control.TextArea;
import javafx.scene.text.Font;

/*
 * https://www-01.ibm.com/support/knowledgecenter/SSLTBW_2.2.0/com.ibm.zos.v2r2.ieag100/
 * toc.htm
 * The system uses five special screen characters to indicate the status of certain
 * screen messages. These special characters appear in position three, four, or five
 * of the lines in the message area:
 * A vertical line (|) in position three indicates that required action has been
 * taken for the message and the system has deleted the message.
 * A horizontal bar (-) in position three indicates that the message is for
 * information only and requires no action from you.
 * An asterisk (*) in position four indicates that the message is a system message
 * that requires action from you.
 * An at sign (@) in position four indicates that the message is a problem program
 * message that requires action from you.
 * A plus sign (+) in position five indicates that the message is a problem program
 * message that requires no action from you.
 */
public class ConsoleLog2
{
  private static final Pattern messagePattern =
      Pattern.compile ("^...[-| ][* ]\\d\\d(\\.\\d\\d){2} .*");
  private static final Pattern twoDigits = Pattern.compile ("\\d\\d");

  private final List<ConsoleMessage2> messages = new ArrayList<> ();
  private final TextArea text = new TextArea ();
  private final boolean debug = false;

  public ConsoleLog2 (Font font)
  {
    text.setFont (font);
  }

  public void addLines (List<String> lines)
  {
    List<ConsoleMessage2> tempMessages = new ArrayList<> ();
    int max = lines.size ();

    if (debug)
    {
      System.out.println ("--------1---------------------------------------------------");
      System.out.printf ("%d lines to check%n", max);
    }

    // work backwards from the bottom of the screen to the top
    for (int i = max - 1; i >= 0; i--)
    {
      String line = lines.get (i);
      if (messagePattern.matcher (line).matches ())
      {
        ConsoleMessage2 message = new ConsoleMessage2 ();
        for (int j = i; j < max; j++)
          message.add (lines.get (j));
        max = i;
        tempMessages.add (message);
      }
    }

    if (debug)
    {
      System.out.println ("--------2---------------------------------------------------");
      for (ConsoleMessage2 message : tempMessages)
        System.out.println (message);
      System.out.println ("--------3---------------------------------------------------");
      System.out.printf ("%d lines left over%n", max);
    }

    // check for lines left over that have no message header
    if (max > 0 && messages.size () > 0)
    {
      ConsoleMessage2 checkMessage = messages.get (messages.size () - 1);
      for (int j = 0; j < max; j++)
      {
        checkMessage.add (lines.get (j));
        text.appendText (String.format ("%n                           %s",
                                        lines.get (j).substring (5)));

        if (debug)
          System.out.println (lines.get (j));
      }
    }

    if (debug)
    {
      System.out.println ("-------4----------------------------------------------------");
      System.out.printf ("%d messages to check%n", tempMessages.size ());
    }
    Collections.reverse (tempMessages);

    // only check for duplicate lines if there is no two-digit flag
    String prefix = lines.get (0).substring (1, 3);
    boolean duplicateCheck = !twoDigits.matcher (prefix).matches ();
    for (ConsoleMessage2 message : tempMessages)
      duplicateCheck = !add (message, duplicateCheck);
  }

  private boolean add (ConsoleMessage2 message, boolean duplicateCheck)
  {
    if (debug)
    {
      System.out.printf ("checking (%s):", duplicateCheck);
      System.out.println (message);
    }

    if (duplicateCheck)
    {
      int last = Math.max (0, messages.size () - 20);
      for (int i = messages.size () - 1; i >= last; i--)
        if (messages.get (i).matches (message))
        {
          if (debug)
            System.out.println ("  --> already there");
          return false;
        }
    }

    if (messages.size () > 0)
      text.appendText ("\n");

    text.appendText (message.toString ());
    messages.add (message);
    fireConsoleMessage (message);

    if (debug)
      System.out.println ("  --> adding");

    return true;
  }

  TextArea getTextArea ()
  {
    return text;
  }

  // ---------------------------------------------------------------------------------//
  // Listener events
  // ---------------------------------------------------------------------------------//

  private final Set<ConsoleMessageListener> consoleMessageListeners = new HashSet<> ();

  private void fireConsoleMessage (ConsoleMessage2 consoleMessage)
  {
    consoleMessageListeners.forEach (l -> l.consoleMessage (consoleMessage));
  }

  public void addConsoleMessageListener (ConsoleMessageListener listener)
  {
    if (!consoleMessageListeners.contains (listener))
      consoleMessageListeners.add (listener);
  }

  public void removeConsoleMessageListener (ConsoleMessageListener listener)
  {
    if (consoleMessageListeners.contains (listener))
      consoleMessageListeners.remove (listener);
  }
}