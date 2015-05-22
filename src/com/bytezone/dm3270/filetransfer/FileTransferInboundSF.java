package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.display.Screen;

public class FileTransferInboundSF extends FileTransferSF
{
  public FileTransferInboundSF (byte[] buffer, int offset, int length, Screen screen)
  {
    super (buffer, offset, length, screen, "Inbound");

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
          extraBytes.add (new DataHeader (data, 9));

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
}