package com.bytezone.dm3270.console;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.TextArea;
import javafx.scene.text.Font;

public class ConsoleLog1
{
  private final List<String> log = new ArrayList<> ();
  private final String[] lines = new String[20];
  private final TextArea text = new TextArea ();

  public ConsoleLog1 (Font font)
  {
    text.setFont (font);
  }

  public void addLines (String message)
  {
    int totLines = 0;
    for (int ptr = 0; ptr < message.length (); ptr += 80)
    {
      int max = Math.min (ptr + 80, message.length ());
      String line = message.substring (ptr, max);
      if (line.trim ().isEmpty ())
        break;
      lines[totLines++] = line;
    }

    if (log.size () == 0)
    {
      log.add (lines[0]);
      text.appendText (lines[0]);
      for (int lineNo = 1; lineNo < totLines; lineNo++)
      {
        log.add (lines[lineNo]);
        text.appendText ("\n" + lines[lineNo]);
      }
      return;
    }

    for (int lineNo = totLines - 1; lineNo >= 0; lineNo--)
    {
      String line = lines[lineNo];
      String code = line.substring (0, 10);
      if ("          ".equals (code))
      {
        log.add (line);
        text.appendText ("\n" + line);
        break;
      }

      if (code.charAt (2) == '+' && code.charAt (3) == '+')
      {
        log.add (line);
        text.appendText ("\n" + line);
        break;
      }

      // this should use a regex
      if ((code.charAt (2) == 'I' && code.charAt (8) == 'I')
          || (code.charAt (2) == 'C' && code.charAt (9) == 'I'))
      {
        for (int i = lineNo; i < totLines; i++)
        {
          log.add (lines[i]);
          text.appendText ("\n" + lines[i]);
        }
        break;
      }
    }
  }

  TextArea getTextArea ()
  {
    return text;
  }

  public void dump ()
  {
    for (String line : log)
      System.out.println (line);
    System.out.println ("---------------------------------------------------"
        + "----------------------------");
  }
}