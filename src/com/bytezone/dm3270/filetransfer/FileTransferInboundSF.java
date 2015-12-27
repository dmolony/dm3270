package com.bytezone.dm3270.filetransfer;

public class FileTransferInboundSF extends FileTransferSF
{
  public FileTransferInboundSF (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length, "Inbound");

    TransferRecord transferRecord;

    int ptr = 3;
    while (ptr < data.length)
    {
      switch (data[ptr])
      {
        case 0x63:
          transferRecord = new RecordNumber (data, ptr);
          break;

        case 0x69:
          transferRecord = new ErrorRecord (data, ptr);
          break;

        case (byte) 0xC0:
          transferRecord = new DataRecord (data, ptr);
          break;

        default:
          System.out.printf ("Unknown inbound TransferRecord: %02X%n", data[ptr]);
          transferRecord = new TransferRecord (data, ptr);
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
}