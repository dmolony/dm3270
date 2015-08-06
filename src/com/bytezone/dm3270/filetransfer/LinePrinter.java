package com.bytezone.dm3270.filetransfer;

// http://en.wikipedia.org/wiki/ASA_carriage_control_characters

public class LinePrinter
{
  //  private static final String EBCDIC = "CP1047";
  //  //  private static final String ASCII = "UTF8";
  //
  //  private int currentLine;
  //  private final int pageSize;
  //  private final FileStructure fileStructure;
  //
  //  private final StringBuilder text = new StringBuilder ();
  //
  //  public LinePrinter (int pageSize, FileStructure fileStructure)
  //  {
  //    this.pageSize = pageSize;
  //    this.fileStructure = fileStructure;
  //  }
  //
  //  public void printLine (String line)
  //  {
  //    if (fileStructure.hasASA)
  //    {
  //      if (line.isEmpty ())
  //        lineFeed ();
  //      else
  //      {
  //        switch (line.charAt (0))
  //        {
  //          case '-':
  //            lineFeed ();
  //          case '0':
  //            lineFeed ();
  //          case ' ':
  //            lineFeed ();
  //            break;
  //
  //          case '1':
  //            formFeed ();
  //            break;
  //
  //          default:
  //            System.err.println ("Unknown control character : " + line.charAt (0));
  //            lineFeed ();
  //        }
  //        text.append (line.substring (1));
  //      }
  //    }
  //    else
  //    {
  //      text.append (line);
  //      lineFeed ();
  //    }
  //  }
  //
  //  public void printFile (Path file, int reclen)
  //  {
  //    try
  //    {
  //      printBuffer (Files.readAllBytes (file), reclen);
  //    }
  //    catch (IOException e)
  //    {
  //      e.printStackTrace ();
  //    }
  //  }
  //
  //  public void printBuffer (byte[] buffer, int reclen)
  //  {
  //    for (int ptr = 0; ptr < buffer.length; ptr += reclen)
  //      printLine (getString (buffer, ptr, reclen, EBCDIC));
  //  }
  //
  //  public void printBuffer ()
  //  {
  //    for (String line : fileStructure.lines)
  //      printLine (line);
  //  }
  //
  //  public String getOutput ()
  //  {
  //    return text.toString ();
  //  }
  //
  //  private void lineFeed ()
  //  {
  //    text.append ("\n");
  //    if (++currentLine >= pageSize)
  //      currentLine = 0;
  //    return;
  //  }
  //
  //  private void formFeed ()
  //  {
  //    if (text.length () == 0)
  //      return;
  //
  //    do
  //    {
  //      lineFeed ();
  //    } while (currentLine > 0);
  //  }
  //
  //  private String getString (byte[] buffer, int offset, int length, String encoding)
  //  {
  //    try
  //    {
  //      int last = offset + length;
  //      if (last > buffer.length)
  //        length = buffer.length - offset - 1;
  //      return new String (buffer, offset, length, encoding);
  //    }
  //    catch (UnsupportedEncodingException e)
  //    {
  //      e.printStackTrace ();
  //      return "FAIL";
  //    }
  //  }
}