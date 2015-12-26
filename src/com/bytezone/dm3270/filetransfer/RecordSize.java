package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.utilities.Dm3270Utility;

public class RecordSize extends TransferRecord
{
  int recordSize1;
  int recordSize2;

  public RecordSize (byte[] data, int offset)
  {
    super (data, offset);
    recordSize1 = Dm3270Utility.unsignedShort (data, offset + 2);
    recordSize2 = Dm3270Utility.unsignedShort (data, offset + 4);
  }

  @Override
  public String toString ()
  {
    return String.format ("rec size  : %s (%,d or %,d)", Dm3270Utility.toHexString (data),
                          recordSize1, recordSize2);
  }
}