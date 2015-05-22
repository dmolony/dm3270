package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;

public class DataHeader extends DataRecord
{
  int bufferLength;

  public DataHeader (byte[] data, int offset)
  {
    super (new byte[5]);
    System.arraycopy (data, offset, this.data, 0, 5);
    bufferLength = Utility.unsignedShort (this.data, 3);
  }

  @Override
  public String toString ()
  {
    return String.format ("header    : %s", Utility.toHexString (data));
  }
}