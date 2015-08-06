package com.bytezone.dm3270.filetransfer;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.filetransfer.Transfer.TransferContents;
import com.bytezone.dm3270.filetransfer.Transfer.TransferType;
import com.bytezone.dm3270.structuredfields.StructuredField;

public class FileTransferSF extends StructuredField
{
  protected final String direction;
  protected final byte rectype;
  protected final byte subtype;
  protected final List<TransferRecord> transferRecords = new ArrayList<> ();

  protected TransferContents transferContents;// outbound only
  protected TransferType transferType;
  protected Transfer transfer;

  protected final boolean debug = false;

  public FileTransferSF (byte[] buffer, int offset, int length, Screen screen,
      String direction)
  {
    super (buffer, offset, length, screen);

    assert data[0] == (byte) 0xD0;

    rectype = data[1];
    subtype = data[2];
    this.direction = direction;
  }

  protected void setTransferContents (ContentsRecord contentsRecord)
  {
    transferContents = contentsRecord.transferContents;
  }

  protected void setTransferType (TransferType transferType)
  {
    this.transferType = transferType;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ("Struct Field : D0 File Transfer ");
    text.append (direction + "\n");
    text.append (String.format ("   type      : %02X%n", rectype));
    text.append (String.format ("   subtype   : %02X", subtype));

    for (TransferRecord transferRecord : transferRecords)
      text.append (String.format ("%n   %s", transferRecord));

    return text.toString ();
  }
}