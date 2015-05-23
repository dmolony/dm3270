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
        dataRecords.add (new DataRecord (data, 3));
        dataRecords.add (new DataRecord (data, 9));
        dataRecords.add (new DataRecord (data, 19));

        if (data.length == 33)
          message = new String (data, 26, 7);
        else if (data.length == 39)
        {
          dataRecords.add (new RecordSize (data, 24));
          message = new String (data, 32, 7);
        }
        else
          System.out.printf ("Unrecognised data length: %d%n", data.length);
        screen.setTransferMessage (message);
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
        String transferMessage = screen.getTransferMessage ();
        ebcdic = "FT:DATA".equals (transferMessage);
        if (subtype == 0x04)                  // message or transfer buffer
        {
          DataHeader header = new DataHeader (data, 3);
          dataRecords.add (header);
          int buflen = header.bufferLength - 5;
          transferBuffer = new byte[buflen];
          System.arraycopy (data, 8, transferBuffer, 0, buflen);
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

          if (true)                       // have data to send
          {
            int buflen = 50;
            int length =
                3 + 3 + RecordNumber.RECORD_LENGTH + DataHeader.RECORD_LENGTH + buflen;
            // if CRLF option add 1 to length for the ctrl-z
            buffer = new byte[length];

            buffer[ptr++] = (byte) 0x88;
            ptr = Utility.packUnsignedShort (buffer.length - 1, buffer, ptr);

            buffer[ptr++] = (byte) 0xD0;
            buffer[ptr++] = (byte) 0x46;
            buffer[ptr++] = (byte) 0x05;

            RecordNumber recordNumber = new RecordNumber (1);
            ptr = recordNumber.pack (buffer, ptr);

            DataHeader dataHeader = new DataHeader (buflen, false);
            ptr = dataHeader.pack (buffer, ptr);

            // (if CR/LF 0x0D/0x0A terminate with ctrl-z 0x1A)
          }
          else
          {
            buffer = new byte[3 + 3 + ErrorRecord.RECORD_LENGTH];

            buffer[ptr++] = (byte) 0x88;
            ptr = Utility.packUnsignedShort (buffer.length - 1, buffer, ptr);

            buffer[ptr++] = (byte) 0xD0;
            buffer[ptr++] = (byte) 0x46;
            buffer[ptr++] = (byte) 0x08;

            ErrorRecord errorRecord = new ErrorRecord (ErrorRecord.EOF);
            ptr = errorRecord.pack (buffer, ptr);
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