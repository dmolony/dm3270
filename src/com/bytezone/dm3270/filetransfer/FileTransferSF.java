package com.bytezone.dm3270.filetransfer;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.structuredfields.StructuredField;

public class FileTransferSF extends StructuredField
{
  protected final byte rectype;
  protected final byte subtype;
  protected final List<DataRecord> extraBytes = new ArrayList<> ();
  protected byte[] transferBuffer;
  protected boolean ebcdic;
  protected String message;
  protected final String direction;

  public FileTransferSF (byte[] buffer, int offset, int length, Screen screen,
      String direction)
  {
    super (buffer, offset, length, screen);

    assert data[0] == (byte) 0xD0;

    rectype = data[1];
    subtype = data[2];
    this.direction = direction;
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

    for (DataRecord extra : extraBytes)
      text.append (String.format ("\n   %s", extra));

    if (message != null)
      text.append (String.format ("\n   message   : %s", message));

    if (transferBuffer != null)
    {
      text.append ("\n");
      text.append (Utility.toHex (transferBuffer, ebcdic));
    }

    return text.toString ();
  }
}