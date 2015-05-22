package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.commands.ReadStructuredFieldCommand;
import com.bytezone.dm3270.display.Screen;

public class FileTransferOutbound extends FileTransferSF
{
  public FileTransferOutbound (byte[] buffer, int offset, int length, Screen screen)
  {
    super (buffer, offset, length, screen, "Outbound");

    switch (rectype)
    {
      case 0x00:
        if (data.length == 33)
        {
          message = new String (data, 26, 7);
          dataRecords.add (new DataRecord (data, 3));
          dataRecords.add (new DataRecord (data, 9));
          dataRecords.add (new DataRecord (data, 19));
        }
        else if (data.length == 39)
        {
          message = new String (data, 32, 7);
          dataRecords.add (new DataRecord (data, 3));
          dataRecords.add (new DataRecord (data, 9));
          dataRecords.add (new DataRecord (data, 19));
          dataRecords.add (new DataRecord (data, 24));
        }
        else
          System.out.printf ("Unrecognised data length: %d%n", data.length);
        break;

      case 0x41:
        if (data.length != 3)
          System.out.printf ("Unrecognised data length: %d%n", data.length);
        break;

      case 0x45:
        dataRecords.add (new DataRecord (data, 3));
        dataRecords.add (new DataRecord (data, 8));
        if (data.length != 13)
          System.out.printf ("Unrecognised data length: %d%n", data.length);
        break;

      case 0x46:
        dataRecords.add (new DataRecord (data, 3));
        if (data.length != 7)
          System.out.printf ("Unrecognised data length: %d%n", data.length);
        break;

      case 0x47:
        if (subtype == 0x04)                  // message or transfer buffer
        {
          DataHeader header = new DataHeader (data, 3);
          dataRecords.add (header);
          int buflen = header.bufferLength - 5;
          if (data[6] == 0)                   // temp
            message = new String (data, 8, buflen).trim ();
          else
          {
            ebcdic = checkEbcdic (data, 8, buflen);
            transferBuffer = new byte[buflen];
            System.arraycopy (data, 8, transferBuffer, 0, buflen);
          }
        }
        else if (subtype == 0x11)             // transfer buffer
          dataRecords.add (new DataRecord (data, 3));
        else
        {
          if (data.length > 3)
            dataRecords.add (new DataRecord (data, 3));
          System.out.println ("Unknown subtype");
        }
        break;

      default:
        dataRecords.add (new DataRecord (data, 3));
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
  public void process ()
  {
    switch (rectype)
    {
      case 0x00:                      // OPEN request
        if (subtype == 0x12)
        {
          // RSF 0xD0 0x00 0x09
          byte[] buffer = new byte[6];
          int ptr = 0;

          buffer[ptr++] = (byte) 0x88;
          ptr = Utility.packUnsignedShort (buffer.length - 1, buffer, ptr);

          buffer[ptr++] = (byte) 0xD0;
          buffer[ptr++] = (byte) 0x00;
          buffer[ptr++] = (byte) 0x09;

          reply = new ReadStructuredFieldCommand (buffer, screen);
        }
        break;

      case 0x41:                          // CLOSE request
        if (subtype == 0x12)
        {
          // RSF 0xD0 0x41 0x09         CLOSE acknowledgement
          byte[] buffer = new byte[6];
          int ptr = 0;

          buffer[ptr++] = (byte) 0x88;
          ptr = Utility.packUnsignedShort (buffer.length - 1, buffer, ptr);

          buffer[ptr++] = (byte) 0xD0;
          buffer[ptr++] = (byte) 0x41;
          buffer[ptr++] = (byte) 0x09;

          reply = new ReadStructuredFieldCommand (buffer, screen);
        }
        break;

      case 0x45:                          // SET CURSOR request
        if (subtype == 0x11)
        {

        }
        break;

      case 0x46:                          // GET request
        if (subtype == 0x11)
        {
          byte[] buffer;
          int ptr = 0;

          if (false)                      // have data to send
          {
            int buflen = 2000;
            buffer = new byte[buflen + 5 + 9 + 3];

            buffer[ptr++] = (byte) 0x88;
            ptr = Utility.packUnsignedShort (buffer.length - 1, buffer, ptr);

            buffer[ptr++] = (byte) 0xD0;
            buffer[ptr++] = (byte) 0x46;
            buffer[ptr++] = (byte) 0x05;

            buffer[ptr++] = (byte) 0x63;      // 6-byte recnum record
            buffer[ptr++] = (byte) 0x06;
            ptr = Utility.packUnsignedLong (1, buffer, ptr);

            buffer[ptr++] = (byte) 0xC0;
            buffer[ptr++] = (byte) 0x80;
            buffer[ptr++] = (byte) 0x61;
            ptr = Utility.packUnsignedShort (buflen + 5, buffer, ptr);
            // (if CR/LF 0x0D/0x0A terminate with ctrl-z 0x1A)
          }
          else
          {
            buffer = new byte[10];

            buffer[ptr++] = (byte) 0x88;
            ptr = Utility.packUnsignedShort (buffer.length - 1, buffer, ptr);

            buffer[ptr++] = (byte) 0xD0;
            buffer[ptr++] = (byte) 0x46;
            buffer[ptr++] = (byte) 0x08;

            buffer[ptr++] = (byte) 0x69;      // 4-byte xxx record
            buffer[ptr++] = (byte) 0x04;
            buffer[ptr++] = (byte) 0x22;
            buffer[ptr++] = (byte) 0x00;
          }

          reply = new ReadStructuredFieldCommand (buffer, screen);
        }
        break;

      case 0x47:                          // data transfer buffer
        if (subtype == 0x04)
        {
          byte[] buffer = new byte[12];
          int ptr = 0;

          buffer[ptr++] = (byte) 0x88;
          ptr = Utility.packUnsignedShort (buffer.length - 1, buffer, ptr);

          buffer[ptr++] = (byte) 0xD0;
          buffer[ptr++] = (byte) 0x47;
          buffer[ptr++] = (byte) 0x05;
          buffer[ptr++] = (byte) 0x63;        // 6-byte recnum record
          buffer[ptr++] = (byte) 0x06;        // length of this record
          ptr = Utility.packUnsignedLong (1, buffer, ptr);

          reply = new ReadStructuredFieldCommand (buffer, screen);
        }
        else if (subtype == 0x11)     // INSERT request
        {
          // do nothing
        }
        break;
    }
  }
}