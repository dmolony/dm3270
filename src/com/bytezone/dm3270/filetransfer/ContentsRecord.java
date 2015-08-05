package com.bytezone.dm3270.filetransfer;

import java.security.InvalidParameterException;

import com.bytezone.dm3270.filetransfer.Transfer.TransferContents;

public class ContentsRecord extends DataRecord
{
  final String contents;
  final TransferContents transferContents;

  public ContentsRecord (byte[] data, int offset)
  {
    super (data, offset);

    contents = new String (data, offset + 2, 7);
    if (contents.equals ("FT:MSG "))
      transferContents = TransferContents.MSG;
    else if (contents.equals ("FT:DATA"))
      transferContents = TransferContents.DATA;
    else
      throw new InvalidParameterException ();
  }

  @Override
  public String toString ()
  {
    return String.format ("contents  : %s", contents);
  }
}