package com.bytezone.dm3270.filetransfer;

import java.util.ArrayList;
import java.util.List;

// Control Unit Terminal (CUT)
// Distributed Function Terminal (DFT)

public class Transfer
{
  private TransferType type;
  private String fileName;

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

  public void setFileName (String fileName)
  {
    fileName = fileName.toLowerCase ();
    if (fileName.startsWith ("tso ind$file"))
      this.fileName = fileName.substring (17).trim ();
    else if (fileName.startsWith ("ind$file"))
      this.fileName = fileName.substring (13).trim ();
    else
      this.fileName = fileName;
  }

  public String getFileName ()
  {
    return fileName;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Transfer ... : %s", type));

    int bufno = 0;
    for (DataHeader buffer : dataBuffers)
      text.append (String.format ("%n  Buffer %3d : %,d", bufno++, buffer.size ()));
    text.append (String.format ("%nTotal length : %,d", dataLength));

    return text.toString ();
  }
}