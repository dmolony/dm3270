package com.bytezone.dm3270.session;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.streams.TelnetSocket.Source;
import com.bytezone.dm3270.utilities.Dm3270Utility;

public class SessionReader
{
  private final String name;
  private final String firstLetter;
  private final List<String> lines;
  private int nextLine;

  private boolean genuine;
  private boolean returnGenuine;

  private LocalDateTime dateTime;
  private LocalDateTime returnDateTime;

  private String label;
  private String returnLabel;

  public byte[] buffer = new byte[16500];       // see also TelnetProcessor

  public SessionReader (Source source, List<String> lines)
  {
    this.name = source == Source.CLIENT ? "Client" : "Server";
    firstLetter = name.substring (0, 1);
    this.lines = new ArrayList<> ();
    this.lines.addAll (lines);
    skipToNext ();
  }

  public SessionReader (Source source, Path path)
  {
    this.name = source == Source.CLIENT ? "Client" : "Server";
    firstLetter = name.substring (0, 1);
    lines = readFile (path);
    skipToNext ();
  }

  private List<String> readFile (Path path)
  {
    List<String> lines = null;
    try
    {
      lines = Files.readAllLines (path);
    }
    catch (IOException e)
    {
      e.printStackTrace ();
      if (lines == null)
        lines = new ArrayList<> ();
    }
    return lines;
  }

  public byte[] nextBuffer () throws Exception
  {
    int bytesRead = next ();
    byte[] data = new byte[bytesRead];
    System.arraycopy (buffer, 0, data, 0, bytesRead);
    return data;
  }

  public int next () throws Exception
  {
    if (nextLine >= lines.size ())
      return 0;

    int bytesWritten = 0;
    returnGenuine = genuine;
    returnDateTime = dateTime;
    returnLabel = label;

    for (String line : getBufferLines ())
    {
      while (!line.isEmpty ())
      {
        String b = line.substring (0, 2).trim ();     // get hex value
        if (b.isEmpty ())
          break;
        buffer[bytesWritten++] = (byte) Integer.parseInt (b, 16);
        line = line.substring (3);                    // remove it from the line
      }
    }
    return bytesWritten;
  }

  public boolean isGenuine ()
  {
    return returnGenuine;
  }

  public LocalDateTime getDateTime ()
  {
    return returnDateTime;
  }

  public String getLabel ()
  {
    return returnLabel;
  }

  // returns a List of lines contained in a single buffer
  private List<String> getBufferLines ()
  {
    // file structure
    int firstHexDigit = 0;

    List<String> list = new ArrayList<> ();
    while (nextLine < lines.size ())
    {
      String line = lines.get (nextLine);

      if (line.isEmpty ())
        break;

      char firstChar = line.charAt (0);
      if (firstChar < '0' || firstChar > '9')
        break;

      if (firstHexDigit == 0)
        firstHexDigit = line.charAt (7) == ' ' ? 8 : 6;

      line = line.substring (firstHexDigit);
      if (line.length () > 48)                    // 16 hex values plus spaces
        line = line.substring (0, 48);            // leave a space on the end
      list.add (line);

      nextLine++;
    }

    skipToNext ();
    return list;
  }

  private void skipToNext ()
  {
    while (nextLine < lines.size ())
    {
      String line = lines.get (nextLine++);

      // skip to next line with our name
      if (line.startsWith (firstLetter))
      {
        if (line.length () > 7)
          genuine = line.charAt (7) != '*';
        else
          genuine = true;

        if (line.length () > 9)
          dateTime = LocalDateTime.parse (line.substring (9).trim ());
        else
          dateTime = null;

        while (nextLine < lines.size ())            // skip all comments and blank lines
        {
          if (lines.get (nextLine).startsWith ("0"))    // first buffer line
            return;
          nextLine++;
        }
        return;
      }
      else if (line.startsWith ("##"))
        label = line.substring (3);
    }

    nextLine++;         // make it greater than lines.size()
  }

  public int nextLineNo ()
  {
    return nextLine;
  }

  public static void main (String[] args) throws IOException
  {
    //    String base = "/Users/denismolony/Documents/workspace/"
    //        + "dm3270/src/com/bytezone/dm3270/application/";
    String base = "/Users/denismolony/Dropbox/Mainframe documentation/";
    //        + "dm3270/src/com/bytezone/dm3270/application/";
    //    String filename = String.format ("%smf.txt", base);
    String filename = String.format ("%sspy001.txt", base);
    int mode = 2;

    SessionReader server = new SessionReader (Source.SERVER, Paths.get (filename));
    SessionReader client = new SessionReader (Source.CLIENT, Paths.get (filename));

    try
    {
      while (client.nextLineNo () != server.nextLineNo ())    // both finished
      {
        if (client.nextLineNo () < server.nextLineNo ())
        {
          System.out.println ("-----------------< Client >--------------------");
          while (client.nextLineNo () < server.nextLineNo ())
            if (mode == 1)
              print (client.getBufferLines ());
            else
              print (client.nextBuffer ());
        }
        else
        {
          System.out.println ("-----------------< Server >--------------------");
          while (server.nextLineNo () < client.nextLineNo ())
            if (mode == 1)
              print (server.getBufferLines ());
            else
              print (server.nextBuffer ());
        }
      }
    }
    catch (Exception e)
    {
      System.out.println ("Exception reading file");
    }
  }

  private static void print (byte[] buffer)
  {
    System.out.println (Dm3270Utility.toHex (buffer));
    if (buffer[buffer.length - 1] == (byte) 0xEF)
      System.out.println ();
  }

  private static void print (List<String> list)
  {
    for (String line : list)
      System.out.println (line);
    System.out.println ();
  }
}