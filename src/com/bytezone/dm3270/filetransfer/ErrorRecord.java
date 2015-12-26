package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.utilities.Dm3270Utility;

public class ErrorRecord extends TransferRecord
{
  public static final int EOF = 0x2200;
  public static final int CANCEL = 0x4700;
  public static final int CMD_FAIL = 0x0100;// according to x3270

  static final byte TYPE = 0x69;
  static final byte RECORD_LENGTH = 4;

  private final int errorNumber;
  private String errorText;

  public ErrorRecord (int error)
  {
    super (TYPE, RECORD_LENGTH);
    this.errorNumber = error;
    Dm3270Utility.packUnsignedShort (error, data, 2);
  }

  public ErrorRecord (byte[] data, int offset)
  {
    super (data, offset);
    errorNumber = Dm3270Utility.unsignedShort (data, offset + 2);
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
    return String.format ("error     : %s - %s", Dm3270Utility.toHexString (data),
                          errorText);
  }
}