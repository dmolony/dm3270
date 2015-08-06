package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.display.Screen;

public class FileTransferInboundSF extends FileTransferSF
{
  public FileTransferInboundSF (byte[] buffer, int offset, int length, Screen screen)
  {
    super (buffer, offset, length, screen, "Inbound");

    switch (rectype)
    {
      case 0x00:// acknowledge OPEN
        // subtype 0x09
        if (data.length != 3)
          System.out.printf ("Unrecognised data length: %d%n", data.length);
        break;

      case 0x41:// acknowledge CLOSE
        // subtype 0x09
        if (data.length != 3)
          System.out.printf ("Unrecognised data length: %d%n", data.length);
        break;

      case 0x46:
        if (subtype == 0x05) // transfer buffer to host
        {
          transferRecords.add (new RecordNumber (data, 3));
          dataHeader = new DataHeader (data, 9);
          ebcdic = dataHeader.checkEbcdic ();
        }
        else if (subtype == 0x08) // no data
        {
          transferRecords.add (new ErrorRecord (data, 3));
          if (data.length != 7)
            System.out.printf ("Unrecognised data length: %d%n", data.length);
        }
        else if (data.length != 3)
          System.out.printf ("Unrecognised data length: %d%n", data.length);
        break;

      case 0x47:// acknowledge DATA received
        // subtype 0x05
        transferRecords.add (new RecordNumber (data, 3));

        if (data.length != 9)
          System.out.printf ("Unrecognised data length: %d%n", data.length);
        break;

      default:
        if (data.length > 3)
          transferRecords.add (new TransferRecord (data, 3));
        System.out.printf ("Unknown type: %02X%n", rectype);
    }

    if (debug)
    {
      System.out.println (this);
      System.out.println ("-----------------------------------------"
          + "------------------------------");
    }
  }
}