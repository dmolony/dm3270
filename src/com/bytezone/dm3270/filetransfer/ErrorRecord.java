package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;

public class ErrorRecord extends DataRecord
{
  private final int errorNumber;
  private final String errorText;

  public ErrorRecord (byte[] data, int offset)
  {
    super (data, offset);
    errorNumber = Utility.unsignedShort (data, offset + 2);
    if (errorNumber == 0x0100)
      errorText = "Command failed";
    else if (errorNumber == 0x2200)
      errorText = "EOF";
    else
      errorText = "Unknown error";
  }

  @Override
  public String toString ()
  {
    return String.format ("error     : %s - %s", Utility.toHexString (data), errorText);
  }
}