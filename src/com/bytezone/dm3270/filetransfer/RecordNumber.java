package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;

public class RecordNumber extends DataRecord
{
  int recordNumber;

  public RecordNumber (byte[] data, int offset)
  {
    super (data, offset);
    recordNumber = Utility.unsignedLong (data, 2);
  }
}