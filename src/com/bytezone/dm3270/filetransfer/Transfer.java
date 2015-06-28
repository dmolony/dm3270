package com.bytezone.dm3270.filetransfer;

import java.util.ArrayList;
import java.util.List;

// Control Unit Terminal (CUT) - Buffered
// Distributed Function Terminal (DFT) - WSF

public class Transfer
{
  private TransferType type;
  private String fileName;
  private boolean crlf;
  private boolean ascii;
  private boolean append;
  private String recfm;
  private String lrecl;
  private String blksize;
  private String space;
  private String direction;

  List<FileTransferOutboundSF> outboundRecords = new ArrayList<> ();
  List<DataHeader> dataBuffers = new ArrayList<> ();
  int dataLength;

  enum TransferType
  {
    MSG, DATA
  }

  public void add (FileTransferOutboundSF outboundRecord)
  {
    outboundRecords.add (outboundRecord);
    if (type == null)
      type = outboundRecord.transferType;
  }

  public int add (DataHeader dataHeader)
  {
    if (dataBuffers.contains (dataHeader))
      return dataBuffers.indexOf (dataHeader) + 1;

    dataBuffers.add (dataHeader);
    dataLength += dataHeader.size ();
    return dataBuffers.size ();
  }

  public byte[] combineDataBuffers ()
  {
    byte[] fullBuffer = new byte[dataLength];

    int ptr = 0;
    for (DataHeader dataHeader : dataBuffers)
    {
      byte[] buffer = dataHeader.getBuffer ();
      System.arraycopy (buffer, 0, fullBuffer, ptr, buffer.length);
      ptr += buffer.length;
    }

    return fullBuffer;
  }

  public int size ()
  {
    return dataBuffers.size ();
  }

  public boolean isData ()
  {
    return type == TransferType.DATA;
  }

  public boolean isMessage ()
  {
    return type == TransferType.MSG;
  }

  boolean hasMoreData ()
  {
    return false;
  }

  public void setTransferCommand (String command)
  {
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

    assert"ind$file".equals (chunks[0]);
    assert"put".equals (chunks[1]) || "get".equals (chunks[1]);

    this.fileName = chunks[2];
    this.direction = chunks[1];

    int lengthMinusOne = chunks.length - 1;
    for (int i = 3; i < chunks.length; i++)
    {
      if (chunks[i].equals ("crlf"))
        crlf = true;
      if (chunks[i].equals ("ascii"))
        ascii = true;
      if (chunks[i].equals ("append"))
        append = true;

      if (chunks[i].equals ("recfm") && i < lengthMinusOne)
        recfm = chunks[i + 1];
      if (chunks[i].equals ("lrecl") && i < lengthMinusOne)
        lrecl = chunks[i + 1];
      if (chunks[i].equals ("blksize") && i < lengthMinusOne)
        blksize = chunks[i + 1];
      if (chunks[i].equals ("space") && i < lengthMinusOne)
        space = chunks[i + 1];

      if (chunks[i].startsWith ("recfm("))
        recfm = chunks[i].substring (5);
      if (chunks[i].startsWith ("lrecl("))
        lrecl = chunks[i].substring (5);
      if (chunks[i].startsWith ("blksize("))
        recfm = chunks[i].substring (7);
      if (chunks[i].startsWith ("space("))
        space = chunks[i].substring (7);
    }

    System.out.println (this);
  }

  public String getFileName ()
  {
    return fileName;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Transfer ...... %s", type));

    int bufno = 0;
    for (DataHeader buffer : dataBuffers)
      text.append (String.format ("%n  Buffer %3d : %,d", bufno++, buffer.size ()));
    text.append (String.format ("%nTotal length .. %,d", dataLength));
    text.append (String.format ("%nDirection ..... %s", direction));
    text.append (String.format ("%nFile name ..... %s", fileName));
    text.append (String.format ("%nCRLF .......... %s", crlf));
    text.append (String.format ("%nASCII ......... %s", ascii));
    text.append (String.format ("%nAPPEND ........ %s", append));
    text.append (String.format ("%nRECFM ......... %s", recfm == null ? "" : recfm));
    text.append (String.format ("%nLRECL ......... %s", lrecl == null ? "" : lrecl));
    text.append (String.format ("%nBLKSIZE ....... %s", blksize == null ? "" : blksize));
    text.append (String.format ("%nSPACE ......... %s", space == null ? "" : space));

    return text.toString ();
  }
}