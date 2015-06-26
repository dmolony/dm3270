package com.bytezone.dm3270.filetransfer;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class FileStructure
{
  boolean hasCRLF;
  boolean hasASA;
  int lineSize;
  String encoding;
  List<String> lines = new ArrayList<> ();

  public FileStructure (byte[] buffer)
  {
    encoding = getEncoding (buffer);
    hasCRLF = hasCRLF (buffer);

    // if that was inconclusive, try the common buffer lengths
    if (!hasCRLF)
    {
      int[] lineSizes = { 133, 132, 80 };
      for (int ls : lineSizes)
        if (buffer.length % ls == 0)
        {
          lineSize = ls;
          break;
        }
    }

    // split the buffer up into lines using the chosen encoding and line method
    try
    {
      if (hasCRLF)
      {
        int lineStart = 0;
        for (int ptr = 1; ptr < buffer.length; ptr++)
        {
          if (buffer[ptr] == 0x0A && buffer[ptr - 1] == 0x0D)
          {
            String line = new String (buffer, lineStart, ptr - lineStart - 1, encoding);
            lines.add (line);
            lineStart = ptr + 1;
          }
        }
      }
      else
      {
        if (lineSize == 0)          // couldn't determine a line size, just go with 80
          lineSize = 80;

        for (int ptr = 0; ptr < buffer.length; ptr += lineSize)
        {
          if (ptr + lineSize >= buffer.length)
            lineSize = buffer.length - ptr;
          String line = new String (buffer, ptr, lineSize, encoding);
          String trimmedLine = line.replaceAll ("\\s*$", "");     // trim right
          lines.add (trimmedLine);
        }
      }
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }

    // check for printer control characters
    hasASA = hasASA (lines);
  }

  private String getEncoding (byte[] buffer)
  {
    int max = Math.min (200, buffer.length);
    int ascii = 0;
    int ebcdic = 0;

    for (int i = 0; i < max; i++)
      if (buffer[i] == 0x20)
        ascii++;
      else if (buffer[i] == 0x40)
        ebcdic++;

    return ascii > ebcdic ? "UTF8" : "CP1047";
  }

  private boolean hasCRLF (byte[] buffer)
  {
    int lastCRLF = 0;
    int totalCRLF = 0;
    int maxLineLength = 150;
    int max = Math.min (5 * maxLineLength, buffer.length);

    for (int i = 1; i < max; i++)
    {
      if (buffer[i] == 0x0A && buffer[i - 1] == 0x0D)
      {
        int recordLength = i - lastCRLF;
        if (recordLength > maxLineLength)
          return false;
        lastCRLF = i;
        ++totalCRLF;
      }
    }

    return totalCRLF > 0;
  }

  private boolean hasASA (List<String> lines)
  {
    for (String line : lines)
      if (!line.isEmpty ())
      {
        char asa = line.charAt (0);
        if (asa != ' ' && asa != '-' && asa != '0' && asa != '1')
          return false;
      }

    return true;
  }
}