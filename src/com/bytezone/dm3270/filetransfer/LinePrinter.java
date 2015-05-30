package com.bytezone.dm3270.filetransfer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;

// http://en.wikipedia.org/wiki/ASA_carriage_control_characters

public class LinePrinter
{
  private static final String EBCDIC = "CP1047";

  private int currentLine;
  private final int pageSize;
  private final boolean controlCharacters;
  private final StringBuilder text = new StringBuilder ();

  public LinePrinter (int pageSize, boolean controlCharacters)
  {
    this.pageSize = pageSize;
    this.controlCharacters = controlCharacters;
  }

  public void printLine (String line)
  {
    if (controlCharacters)
      switch (line.charAt (0))
      {
        case '-':
          lineFeed ();
        case '0':
          lineFeed ();
        case ' ':
          lineFeed ();
          break;

        case '1':
          formFeed ();
          break;

        default:
          System.err.println ("Unknown control character : " + line.charAt (0));
          lineFeed ();
      }

    String trimmedLine = line.replaceAll ("\\s*$", "");     // trim right

    if (controlCharacters)
      text.append (trimmedLine.isEmpty () ? "" : trimmedLine.substring (1));
    else
    {
      text.append (trimmedLine);
      lineFeed ();
    }
  }

  public void printFile (Path file, int reclen)
  {
    try
    {
      printBuffer (Files.readAllBytes (file), reclen);
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }

  public void printBuffer (byte[] buffer, int reclen)
  {
    for (int ptr = 0; ptr < buffer.length; ptr += reclen)
      printLine (getString (buffer, ptr, reclen));
  }

  public String getOutput ()
  {
    return text.toString ();
  }

  private void lineFeed ()
  {
    text.append ("\n");
    if (++currentLine >= pageSize)
      currentLine = 0;
    return;
  }

  private void formFeed ()
  {
    if (text.length () == 0)
      return;

    do
    {
      lineFeed ();
    } while (currentLine > 0);
  }

  private String getString (byte[] buffer, int offset, int length)
  {
    try
    {
      int last = offset + length;
      if (last > buffer.length)
        length = buffer.length - offset - 1;
      return new String (buffer, offset, length, EBCDIC);
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
      return "FAIL";
    }
  }
}