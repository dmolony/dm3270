package com.bytezone.dm3270.assistant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

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
  private final List<ConsoleMessage> messages = new ArrayList<> ();

  public void addLines (List<String> lines)
  {
    List<ConsoleMessage> tempMessages = new ArrayList<> ();

    int max = lines.size ();
    for (int i = max - 1; i >= 0; i--)
    {
      String line = lines.get (i);
      if (messagePattern.matcher (line).matches ())
      {
        ConsoleMessage message = new ConsoleMessage ();
        for (int j = i; j < max; j++)
          message.add (lines.get (j));
        max = i;
        tempMessages.add (message);
      }
    }

    if (max > 0)
    {
      ConsoleMessage lastMessage = messages.get (messages.size () - 1);
      for (int j = 0; j < max; j++)
        lastMessage.add (lines.get (j));
    }

    Collections.reverse (tempMessages);
    for (ConsoleMessage message : tempMessages)
      add (message);
  }

  private void add (ConsoleMessage message)
  {
    int last = Math.max (0, messages.size () - 20);
    for (int i = messages.size () - 1; i >= last; i--)
      if (messages.get (i).matches (message))
        return;

    messages.add (message);

    if (messages.size () == 1317)
      writeMessages ();
  }

  private void writeMessages ()
  {
    for (ConsoleMessage message : messages)
      System.out.println (message);
  }
}