package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.commands.ReadStructuredFieldCommand;
import com.bytezone.dm3270.display.Screen;

public class FileTransferOutboundSF extends FileTransferSF
{
  private int bufferNumber;
  private Transfer transfer;
  private FileStage fileStage;

  public FileTransferOutboundSF (byte[] buffer, int offset, int length, Screen screen)
  {
    super (buffer, offset, length, screen, "Outbound");
    fileStage = screen.getFileStage ();

    switch (rectype)
    {
    // OPEN for SEND or RECEIVE

      case 0x00:
        dataRecords.add (new DataRecord (data, 3));
        dataRecords.add (new DataRecord (data, 9));
        dataRecords.add (new DataRecord (data, 19));

        if (data.length == 33)          // OPEN for SEND
          setTransferType (new String (data, 26, 7));            // FT:MSG. or FT:DATA
        else if (data.length == 39)     // OPEN for RECEIVE
        {
          dataRecords.add (new RecordSize (data, 24));
          setTransferType (new String (data, 32, 7));            // FT:DATA
        }
        else
          System.out.printf ("Unrecognised data length: %d%n", data.length);

        transfer = fileStage.openTransfer (this);

        break;

      // CLOSE

      case 0x41:
        transfer = fileStage.closeTransfer (this);
        if (data.length != 3)
          System.out.printf ("Unrecognised data length: %d%n", data.length);
        break;

      // Receiving data

      case 0x47:
        transfer = fileStage.getTransfer ();
        if (transfer == null)
        {
          System.out.println ("null 47");
          return;
        }

        ebcdic = transfer.isData ();

        if (subtype == 0x11)                              // always before 0x04
        {
          dataRecords.add (new DataRecord (data, 3));
          transfer.add (this);
        }
        else if (subtype == 0x04)                  // message or transfer buffer
        {
          dataHeader = new DataHeader (data, 3);
          transfer.add (dataHeader);
          bufferNumber = transfer.size ();
          ebcdic = checkEbcdic (dataHeader.getBuffer ());
          transfer.add (this);

          if (transfer.isMessage ())              // message transfers don't close
            fileStage.closeTransfer ();
        }
        else
        {
          if (data.length > 3)
            dataRecords.add (new DataRecord (data, 3));
          System.out.println ("Unknown subtype");
        }
        break;

      // Sending data

      case 0x45:
        transfer = fileStage.getTransfer ();
        transfer.add (this);

        dataRecords.add (new DataRecord (data, 3));
        dataRecords.add (new DataRecord (data, 8));
        if (data.length != 13)
          System.out.printf ("Unrecognised data length: %d%n", data.length);
        break;

      case 0x46:
        transfer = fileStage.getTransfer ();
        transfer.add (this);

        dataRecords.add (new DataRecord (data, 3));
        if (data.length != 7)
          System.out.printf ("Unrecognised data length: %d%n", data.length);
        break;

      default:
        dataRecords.add (new DataRecord (data, 3));
        System.out.printf ("Unknown type: %02X%n", rectype);
    }

    if (debug)
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
      case 0x00:                          // OPEN request
        if (subtype == 0x12)
          processOpen ();
        break;

      case 0x41:                          // CLOSE request
        if (subtype == 0x12)
          processClose ();
        break;

      case 0x46:                          // send data transfer buffer
        if (subtype == 0x11)
          processSend ();
        break;

      case 0x47:                          // receive data transfer buffer
        if (subtype == 0x04)
          processReceive ();
        break;
    }
  }

  private void processOpen ()
  {
    byte[] buffer = getReplyBuffer (6, (byte) 0x00, (byte) 0x09);
    reply = new ReadStructuredFieldCommand (buffer, screen);
  }

  private void processClose ()
  {
    byte[] buffer = getReplyBuffer (6, (byte) 0x41, (byte) 0x09);
    reply = new ReadStructuredFieldCommand (buffer, screen);
  }

  private void processSend ()
  {
    byte[] buffer;
    int ptr = 0;

    if (true)                       // have data to send
    {
      int buflen = 50;
      int length = 3 + 3 + RecordNumber.RECORD_LENGTH + DataHeader.HEADER_LENGTH + buflen;
      // if CRLF option add 1 to length for the ctrl-z

      buffer = getReplyBuffer (length, (byte) 0x46, (byte) 0x05);
      ptr = 6;

      RecordNumber recordNumber = new RecordNumber (1);
      ptr = recordNumber.pack (buffer, ptr);

      DataHeader dataHeader = new DataHeader (buflen, false);
      ptr = dataHeader.pack (buffer, ptr);

      // (if CR/LF 0x0D/0x0A terminate with ctrl-z 0x1A)
    }
    else
    {
      int length = 6 + ErrorRecord.RECORD_LENGTH;
      buffer = getReplyBuffer (length, (byte) 0x46, (byte) 0x08);

      ptr = 6;
      ErrorRecord errorRecord = new ErrorRecord (ErrorRecord.EOF);
      ptr = errorRecord.pack (buffer, ptr);
    }

    reply = new ReadStructuredFieldCommand (buffer, screen);
  }

  private void processReceive ()
  {
    int length = 6 + RecordNumber.RECORD_LENGTH;
    byte[] buffer = getReplyBuffer (length, (byte) 0x47, (byte) 0x05);

    int ptr = 6;
    RecordNumber recordNumber = new RecordNumber (bufferNumber);
    ptr = recordNumber.pack (buffer, ptr);

    reply = new ReadStructuredFieldCommand (buffer, screen);
  }

  private byte[] getReplyBuffer (int length, byte command, byte subcommand)
  {
    byte[] buffer = new byte[length];
    int ptr = 0;

    buffer[ptr++] = (byte) 0x88;
    ptr = Utility.packUnsignedShort (buffer.length - 1, buffer, ptr);

    buffer[ptr++] = (byte) 0xD0;
    buffer[ptr++] = command;
    buffer[ptr++] = subcommand;

    return buffer;
  }
}