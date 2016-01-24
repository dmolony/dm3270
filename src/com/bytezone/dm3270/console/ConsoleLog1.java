package com.bytezone.dm3270.console;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.control.TextArea;
import javafx.scene.text.Font;

public class ConsoleLog1
{
  private static final Pattern codePattern =
      Pattern.compile ("  ([A-Z]{3,4}[0-9]{3,5}[A-Z]) (.*?)\\s*");

  private final TextArea text = new TextArea ();
  private final List<ConsoleMessage> messages = new ArrayList<> ();
  private ConsoleMessage currentMessage;

  public ConsoleLog1 (Font font)
  {
    text.setFont (font);
  }

  public void addLines (String[] lines, int firstLine, int lastLine)
  {
    for (int i = firstLine; i < lastLine; i++)
    {
      String line = lines[i];
      Matcher m = codePattern.matcher (line);
      if (m.matches ())
      {
        String code = m.group (1);
        String message = m.group (2);

        currentMessage = new ConsoleMessage (code, message);
        messages.add (currentMessage);
        fireConsoleMessage (currentMessage);

        if (messages.size () == 1)
          text.appendText (code + " " + message);
        else
          text.appendText ("\n" + code + " " + message);
      }
      else
      {
        currentMessage.add (line);
        text.appendText ("\n      " + line);
      }
    }
  }

  TextArea getTextArea ()
  {
    return text;
  }

  public void dump ()
  {
    for (ConsoleMessage message : messages)
      System.out.println (message);
    System.out.println ("---------------------------------------------------"
        + "----------------------------");
  }

  // ---------------------------------------------------------------------------------//
  // Listener events
  // ---------------------------------------------------------------------------------//

  private final Set<ConsoleMessageListener> consoleMessageListeners = new HashSet<> ();

  private void fireConsoleMessage (ConsoleMessage consoleMessage)
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