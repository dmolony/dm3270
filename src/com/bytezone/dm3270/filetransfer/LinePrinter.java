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
  private final boolean hasASA;
  private final StringBuilder text = new StringBuilder ();

  public LinePrinter (int pageSize, boolean hasASA)
  {
    this.pageSize = pageSize;
    this.hasASA = hasASA;
  }

  public void printLine (String line)
  {
    if (hasASA)
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

    if (hasASA)
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

  public void printBuffer (byte[] buffer)
  {
    int lineStart = 0;
    for (int ptr = 1; ptr < buffer.length; ptr++)
    {
      if (buffer[ptr] == 0x0D && buffer[ptr - 1] == 0x0A)
      {
        printLine (getString (buffer, lineStart, ptr - lineStart - 1));
        lineStart = ptr + 1;
      }
    }
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