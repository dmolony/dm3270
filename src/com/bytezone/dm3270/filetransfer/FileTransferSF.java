package com.bytezone.dm3270.filetransfer;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.filetransfer.Transfer.TransferType;
import com.bytezone.dm3270.structuredfields.StructuredField;

public class FileTransferSF extends StructuredField
{
  protected final String direction;
  protected final byte rectype;
  protected final byte subtype;
  protected final List<DataRecord> dataRecords = new ArrayList<> ();

  protected DataHeader dataHeader;
  protected boolean ebcdic;
  //  protected String transferType;        // outbound only
  protected TransferType transferType;      // outbound only

  protected final boolean debug = false;

  //  enum TransferType
  //  {
  //    DATA, MESSAGE
  //  }

  public FileTransferSF (byte[] buffer, int offset, int length, Screen screen,
      String direction)
  {
    super (buffer, offset, length, screen);

    assert data[0] == (byte) 0xD0;

    rectype = data[1];
    subtype = data[2];
    this.direction = direction;
  }

  protected boolean checkEbcdic (byte[] data)
  {
    return checkEbcdic (data, 0, data.length);
  }

  protected void setTransferType (String text)
  {
    if ("FT:DATA".equals (text))
      transferType = TransferType.DATA;
    else if ("FT:MSG ".equals (text))
      transferType = TransferType.MSG;
    else
      throw new InvalidParameterException ();
  }

  protected boolean checkEbcdic (byte[] data, int offset, int length)
  {
    int ascii = 0;
    int ebcdic = 0;

    while (length > 0)
    {
      if (data[offset] == 0x20)
        ascii++;
      else if (data[offset] == 0x40)
        ebcdic++;
      length--;
      offset++;
    }
    return ebcdic > ascii;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ("Struct Field : D0 File Transfer ");
    text.append (direction + "\n");
    text.append (String.format ("   type      : %02X%n", rectype));
    text.append (String.format ("   subtype   : %02X", subtype));

    for (DataRecord dataRecord : dataRecords)
      text.append (String.format ("%n   %s", dataRecord));

    if (transferType != null)
      text.append (String.format ("%n   transfer  : %s", transferType));

    if (dataHeader != null)
    {
      text.append (String.format ("%n   %s", dataHeader));
      text.append ("\n\n");
      text.append (Utility.toHex (dataHeader.getBuffer (), ebcdic));
    }

    return text.toString ();
  }
}