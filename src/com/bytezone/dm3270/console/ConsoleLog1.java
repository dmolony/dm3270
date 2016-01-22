package com.bytezone.dm3270.console;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.control.TextArea;
import javafx.scene.text.Font;

public class ConsoleLog1
{
  private static final Pattern codePattern =
      Pattern.compile ("([A-Z]{3,4}[0-9]{3,5}[A-Z]) (.*)");
  private final List<String> log = new ArrayList<> ();
  //  private final String[] lines = new String[20];
  private final TextArea text = new TextArea ();
  //  private final String previousMessage = "";

  public ConsoleLog1 (Font font)
  {
    text.setFont (font);
  }

  public void addLines (String[] lines, int firstLine, int lastLine)
  {
    int max = lastLine;
    for (int lineNo = lastLine - 1; lineNo >= firstLine; lineNo--)
    {
      String line = lines[lineNo].substring (2);

      System.out.println (line);

      Matcher m = codePattern.matcher (line);
      if (m.matches ())
      {
        log.add (lines[lineNo]);
        String tempLine = lines[lineNo].trim ();
        int length = tempLine.length ();
        text.appendText ("\n" + tempLine);

        for (int i = lineNo + 1; i < max; i++)
        {
          log.add (lines[i]);
          tempLine = lines[i].trim ();

          length += tempLine.length () + 1;
          if (length < 140)
            text.appendText (" " + tempLine);
          else
          {
            text.appendText ("\n        " + tempLine);
            length = tempLine.length ();
          }
        }
        max = lineNo;
      }
    }
  }

  //  private void addLines (String message)
  //  {
  //    // break message up into 80-character lines
  //    int totLines = 0;
  //    for (int ptr = 0; ptr < message.length (); ptr += 80)
  //    {
  //      int max = Math.min (ptr + 80, message.length ());
  //      String line = message.substring (ptr, max);
  //      if (line.trim ().isEmpty ())
  //        break;
  //      lines[totLines++] = line;
  //    }
  //
  //    // the first screen usually contains many lines, so add them all
  //    if (log.size () == 0)
  //    {
  //      log.add (lines[0]);
  //      text.appendText (lines[0].substring (2));
  //      for (int lineNo = 1; lineNo < totLines; lineNo++)
  //      {
  //        log.add (lines[lineNo]);
  //        text.appendText ("\n" + lines[lineNo].substring (2));
  //      }
  //      return;
  //    }
  //
  //    // look for the last message start
  //    for (int lineNo = totLines - 1; lineNo >= 0; lineNo--)
  //    {
  //      String line = lines[lineNo].substring (2);
  //      //      String code = line.substring (0, 8);
  //
  //      if (line.startsWith ("        "))
  //      {
  //        log.add (line);
  //        text.appendText ("\n" + line);
  //        break;
  //      }
  //
  //      if (line.startsWith ("++"))
  //      {
  //        log.add (line);
  //        text.appendText ("\n        " + line);
  //        break;
  //      }
  //
  //      // this should use a regex
  //      //      if ((code.charAt (0) == 'I' && code.charAt (6) == 'I')
  //      //          || (code.charAt (0) == 'C' && code.charAt (6) == 'I')
  //      //          || (code.charAt (0) == 'C' && code.charAt (7) == 'I'))
  //      Matcher m = codePattern.matcher (line);
  //      if (m.matches ())
  //      {
  //        System.out.println (m.group (1));
  //        log.add (lines[lineNo]);
  //        String tempLine = lines[lineNo].trim ();
  //        int length = tempLine.length ();
  //        text.appendText ("\n" + tempLine);
  //
  //        for (int i = lineNo + 1; i < totLines; i++)
  //        {
  //          log.add (lines[i]);
  //          tempLine = lines[i].trim ();
  //
  //          length += tempLine.length () + 1;
  //          if (length < 140)
  //            text.appendText (" " + tempLine);
  //          else
  //          {
  //            text.appendText ("\n        " + tempLine);
  //            length = tempLine.length ();
  //          }
  //        }
  //        break;
  //      }
  //      else
  //        System.out.printf ("Not matched: [%s]%n", line);
  //    }
  //  }

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