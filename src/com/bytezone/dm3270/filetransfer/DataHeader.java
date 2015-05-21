package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;

public class DataHeader
{
  byte[] header;
  int bufferLength;

  public DataHeader (byte[] data, int offset)
  {
    header = new byte[5];
    System.arraycopy (data, offset, header, 0, header.length);
    bufferLength = Utility.unsignedShort (header, 3);
  }

  @Override
  public String toString ()
  {
    return String.format ("header    : %s", Utility.toHexString (header));
  }
}