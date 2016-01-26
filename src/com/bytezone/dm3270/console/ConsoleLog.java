package com.bytezone.dm3270.console;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.control.TextArea;
import javafx.scene.text.Font;

public class ConsoleLog
{
  private static final Pattern codePattern =
      Pattern.compile ("  ([A-Z]{3,4}[0-9]{3,5}[A-Z]) (.*?)\\s*");
  private static final Pattern messagePattern =
      Pattern.compile ("^...[-| ][* ](\\d\\d(\\.\\d\\d){2}) (.*?)\\s*");

  private final TextArea text = new TextArea ();
  private final List<ConsoleMessage> messages = new ArrayList<> ();
  private ConsoleMessage currentMessage;
  private int currentTextLength;

  public ConsoleLog (Font font)
  {
    text.setFont (font);
  }

  // IPL lines
  public void addLines1 (String[] lines, int firstLine, int lastLine)
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
        if (messages.size () > 0)
          text.appendText ("\n");
        currentTextLength = text.getLength ();

        messages.add (currentMessage);
        fireConsoleMessage (currentMessage);
        text.appendText (currentMessage.toString ());
      }
      else
      {
        String code = line.substring (2, 10);
        if (code.trim ().isEmpty ())
        {
          currentMessage.setFormatted ();
          currentMessage.add (line.substring (10));
        }
        else
        {
          if (code.startsWith ("++"))
            currentMessage.setFormatted ();
          currentMessage.add (line.substring (2));
        }

        // remove partial message and replace with the latest version
        text.deleteText (currentTextLength, text.getLength ());
        text.appendText (currentMessage.toString ());
      }
    }
  }

  // Console lines
  public void addLines2 (String[] lines, int firstLine, int lastLine)
  {
    for (int i = firstLine; i < lastLine; i++)
    {
      String line = lines[i];
      Matcher m = messagePattern.matcher (line);
      if (m.matches ())
      {
        currentMessage = new ConsoleMessage (line);
        text.appendText ("\n");
        currentTextLength = text.getLength ();

        messages.add (currentMessage);
        fireConsoleMessage (currentMessage);
        text.appendText (currentMessage.toString ());
      }
      else
      {
        currentMessage.add (line.substring (5));
        text.deleteText (currentTextLength, text.getLength ());
        text.appendText (currentMessage.toString ());
      }
    }
  }

  TextArea getTextArea ()
  {
    return text;
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