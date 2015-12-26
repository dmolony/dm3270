package com.bytezone.dm3270.filetransfer;

import java.security.InvalidParameterException;

import com.bytezone.dm3270.filetransfer.Transfer.TransferContents;
import com.bytezone.dm3270.utilities.Dm3270Utility;

public class ContentsRecord extends TransferRecord
{
  final String contents;
  final TransferContents transferContents;

  public ContentsRecord (byte[] data, int offset)
  {
    super (data, offset);

    contents = new String (data, offset + 2, 7);
    if ("FT:MSG ".equals (contents))
      transferContents = TransferContents.MSG;
    else if ("FT:DATA".equals (contents))
      transferContents = TransferContents.DATA;
    else
      throw new InvalidParameterException ();
  }

  @Override
  public String toString ()
  {
    return String.format ("contents  : %s (%s)", Dm3270Utility.toHexString (data),
                          contents);
  }
}