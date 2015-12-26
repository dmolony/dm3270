package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.utilities.Dm3270Utility;

public class RecordNumber extends TransferRecord
{
  static final byte RECORD_LENGTH = 6;
  static final byte TYPE = 0x63;

  int recordNumber;

  public RecordNumber (int recordNumber)
  {
    super (TYPE, RECORD_LENGTH);
    Dm3270Utility.packUnsignedLong (recordNumber, data, 2);
  }

  public RecordNumber (byte[] data, int offset)
  {
    super (data, offset);
    recordNumber = Dm3270Utility.unsignedLong (data, 2);
  }

  @Override
  public String toString ()
  {
    return String.format ("recnum    : %s", Dm3270Utility.toHexString (data));
  }
}