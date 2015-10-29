package com.bytezone.dm3270.filetransfer;

import com.bytezone.dm3270.assistant.AssistantStage;
import com.bytezone.dm3270.commands.ReadStructuredFieldCommand;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.filetransfer.Transfer.TransferContents;
import com.bytezone.dm3270.filetransfer.Transfer.TransferType;
import com.bytezone.dm3270.utilities.Utility;

public class FileTransferOutboundSF extends FileTransferSF
{
  private AssistantStage assistantStage;

  public FileTransferOutboundSF (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length, "Outbound");

    if (rectype == 0 && subtype == 0x12)
      transferType = TransferType.SEND;
    TransferRecord transferRecord;

    int ptr = 3;
    while (ptr < data.length)
    {
      switch (data[ptr])
      {
        case 0x01:
        case 0x09:
        case 0x0A:
        case 0x50:
          transferRecord = new TransferRecord (data, ptr);
          break;

        case 0x03:
          transferRecord = new ContentsRecord (data, ptr);
          transferContents = ((ContentsRecord) transferRecord).transferContents;
          break;

        case 0x08:
          transferRecord = new RecordSize (data, ptr);
          transferType = TransferType.RECEIVE;
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
    // check for an already processed replay command
    if (transfer != null)
      return;

    assistantStage = screen.getAssistantStage ();

    switch (rectype)
    {
      case 0x00:                        // OPEN request
        if (subtype == 0x12)
          processOpen (screen);
        break;

      case 0x41:                        // CLOSE request
        if (subtype == 0x12)
          processClose (screen);
        break;

      case 0x45:                        // something to do with SEND
        processSend0x45 ();
        break;

      case 0x46:                        // send data transfer buffer
        if (subtype == 0x11)
          processSend0x46 (screen);
        break;

      case 0x47:                        // receive data transfer buffer
        processReceive (screen);
        break;
    }
  }

  private void processOpen (Screen screen)
  {
    transfer = new Transfer ();
    transfer.add (this);
    assistantStage.openTransfer (transfer);

    byte[] buffer = getReplyBuffer (6, (byte) 0x00, (byte) 0x09);
    reply = new ReadStructuredFieldCommand (buffer);

    if (transfer.getTransferContents () == TransferContents.DATA)
    {
      // get the user command that initiated the transfer
      transfer.setTransferCommand (screen.getPreviousTSOCommand ());

      // connect the buffer that contains the data to send
      if (transfer.getTransferType () == TransferType.RECEIVE)
      {
        transfer.setTransferBuffer (assistantStage.getCurrentFileBuffer ());
        screen.setStatusText ("Sending...");
      }
      else
        screen.setStatusText ("Receiving...");
    }
  }

  private void processClose (Screen screen)
  {
    transfer = assistantStage.closeTransfer (this);

    byte[] buffer = getReplyBuffer (6, (byte) 0x41, (byte) 0x09);
    reply = new ReadStructuredFieldCommand (buffer);
    screen.setStatusText ("Closing...");
  }

  private void processSend0x45 ()
  {
    transfer = assistantStage.getTransfer (this);
  }

  private void processSend0x46 (Screen screen)
  {
    transfer = assistantStage.getTransfer (this);

    byte[] replyBuffer;
    int ptr = 0;

    if (transfer.hasMoreData () && !transfer.cancelled ())    // have data to send
    {
      DataRecord dataHeader = transfer.getDataHeader ();

      ptr = 6;
      int replyBufferLength = ptr + RecordNumber.RECORD_LENGTH + DataRecord.HEADER_LENGTH
          + dataHeader.getBufferLength ();

      // if CRLF option add 1 to length for the ctrl-z

      replyBuffer = getReplyBuffer (replyBufferLength, (byte) 0x46, (byte) 0x05);

      RecordNumber recordNumber = new RecordNumber (transfer.size ());
      ptr = recordNumber.pack (replyBuffer, ptr);
      ptr = dataHeader.pack (replyBuffer, ptr);
      screen.setStatusText (String.format ("Bytes sent: %,d%n", transfer.dataLength));

      // (if CR/LF 0x0D/0x0A terminate with ctrl-z 0x1A)
    }
    else      // finished sending buffers, now send an EOF
    {
      int length = 6 + ErrorRecord.RECORD_LENGTH;
      replyBuffer = getReplyBuffer (length, (byte) 0x46, (byte) 0x08);

      ptr = 6;
      ErrorRecord errorRecord = new ErrorRecord (ErrorRecord.EOF);
      ptr = errorRecord.pack (replyBuffer, ptr);
    }

    reply = new ReadStructuredFieldCommand (replyBuffer);
  }

  private void processReceive (Screen screen)
  {
    transfer = assistantStage.getTransfer (this);

    if (subtype == 0x04) // message or transfer buffer
    {
      int ptr = 6;
      byte[] buffer;

      if (transfer.cancelled ())
      {
        int length = ptr + ErrorRecord.RECORD_LENGTH;
        buffer = getReplyBuffer (length, (byte) 0x47, (byte) 0x08);

        ErrorRecord errorRecord = new ErrorRecord (ErrorRecord.CANCEL);
        ptr = errorRecord.pack (buffer, ptr);
      }
      else
      {
        int length = ptr + RecordNumber.RECORD_LENGTH;
        buffer = getReplyBuffer (length, (byte) 0x47, (byte) 0x05);

        DataRecord dataRecord =
            (DataRecord) transferRecords.get (transferRecords.size () - 1);
        int bufferNumber = transfer.add (dataRecord);
        RecordNumber recordNumber = new RecordNumber (bufferNumber);
        ptr = recordNumber.pack (buffer, ptr);
        if (transfer.getTransferContents () == TransferContents.DATA)
          screen.setStatusText (String.format ("Bytes received: %,d%n",
                                               transfer.dataLength));
      }
      reply = new ReadStructuredFieldCommand (buffer);

      // message transfers don't close
      if (transfer.getTransferContents () == TransferContents.MSG)
        assistantStage.closeTransfer ();
    }
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