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
    // break message up into 80-character lines
    int totLines = 0;
    for (int ptr = 0; ptr < message.length (); ptr += 80)
    {
      int max = Math.min (ptr + 80, message.length ());
      String line = message.substring (ptr, max);
      if (line.trim ().isEmpty ())
        break;
      lines[totLines++] = line;
    }

    // the first screen usually contains many lines, so add them all
    if (log.size () == 0)
    {
      log.add (lines[0]);
      text.appendText (lines[0].substring (2));
      for (int lineNo = 1; lineNo < totLines; lineNo++)
      {
        log.add (lines[lineNo]);
        text.appendText ("\n" + lines[lineNo].substring (2));
      }
      return;
    }

    // look for the last message start
    for (int lineNo = totLines - 1; lineNo >= 0; lineNo--)
    {
      String line = lines[lineNo].substring (2);
      String code = line.substring (0, 8);

      if ("        ".equals (code))
      {
        log.add (line);
        text.appendText ("\n" + line);
        break;
      }

      if (code.charAt (0) == '+' && code.charAt (1) == '+')
      {
        log.add (line);
        text.appendText ("\n        " + line);
        break;
      }

      // this should use a regex
      if ((code.charAt (0) == 'I' && code.charAt (6) == 'I')
          || (code.charAt (0) == 'C' && code.charAt (7) == 'I'))
      {
        log.add (lines[lineNo]);
        String tempLine = lines[lineNo].trim ();
        int length = tempLine.length ();
        text.appendText ("\n" + tempLine);

        for (int i = lineNo + 1; i < totLines; i++)
        {
          log.add (lines[i]);
          tempLine = lines[i].trim ();

          if (length + tempLine.length () + 1 < 140)
          {
            text.appendText (" " + tempLine);
            length += tempLine.length () + 1;
          }
          else
          {
            text.appendText ("\n        " + tempLine);
            length = tempLine.length ();
          }
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