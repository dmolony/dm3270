package com.bytezone.dm3270.filetransfer;

import java.util.ArrayList;
import java.util.List;

// Control Unit Terminal (CUT) - Buffered
// Distributed Function Terminal (DFT) - WSF

public class Transfer
{
  private static int INBOUND_MAX_BUFFER_SIZE = 2048;

  private TransferContents transferContents;
  private TransferType transferType;

  private String fileName;
  private boolean crlf;
  private boolean ascii;
  private boolean append;
  private String recfm;
  private String lrecl;
  private String blksize;
  private String space;
  private String direction;
  private String units;

  List<FileTransferOutboundSF> outboundRecords = new ArrayList<> ();
  List<DataRecord> dataRecords = new ArrayList<> ();
  int dataLength;

  byte[] inboundBuffer;
  int inboundBufferPtr;

  public enum TransferContents
  {
    MSG, DATA
  }

  public enum TransferType
  {
    SEND, RECEIVE
  }

  public void add (FileTransferOutboundSF outboundRecord)
  {
    outboundRecords.add (outboundRecord);

    if (transferContents == null)
      transferContents = outboundRecord.transferContents;
    if (transferType == null)
      transferType = outboundRecord.transferType;
  }

  int add (DataRecord dataRecord)
  {
    if (dataRecords.contains (dataRecord))
      return dataRecords.indexOf (dataRecord) + 1;

    dataRecords.add (dataRecord);
    dataLength += dataRecord.getBufferLength ();

    return dataRecords.size ();
  }

  public boolean isSendData ()
  {
    return transferContents == TransferContents.DATA && transferType == TransferType.SEND;
  }

  public byte[] combineDataBuffers ()
  {
    byte[] fullBuffer = new byte[dataLength];

    int ptr = 0;
    for (DataRecord dataRecord : dataRecords)
      ptr = dataRecord.packBuffer (fullBuffer, ptr);

    return fullBuffer;
  }

  // called from FileStage.setBuffer()
      void setTransferBuffer (byte[] buffer)
  {
    inboundBuffer = buffer;
    inboundBufferPtr = 0;
  }

  DataRecord getDataHeader ()
  {
    assert hasMoreData ();

    int buflen = Math.min (INBOUND_MAX_BUFFER_SIZE, getBytesLeft ());
    DataRecord dataHeader =
        new DataRecord (inboundBuffer, inboundBufferPtr, buflen, false);
    inboundBufferPtr += buflen;
    add (dataHeader);

    return dataHeader;
  }

  int size ()
  {
    return dataRecords.size ();
  }

  public TransferContents getTransferContents ()
  {
    return transferContents;
  }

  public TransferType getTransferType ()
  {
    return transferType;
  }

  boolean cancelled ()
  {
    return false;
  }

  boolean hasMoreData ()
  {
    return getBytesLeft () > 0;
  }

  int getBytesLeft ()
  {
    if (inboundBuffer == null)
      return 0;
    return inboundBuffer.length - inboundBufferPtr;
  }

  // optional step to use the TSO command that started the transfer
      void setTransferCommand (String command)
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
        blksize = chunks[i].substring (7);
      if (chunks[i].startsWith ("space("))
      {
        space = chunks[i].substring (5);
        if (chunks[i - 1].startsWith ("cyl") || chunks[i - 1].startsWith ("track"))
          units = chunks[i - 1];
      }
    }
  }

  public String getFileName ()
  {
    return fileName;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Contents ....... %s%n", transferContents));
    text.append (String.format ("Type ........... %s", transferType));

    int bufno = 0;
    for (DataRecord dataRecord : dataRecords)
      text.append (String.format ("%n  Buffer %3d ...  %,8d", bufno++,
                                  dataRecord.getBufferLength ()));

    text.append (String.format ("%nLength ......... %,9d", dataLength));
    text.append (String.format ("%nCommand ........ %s", direction));
    text.append (String.format ("%nFile name ...... %s", fileName));
    text.append (String.format ("%nCRLF ........... %s", crlf));
    text.append (String.format ("%nASCII .......... %s", ascii));
    text.append (String.format ("%nAPPEND ......... %s", append));
    text.append (String.format ("%nRECFM .......... %s", recfm == null ? "" : recfm));
    text.append (String.format ("%nLRECL .......... %s", lrecl == null ? "" : lrecl));
    text.append (String.format ("%nBLKSIZE ........ %s", blksize == null ? "" : blksize));
    text.append (String.format ("%nUNITS .......... %s", units == null ? "" : units));
    text.append (String.format ("%nSPACE .......... %s", space == null ? "" : space));

    text.append (String.format ("%ninbuf length ... %d",
                                inboundBuffer == null ? -1 : inboundBuffer.length));
    text.append (String.format ("%nin ptr ......... %d", inboundBufferPtr));

    return text.toString ();
  }
}