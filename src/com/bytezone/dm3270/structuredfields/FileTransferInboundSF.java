package com.bytezone.dm3270.structuredfields;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.display.Screen;

public class FileTransferInboundSF extends StructuredField
{
  private final byte rectype;
  private final byte subtype;

  private byte[] transfer;
  private boolean ebcdic;
  private final List<String> extraBytes = new ArrayList<> ();
  private RecordHeader recordNumber;

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
          recordNumber = new RecordHeader (data, 3);

          ebcdic = true;
          transfer = new byte[buflen];
          System.arraycopy (data, 14, transfer, 0, buflen);
        }
        else if (subtype == 0x08)       // no data
        {
          extraBytes.add (Utility.toHexString (data, 3, 4));
          if (data.length != 7)
            System.out.printf ("Unrecognised data length: %d%n", data.length);
        }
        break;

      case 0x47:                        // acknowledge DATA
        // subtype 0x05
        extraBytes.add (Utility.toHexString (data, 3, 6));
        if (data.length != 9)
          System.out.printf ("Unrecognised data length: %d%n", data.length);
        break;

      default:
        extraBytes.add (Utility.toHexString (data, 3, data.length - 3));
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
    if (recordNumber != null)
    {
      text.append (String.format ("%n   recnumhdr : %s", recordNumber.header));
      text.append (String.format ("%n   record no : %s", recordNumber.recordNumber));
      text.append (String.format ("%n   compress  : %s", recordNumber.compression));
      text.append (String.format ("%n   start flag: %s", recordNumber.startFlag));
      text.append (String.format ("%n   buflen    : %s (%,d + 5)", recordNumber.bufLen,
                                  recordNumber.bufferLength));
    }

    for (String extra : extraBytes)
      if (!extra.isEmpty ())
        text.append ("\n   extras    : " + extra);

    if (transfer != null)
    {
      text.append ("\n");
      text.append (Utility.toHex (transfer, ebcdic));
    }

    return text.toString ();
  }

  private class RecordHeader
  {
    byte[] buffer = new byte[11];
    int bufferLength;
    String header;
    String recordNumber;
    String compression;
    String startFlag;
    String bufLen;

    public RecordHeader (byte[] data, int offset)
    {
      System.arraycopy (data, offset, buffer, 0, buffer.length);
      bufferLength = Utility.unsignedShort (buffer, 9) - 5;
      header = Utility.toHexString (buffer, 0, 2);
      recordNumber = Utility.toHexString (buffer, 2, 4);
      compression = Utility.toHexString (buffer, 6, 2);
      startFlag = Utility.toHexString (buffer, 8, 1);
      bufLen = Utility.toHexString (buffer, 9, 2);

      if (buffer[6] == (byte) 0xC0 && buffer[7] == (byte) 0x80)
        compression += " (not compressed)";
    }
  }
}