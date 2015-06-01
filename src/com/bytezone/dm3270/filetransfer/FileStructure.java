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

    int[] lineSizes = { 80, 132, 133 };
    int lineSize = 80;

    if (buffer[buffer.length - 1] == 0x1A)
    {
      hasCRLF = hasCRLF (buffer);
      if (hasCRLF)
        lineSize = 0;
    }

    if (!hasCRLF)
      for (int ls : lineSizes)
        if (buffer.length % ls == 0)
        {
          lineSize = ls;
          break;
        }

    try
    {
      if (lineSize > 0)
        for (int ptr = 0; ptr < buffer.length; ptr += lineSize)
        {
          String line = new String (buffer, ptr, lineSize, encoding);
          String trimmedLine = line.replaceAll ("\\s*$", "");     // trim right
          lines.add (trimmedLine);
        }
      else
      {
        int lineStart = 0;
        for (int ptr = 1; ptr < buffer.length; ptr++)
        {
          if (buffer[ptr] == 0x0A && buffer[ptr - 1] == 0x0D)
          {
            lines.add (new String (buffer, lineStart, ptr - lineStart - 1, encoding));
            lineStart = ptr + 1;
          }
        }
      }
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }

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

  private boolean hasCRLF (byte[] buffer)
  {
    if (buffer[buffer.length - 1] != 0x1A)
      return false;

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
}