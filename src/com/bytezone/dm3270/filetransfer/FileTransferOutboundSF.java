package com.bytezone.dm3270.filetransfer;

import java.util.Optional;

import com.bytezone.dm3270.commands.ReadStructuredFieldCommand;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.filetransfer.Transfer.TransferType;
import com.bytezone.dm3270.utilities.Dm3270Utility;

public class FileTransferOutboundSF extends FileTransferSF
{
  private TransferManager transferManager;

  public FileTransferOutboundSF (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length, "Outbound");

    if (rectype == 0 && subtype == 0x12)
      transferType = TransferType.DOWNLOAD;

    TransferRecord transferRecord;

    int ptr = 3;
    while (ptr < data.length)
    {
      switch (data[ptr])
      {
        case 0x01:
        case 0x09:        // upload only
        case 0x0A:
        case 0x50:
          transferRecord = new TransferRecord (data, ptr);
          break;

        case 0x03:                                          // MSG or DATA
          transferRecord = new ContentsRecord (data, ptr);
          transferContents = ((ContentsRecord) transferRecord).transferContents;
          break;

        case 0x08:
          transferRecord = new RecordSize (data, ptr);
          transferType = TransferType.UPLOAD;
          break;

        case (byte) 0xC0:
          transferRecord = new DataRecord (data, ptr);
          break;

        default:
          System.out.printf ("Unknown outbound TransferRecord: %02X%n", data[ptr]);
          transferRecord = new TransferRecord (data, ptr);
          break;
      }

      transferRecords.add (transferRecord);
      ptr += transferRecord.length ();
    }

    if (debug)
    {
      System.out.println (this);
      System.out.println ("-----------------------------------------"
          + "------------------------------");
    }
  }

  @Override
  public void process (Screen screen)
  {
    transferManager = screen.getTransferManager ();

    switch (rectype)
    {
      case 0x00:                        // OPEN request
        if (subtype == 0x12)
          processOpen ();
        break;

      case 0x41:                        // CLOSE request
        if (subtype == 0x12)
          processClose ();
        break;

      case 0x45:                        // something to do with UPLOAD
        break;

      case 0x46:                        // UPLOAD data transfer buffer
        if (subtype == 0x11)
          process (TransferType.UPLOAD);
        break;

      case 0x47:                        // DOWNLOAD data transfer buffer
        if (subtype == 0x04)
          process (TransferType.DOWNLOAD);
        break;

      default:
        System.out.printf ("Unknown record type: %02X%n", rectype);
    }
  }

  private void processOpen ()
  {
    Optional<Transfer> optionalTransfer = transferManager.openTransfer (this);
    if (optionalTransfer.isPresent ())
    {
      byte[] buffer = getReplyBuffer (6, (byte) 0x00, (byte) 0x09);
      setReply (new ReadStructuredFieldCommand (buffer));
    }
  }

  private void processClose ()
  {
    Optional<Transfer> optionalTransfer = transferManager.closeTransfer (this);
    if (optionalTransfer.isPresent ())
    {
      byte[] buffer = getReplyBuffer (6, (byte) 0x41, (byte) 0x09);
      setReply (new ReadStructuredFieldCommand (buffer));
    }
  }

  private void process (TransferType type)
  {
    Optional<Transfer> optionalTransfer = transferManager.getTransfer ();
    if (!optionalTransfer.isPresent ())
    {
      System.out.println ("No active transfer");
      return;
    }

    Transfer transfer = optionalTransfer.get ();
    byte[] replyBuffer = type == TransferType.UPLOAD ? processUpload (transfer)
        : processDownload (transfer);

    setReply (new ReadStructuredFieldCommand (replyBuffer));
    transferManager.process (this);

    // message transfers don't close
    if (transfer.isMessage ())
      transferManager.closeTransfer ();
  }

  private byte[] processUpload (Transfer transfer)
  {
    byte[] replyBuffer;
    int ptr = 6;

    if (transfer.hasMoreData () && !transfer.cancelled ())    // have data to send
    {
      DataRecord dataHeader = transfer.getDataHeader ();
      int replyBufferLength = ptr + RecordNumber.RECORD_LENGTH + DataRecord.HEADER_LENGTH
          + dataHeader.getBufferLength ();
      replyBuffer = getReplyBuffer (replyBufferLength, (byte) 0x46, (byte) 0x05);

      RecordNumber recordNumber = new RecordNumber (transfer.size ());
      ptr = recordNumber.pack (replyBuffer, ptr);
      ptr = dataHeader.pack (replyBuffer, ptr);
    }
    else      // finished sending buffers, now send an EOF
    {
      int replyBufferLength = ptr + ErrorRecord.RECORD_LENGTH;
      replyBuffer = getReplyBuffer (replyBufferLength, (byte) 0x46, (byte) 0x08);

      ErrorRecord errorRecord = new ErrorRecord (ErrorRecord.EOF);
      ptr = errorRecord.pack (replyBuffer, ptr);
    }

    assert ptr == replyBuffer.length;
    return replyBuffer;
  }

  private byte[] processDownload (Transfer transfer)
  {
    byte[] replyBuffer;
    int ptr = 6;

    if (transfer.cancelled ())
    {
      int replyBufferLength = ptr + ErrorRecord.RECORD_LENGTH;
      replyBuffer = getReplyBuffer (replyBufferLength, (byte) 0x47, (byte) 0x08);

      ErrorRecord errorRecord = new ErrorRecord (ErrorRecord.CANCEL);
      ptr = errorRecord.pack (replyBuffer, ptr);
    }
    else
    {
      int replyBufferLength = ptr + RecordNumber.RECORD_LENGTH;
      replyBuffer = getReplyBuffer (replyBufferLength, (byte) 0x47, (byte) 0x05);

      DataRecord dataRecord =
          (DataRecord) transferRecords.get (transferRecords.size () - 1);
      int bufferNumber = transfer.add (dataRecord);
      RecordNumber recordNumber = new RecordNumber (bufferNumber);
      ptr = recordNumber.pack (replyBuffer, ptr);
    }

    assert ptr == replyBuffer.length;
    return replyBuffer;
  }

  private byte[] getReplyBuffer (int length, byte command, byte subcommand)
  {
    byte[] buffer = new byte[length];
    int ptr = 0;

    buffer[ptr++] = (byte) 0x88;
    ptr = Dm3270Utility.packUnsignedShort (buffer.length - 1, buffer, ptr);

    buffer[ptr++] = (byte) 0xD0;
    buffer[ptr++] = command;
    buffer[ptr++] = subcommand;

    return buffer;
  }
}