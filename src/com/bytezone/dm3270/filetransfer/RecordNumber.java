package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;

public class RecordNumber extends DataRecord
{
  public static final byte RECORD_LENGTH = 6;
  static final byte type = 0x63;

  int recordNumber;

  public RecordNumber (int recordNumber)
  {
    super (type, 6);
    Utility.packUnsignedLong (recordNumber, data, 2);
  }

  public RecordNumber (byte[] data, int offset)
  {
    super (data, offset);
    recordNumber = Utility.unsignedLong (data, 2);
  }

  @Override
  public String toString ()
  {
    return String.format ("recnum    : %s", Utility.toHexString (data));
  }
}