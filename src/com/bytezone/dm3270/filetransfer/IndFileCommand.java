package com.bytezone.dm3270.filetransfer;

import java.nio.file.Path;

public class IndFileCommand
{
  private String command;
  private String fileName;
  private boolean hasTLQ;
  private boolean crlf;
  private boolean ascii;
  private boolean append;
  private String recfm;
  private String lrecl;
  private String blksize;
  private String space;
  private String direction;
  private String units;

  private byte[] buffer;
  private Path localFile;

  public IndFileCommand (String command)
  {
    this.command = command;
    command = command.toLowerCase ().trim ();
    if (command.startsWith ("tso "))
      command = command.substring (4);

    String[] chunks = command.split ("\\s");

    if (false)
    {
      int count = 0;
      for (String chunk : chunks)
        System.out.printf ("Chunk %d: %s%n", count++, chunk);
    }
    assert chunks[0].equals ("ind$file");
    assert chunks[1].equals ("put") || chunks[1].equals ("get");

    // check for a reconnection after an abort during a file transfer
    if (!"ind$file".equals (chunks[0]))
    {
      System.out.printf ("Unexpected error: %s%n", chunks[0]);
      int count = 0;
      for (String chunk : chunks)
        System.out.printf ("Chunk %d: %s%n", count++, chunk);
      return;
    }

    this.direction = chunks[1];
    this.fileName = chunks[2];
    if (fileName.startsWith ("'") && fileName.endsWith ("'") && fileName.length () > 2)
    {
      fileName = fileName.substring (1, fileName.length () - 1);
      hasTLQ = true;
    }

    int lengthMinusOne = chunks.length - 1;
    for (int i = 3; i < chunks.length; i++)
    {
      if (chunks[i].equals ("crlf"))
        crlf = true;
      else if (chunks[i].equals ("ascii"))
        ascii = true;
      else if (chunks[i].equals ("append"))
        append = true;

      if (i < lengthMinusOne)
      {
        if (chunks[i].equals ("recfm"))
          recfm = chunks[i + 1];
        else if (chunks[i].equals ("lrecl"))
          lrecl = chunks[i + 1];
        else if (chunks[i].equals ("blksize"))
          blksize = chunks[i + 1];
        else if (chunks[i].equals ("space"))
          space = chunks[i + 1];
      }

      if (chunks[i].startsWith ("recfm("))
        recfm = chunks[i].substring (5);
      else if (chunks[i].startsWith ("lrecl("))
        lrecl = chunks[i].substring (5);
      else if (chunks[i].startsWith ("blksize("))
        blksize = chunks[i].substring (7);
      else if (chunks[i].startsWith ("space("))
      {
        space = chunks[i].substring (5);
        if (chunks[i - 1].startsWith ("cyl") || chunks[i - 1].startsWith ("track"))
          units = chunks[i - 1];
      }
    }
  }

  public void compareWith (IndFileCommand other)
  {
    System.out.println (this);
    System.out.println (other);
  }

  public String getCommand ()
  {
    return command;
  }

  public String getFileName ()
  {
    return fileName;
  }

  public boolean hasTLQ ()
  {
    return hasTLQ;
  }

  public boolean ascii ()
  {
    return ascii;
  }

  public boolean crlf ()
  {
    return crlf;
  }

  public boolean isPut ()
  {
    return "put".equals (direction);
  }

  public byte[] getBuffer ()
  {
    return buffer;
  }

  public void setBuffer (byte[] buffer)
  {
    this.buffer = buffer;
  }

  public void setLocalPath (Path path)
  {
    this.localFile = path;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("%nCommand ........ %s", direction));
    text.append (String.format ("%nFile name ...... %s", fileName));
    text.append (String.format ("%nPath name ...... %s", localFile));
    text.append (String.format ("%nBuffer length .. %,d",
                                buffer == null ? -1 : buffer.length));
    text.append (String.format ("%nhas TLQ ........ %s", hasTLQ));
    text.append (String.format ("%nCRLF ........... %s", crlf));
    text.append (String.format ("%nASCII .......... %s", ascii));
    text.append (String.format ("%nAPPEND ......... %s", append));
    text.append (String.format ("%nRECFM .......... %s", recfm == null ? "" : recfm));
    text.append (String.format ("%nLRECL .......... %s", lrecl == null ? "" : lrecl));
    text.append (String.format ("%nBLKSIZE ........ %s", blksize == null ? "" : blksize));
    text.append (String.format ("%nUNITS .......... %s", units == null ? "" : units));
    text.append (String.format ("%nSPACE .......... %s", space == null ? "" : space));

    return text.toString ();
  }
}