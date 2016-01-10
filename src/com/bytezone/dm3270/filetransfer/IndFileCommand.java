package com.bytezone.dm3270.filetransfer;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.filetransfer.Transfer.TransferType;

public class IndFileCommand
{
  private static final Pattern jclPattern = Pattern.compile (".*\\.(CNTL|JCL)[.(].*\\)");
  private static final Pattern procPattern =
      Pattern.compile (".*\\.(PROC|PARM)LIB[.(].*\\)");

  private String command;
  private String datasetName;
  private boolean hasHLQ;
  private String hlq;
  private String prefix;
  private boolean crlf;
  private boolean ascii;
  private boolean append;
  private String recfm;
  private String lrecl;
  private String blksize;
  private String space;
  private String units;

  private byte[] buffer;
  private File localFile;
  private final TransferType transferType;

  public IndFileCommand (TransferType transferType, String datasetName, File localFile)
  {
    this.datasetName = datasetName;
    this.transferType = transferType;
    this.localFile = localFile;

    examineDataset ();
  }

  public IndFileCommand (TransferType transferType, String datasetName, byte[] buffer)
  {
    this.datasetName = datasetName;
    this.transferType = transferType;
    this.buffer = buffer;

    examineDataset ();
  }

  public IndFileCommand (String command)
  {
    this.command = command;
    command = command.toLowerCase ().trim ();
    if (command.startsWith ("tso "))
      command = command.substring (4).trim ();

    String[] chunks = command.split ("\\s+");

    if (chunks.length == 0
        || (!"ind$file".equals (chunks[0]) && !"ind£file".equals (chunks[0])))
      throw new IllegalArgumentException ("No IND$FILE in that command");

    if (chunks.length == 1 || (!chunks[1].equals ("put") && !chunks[1].equals ("get")))
      throw new IllegalArgumentException ("No PUT or GET in that command");

    if (chunks.length == 2)
      throw new IllegalArgumentException ("No dataset name in that command");

    transferType = "put".equals (chunks[1]) ? TransferType.UPLOAD : TransferType.DOWNLOAD;
    datasetName = chunks[2];
    if (datasetName.startsWith ("'") && datasetName.endsWith ("'"))
    {
      if (datasetName.length () == 2)
        throw new IllegalArgumentException ("No dataset name in that command");

      datasetName = datasetName.substring (1, datasetName.length () - 1);
      hasHLQ = true;
      int pos = datasetName.indexOf ('.');
      if (pos > 0)
        hlq = datasetName.substring (0, pos);
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

  private void examineDataset ()
  {
    int pos = datasetName.indexOf ('.');
    if (pos > 0)
    {
      hasHLQ = true;
      hlq = datasetName.substring (0, pos);
    }

    setCommandText ();
  }

  private void setCommandText ()
  {
    Matcher matcher1 = jclPattern.matcher (datasetName);
    Matcher matcher2 = procPattern.matcher (datasetName);
    boolean useCrlf = matcher1.matches () || matcher2.matches ();

    String commandDatasetName = "";
    if (!prefix.isEmpty () && datasetName.startsWith (prefix))
    {
      if (datasetName.length () == prefix.length ())
      {
        command = "";
        return;
      }
      commandDatasetName = datasetName.substring (prefix.length () + 1);
    }
    else
      commandDatasetName = "'" + datasetName + "'";

    String options = useCrlf ? " ASCII CRLF" : "";
    String type = transferType == TransferType.DOWNLOAD ? "GET" : "PUT";

    command = String.format ("IND$FILE %s %s%s", type, commandDatasetName, options);
  }

  public String getCommand ()
  {
    return command;
  }

  public String getDatasetName ()
  {
    return datasetName;
  }

  public void setDatasetName (String datasetName)
  {
    this.datasetName = datasetName;
  }

  public void setPrefix (String prefix)
  {
    this.prefix = prefix;
  }

  public void setTlq (String tlq)
  {
    this.hlq = tlq;
  }

  public boolean hasHLQ ()
  {
    return hasHLQ;
  }

  public void setAscii (boolean value)
  {
    this.ascii = value;
  }

  public boolean getAscii ()
  {
    return ascii;
  }

  public void setCrlf (boolean value)
  {
    this.crlf = value;
  }

  public boolean getCrlf ()
  {
    return crlf;
  }

  public boolean isUpload ()
  {
    return transferType == TransferType.UPLOAD;
  }

  public boolean isDownload ()
  {
    return transferType == TransferType.DOWNLOAD;
  }

  public byte[] getBuffer ()
  {
    return buffer;
  }

  public void setBuffer (byte[] buffer)
  {
    this.buffer = buffer;
  }

  public void setLocalFile (File file)
  {
    this.localFile = file;
  }

  public File getLocalFile ()
  {
    return localFile;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("%nCommand ........ %s", command));
    text.append (String.format ("%nTransfer ....... %s", transferType));
    text.append (String.format ("%nDataset ........ %s", datasetName));
    text.append (String.format ("%nhas HLQ ........ %s", hasHLQ));
    text.append (String.format ("%nHLQ ............ %s", hlq));
    text.append (String.format ("%nPrefix ......... %s", prefix));
    text.append (String.format ("%nFile name ...... %s", localFile));
    text.append (String.format ("%nBuffer length .. %,d",
                                buffer == null ? -1 : buffer.length));
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