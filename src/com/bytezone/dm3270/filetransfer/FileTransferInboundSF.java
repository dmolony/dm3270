package com.bytezone.dm3270.filetransfer;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.structuredfields.StructuredField;

public class FileTransferInboundSF extends StructuredField
{
  private final byte rectype;
  private final byte subtype;

  private byte[] transferBuffer;
  private boolean ebcdic;
  private DataHeader header;
  private final List<DataRecord> extraBytes = new ArrayList<> ();

  public FileTransferInboundSF (byte[] buffer, int offset, int length, Screen screen)
  {
    super (buffer, offset, length, screen);

    assert data[0] == (byte) 0xD0;

    rectype = data[1];
    subtype = data[2];

    switch (rectype)
    {
      case 0x00:                      // acknowledge OPEN
        // subtype 0x09
        if (data.length != 3)
          System.out.printf ("Unrecognised data length: %d%n", data.length);
        break;

      case 0x41:                      // acknowledge CLOSE
        // subtype 0x09
        if (data.length != 3)
          System.out.printf ("Unrecognised data length: %d%n", data.length);
        break;

      case 0x46:
        if (subtype == 0x05)            // transfer buffer
        {
          int buflen = Utility.unsignedShort (data, 12) - 5;
          extraBytes.add (new RecordNumber (data, 3));
          header = new DataHeader (data, 9);

          ebcdic = true;
          transferBuffer = new byte[buflen];
          System.arraycopy (data, 14, transferBuffer, 0, buflen);
        }
        else if (subtype == 0x08)       // no data
        {
          extraBytes.add (new DataRecord (data, 3));
          if (data.length != 7)
            System.out.printf ("Unrecognised data length: %d%n", data.length);
        }
        break;

      case 0x47:                        // acknowledge DATA
        // subtype 0x05
        extraBytes.add (new RecordNumber (data, 3));
        if (data.length != 9)
          System.out.printf ("Unrecognised data length: %d%n", data.length);
        break;

      default:
        if (data.length > 3)
          extraBytes.add (new DataRecord (data, 3));
        System.out.printf ("Unknown type: %02X%n", rectype);
    }

    if (true)
    {
      System.out.println (this);
      System.out.println ("-----------------------------------------"
          + "------------------------------");
    }
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ("Struct Field : D0 File Transfer Inbound\n");
    text.append (String.format ("   type      : %02X%n", rectype));
    text.append (String.format ("   subtype   : %02X", subtype));

    for (DataRecord extra : extraBytes)
      text.append (String.format ("\n   %s", extra));

    if (header != null)
      text.append (String.format ("\n   %s", header));

    if (transferBuffer != null)
    {
      text.append ("\n");
      text.append (Utility.toHex (transferBuffer, ebcdic));
    }

    return text.toString ();
  }
}