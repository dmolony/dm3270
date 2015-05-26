package com.bytezone.dm3270.filetransfer;

import java.util.ArrayList;
import java.util.List;

// Control Unit Terminal (CUT)
// Distributed Function Terminal (DFT)

public class Transfer
{
  TransferType type;
  TransferStatus status;

  List<byte[]> messageBuffers = new ArrayList<> ();
  List<byte[]> dataBuffers = new ArrayList<> ();
  int messageLength;
  int dataLength;

  enum TransferType
  {
    MSG, DATA
  }

  enum TransferStatus
  {
    OPEN_DATA, OPEN_MSG, TRANSFER, CLOSE
  }

  //  public void setStatus (TransferStatus status)
  //  {
  //    this.status = status;
  //  }

  public void setCurrentTransfer (String type)
  {
    if ("FT:DATA".equals (type))
    {
      this.type = TransferType.DATA;
      dataBuffers.clear ();
      status = TransferStatus.OPEN_DATA;
    }
    else if ("FT:MSG ".equals (type))
    {
      this.type = TransferType.MSG;
      messageBuffers.clear ();
      status = TransferStatus.OPEN_MSG;
    }
    else
      throw new IllegalArgumentException ();
  }

  public void add (byte[] buffer)
  {
    if (type == TransferType.DATA)
    {
      dataBuffers.add (buffer);
      dataLength += buffer.length;
    }
    else
    {
      messageBuffers.add (buffer);
      messageLength += buffer.length;
    }
    status = TransferStatus.TRANSFER;
  }

  public int size ()
  {
    return isData () ? dataBuffers.size () : messageBuffers.size ();
  }

  public boolean isData ()
  {
    return type == TransferType.DATA;
  }

  public boolean isMessage ()
  {
    return type == TransferType.MSG;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Transfer ... : %s", type));

    int bufno = 0;
    if (type == TransferType.DATA)
    {
      for (byte[] buffer : dataBuffers)
        text.append (String.format ("%n  Buffer %3d : %,d", bufno++, buffer.length));
      text.append (String.format ("%nTotal length : %,d", dataLength));
    }
    else
    {
      for (byte[] buffer : messageBuffers)
        text.append (String.format ("%n  Buffer %3d : %,d", bufno++, buffer.length));
      text.append (String.format ("%nTotal length : %,d", messageLength));
    }

    return text.toString ();
  }
}