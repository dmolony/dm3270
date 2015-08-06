package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;

public class RecordSize extends TransferRecord
{
  int recordSize1;
  int recordSize2;

  public RecordSize (byte[] data, int offset)
  {
    super (data, offset);
    recordSize1 = Utility.unsignedShort (data, offset + 2);
    recordSize2 = Utility.unsignedShort (data, offset + 4);
  }

  @Override
  public String toString ()
  {
    return String.format ("rec size  : %s (%,d or %,d)", Utility.toHexString (data),
                          recordSize1, recordSize2);
  }
}