package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;

public class ErrorRecord extends TransferRecord
{
  public static final int EOF = 0x2200;
  public static final int CANCEL = 0x4700;
  public static final int RECORD_LENGTH = 4;
  private static final byte type = 0x69;

  private final int errorNumber;
  private String errorText;

  public ErrorRecord (int error)
  {
    super (type, RECORD_LENGTH);
    this.errorNumber = error;
    Utility.packUnsignedShort (error, data, 2);
  }

  public ErrorRecord (byte[] data, int offset)
  {
    super (data, offset);
    errorNumber = Utility.unsignedShort (data, offset + 2);
    if (errorNumber == 0x0100)
      errorText = "Command failed";
    else if (errorNumber == EOF)
      errorText = "EOF";
    else if (errorNumber == CANCEL)
      errorText = "Cancel";
    else
      errorText = "Unknown error";
  }

  @Override
  public String toString ()
  {
    return String.format ("error     : %s - %s", Utility.toHexString (data), errorText);
  }
}