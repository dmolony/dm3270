package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;

public class RecordSize extends DataRecord
{
  int recordSize;

  public RecordSize (byte[] data, int offset)
  {
    super (data, offset);
    recordSize = Utility.unsignedShort (data, 2);
  }

  @Override
  public String toString ()
  {
    return String.format ("rec size  : %s (%,d)", Utility.toHexString (data), recordSize);
  }
}