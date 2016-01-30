package com.bytezone.dm3270.filetransfer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.utilities.FileSaver;
import com.bytezone.dm3270.utilities.Site;

// CUT - Control Unit Terminal --------- Buffered
// DFT - Distributed Function Terminal - WSF
// http://publibz.boulder.ibm.com/cgi-bin/bookmgr/BOOKS/cn7a7003/2.4.1
// http://www.hob-techtalk.com/2008/09/12/3270-a-brief-history

public class Transfer
{
  private static int INBOUND_MAX_BUFFER_SIZE = 2048;

  private TransferContents transferContents;          // MSG or DATA
  private TransferType transferType;                  // UPLOAD or DOWNLOAD
  private final IndFileCommand indFileCommand;        // user's TSO command

  private final List<DataRecord> dataRecords = new ArrayList<> ();  // downloading data
  private int dataLength;

  private DataRecord message;

  private final byte[] inboundBuffer;       // uploading data
  private int inboundBufferPtr;

  private final String datasetName;
  private final File localFile;
  private final String siteFolderName;

  public enum TransferContents
  {
    MSG, DATA
  }

  public enum TransferType
  {
    DOWNLOAD,       // mainframe -> terminal (send)
    UPLOAD          // terminal -> mainframe (receive)
  }

  // called from TransferManager.tsoCommand()
  // called from TransferManager.prepareTransfer()
  Transfer (IndFileCommand indFileCommand, Site site, String tlq)
  {
    this.indFileCommand = indFileCommand;
    inboundBuffer = indFileCommand.getBuffer ();

    String tempDatasetName = indFileCommand.getDatasetName ().toUpperCase ();
    if (!indFileCommand.hasHLQ () && !tlq.isEmpty ())
      datasetName = tlq + "." + tempDatasetName;
    else
      datasetName = tempDatasetName;

    siteFolderName = site == null ? "" : site.getFolder ();

    Path homePath = FileSaver.getHomePath (siteFolderName);
    String saveFolderName = FileSaver.getSaveFolderName (homePath, datasetName);
    Path filePath = Paths.get (saveFolderName, getDatasetName ());
    localFile = filePath.toFile ();
  }

  public String getSiteFolderName ()
  {
    return siteFolderName;
  }

  public String getDatasetName ()
  {
    return datasetName;
  }

  public File getFile ()
  {
    return localFile;
  }

  public String getMessage ()
  {
    if (!isMessage () || message == null)
      return "";

    return message.getText ();
  }

  public boolean isMessage ()
  {
    return transferContents == TransferContents.MSG;
  }

  public boolean isData ()
  {
    return transferContents == TransferContents.DATA;
  }

  // called from TransferManager.openTransfer()
  // called from TransferManager.process()
  // called from TransferManager.closeTransfer()
  void add (FileTransferOutboundSF outboundRecord)
  {
    if (outboundRecord.transferContents != null)
      transferContents = outboundRecord.transferContents;   // MSG or DATA

    if (transferType == null)
      transferType = outboundRecord.transferType;           // UPLOAD or DOWNLOAD
  }

  // called from FileTransferOutboundSF.processDownload()
  int add (DataRecord dataRecord)
  {
    if (isMessage ())
    {
      message = dataRecord;
      return 1;
    }

    if (dataRecords.contains (dataRecord))
      return dataRecords.indexOf (dataRecord) + 1;

    dataRecords.add (dataRecord);
    dataLength += dataRecord.getBufferLength ();

    return dataRecords.size ();
  }

  public boolean isDownloadAndIsData ()
  {
    return transferContents == TransferContents.DATA
        && transferType == TransferType.DOWNLOAD;
  }

  void write ()
  {
    byte[] buffer = combineDataBuffers ();
    try
    {
      Files.write (localFile.toPath (), buffer);
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }

  // called from TransferManager.closeTransfer()
  // called from AssistantStage.transferStatusChanged()
  public byte[] combineDataBuffers ()
  {
    int length = dataLength;
    if (indFileCommand.getAscii () && indFileCommand.getCrlf ())
      --length;       // assumes the file has 0x1A on the end
    byte[] fullBuffer = new byte[length];

    int ptr = 0;
    for (DataRecord dataRecord : dataRecords)
    {
      ptr = dataRecord.packBuffer (fullBuffer, ptr);

      // check for non-ascii characters
      if (indFileCommand.getAscii ())
        dataRecord.checkAscii (indFileCommand.getCrlf ());
    }

    if (fullBuffer.length < dataLength)
    {
      assert fullBuffer[fullBuffer.length - 2] == 0x0D;
      assert fullBuffer[fullBuffer.length - 1] == 0x0A;
      assert fullBuffer.length == dataLength - 1;
    }

    return fullBuffer;
  }

  DataRecord getDataHeader ()
  {
    assert hasMoreData ();

    int buflen = Math.min (INBOUND_MAX_BUFFER_SIZE, getBytesLeft ());
    DataRecord dataHeader =
        new DataRecord (inboundBuffer, inboundBufferPtr, buflen, false);
    inboundBufferPtr += buflen;
    add (dataHeader);

    return dataHeader;
  }

  public int size ()
  {
    return dataRecords.size ();
  }

  public int getDataLength ()
  {
    return dataLength;      // used to display buffer length on the console
  }

  public TransferContents getTransferContents ()
  {
    return transferContents;
  }

  public TransferType getTransferType ()
  {
    return transferType;
  }

  boolean cancelled ()
  {
    return false;
  }

  boolean hasMoreData ()
  {
    return getBytesLeft () > 0;
  }

  int getBytesLeft ()
  {
    return inboundBuffer == null ? 0 : inboundBuffer.length - inboundBufferPtr;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Contents ....... %s%n", transferContents));
    text.append (String.format ("Type ........... %s%n", transferType));
    text.append (String.format ("Command ........ %s%n", indFileCommand.getCommand ()));
    text.append (String.format ("Dataset name ... %s%n", datasetName));
    text.append (String.format ("Local file ..... %s%n", localFile));

    if (isMessage ())
      text.append (String.format ("Message ........ %s%n", getMessage ()));
    else
    {
      int bufno = 0;
      for (DataRecord dataRecord : dataRecords)
        text.append (String.format ("  Buffer %3d ...  %,8d%n", bufno++,
                                    dataRecord.getBufferLength ()));

      text.append (String.format ("Data length .... %,9d%n", dataLength));
    }

    if (inboundBuffer != null)
    {
      text.append (String.format ("inbuf length ... %d%n", inboundBuffer.length));
      text.append (String.format ("in ptr ......... %d%n", inboundBufferPtr));
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}