package com.bytezone.dm3270.filetransfer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.TSOCommandListener;
import com.bytezone.dm3270.utilities.Site;

public class TransferManager implements TSOCommandListener
{
  private static Pattern INDFILE_PATTERN =
      Pattern.compile ("^(TSO )?\\s*IND[$£]FILE\\s+(GET|PUT).*");
  private Transfer currentTransfer;

  private final Screen screen;
  private Site site;

  public enum TransferStatus
  {
    READY, OPEN, PROCESSING, FINISHED
  }

  public static boolean isIndfileCommand (String command)
  {
    return INDFILE_PATTERN.matcher (command).matches ();
  }

  public TransferManager (Screen screen, Site site)
  {
    this.screen = screen;
    this.site = site;
  }

  // called from ScreenPacker.addTSOCommand()
  @Override
  public void tsoCommand (String command)
  {
    // check for a user-initiated IND$FILE command
    if (currentTransfer == null && isIndfileCommand (command))

      // If it is a download, we can either keep it as a temporary buffer, or ask
      // the user for a filename. If it is an upload we will have to ask for the
      // source filename.

      try
      {
        IndFileCommand newCommand = new IndFileCommand (command);
        currentTransfer = new Transfer (newCommand, site, screen.getPrefix ());
      }
      catch (IllegalArgumentException e)
      {
        System.out.println (e);
      }
  }

  public void setReplayServer (Site serverSite)
  {
    site = serverSite;
  }

  // called from TSOCommand.execute()
  // called from TransferMenu.transfer()
  public void prepareTransfer (IndFileCommand indFileCommand)
  {
    if (indFileCommand.isUpload ())
    {
      File localFile = indFileCommand.getLocalFile ();
      byte[] buffer = indFileCommand.getBuffer ();

      if (buffer == null)
      {
        if (localFile == null || !localFile.exists () || !localFile.isFile ())
        {
          System.out.println ("******** No file to read ********");
          return;
        }
        try
        {
          buffer = Files.readAllBytes (localFile.toPath ());
          indFileCommand.setBuffer (buffer);
        }
        catch (IOException e)
        {
          e.printStackTrace ();
        }
      }
    }

    currentTransfer = new Transfer (indFileCommand, site, screen.getPrefix ());
    fireTransferStatusChanged (TransferStatus.READY, currentTransfer);
  }

  // called from FileTransferOutboundSF.processOpen()
  Optional<Transfer> openTransfer (FileTransferOutboundSF transferRecord)
  {
    if (currentTransfer == null)
      return Optional.empty ();

    currentTransfer.add (transferRecord);
    fireTransferStatusChanged (TransferStatus.OPEN, currentTransfer);

    return Optional.of (currentTransfer);
  }

  // called from FileTransferOutboundSF.processUpload()
  // called from FileTransferOutboundSF.processDownload()
  Optional<Transfer> getTransfer ()
  {
    return currentTransfer == null ? Optional.empty () : Optional.of (currentTransfer);
  }

  // called from FileTransferOutboundSF.processUpload()
  // called from FileTransferOutboundSF.processDownload()
  void process (FileTransferOutboundSF transferRecord)
  {
    currentTransfer.add (transferRecord);
    if (currentTransfer.isData ())
      fireTransferStatusChanged (TransferStatus.PROCESSING, currentTransfer);
  }

  // called from FileTransferOutboundSF.processClose()
  Optional<Transfer> closeTransfer (FileTransferOutboundSF transferRecord)
  {
    if (currentTransfer == null)
      return Optional.empty ();

    assert currentTransfer.isData ();

    Transfer transfer = currentTransfer;
    currentTransfer.add (transferRecord);
    if (currentTransfer.isDownloadAndIsData ())
      currentTransfer.write ();

    closeTransfer ();

    return Optional.of (transfer);
  }

  // called from FileTransferOutboundSF.processDownload() if MSG
  void closeTransfer ()
  {
    fireTransferStatusChanged (TransferStatus.FINISHED, currentTransfer);
    if (currentTransfer.isMessage ())
      currentTransfer = null;
  }

  // ---------------------------------------------------------------------------------//
  // TransferListener
  // ---------------------------------------------------------------------------------//

  private final Set<TransferListener> transferListeners = new HashSet<> ();

  void fireTransferStatusChanged (TransferStatus status, Transfer transfer)
  {
    transferListeners
        .forEach (listener -> listener.transferStatusChanged (status, transfer));
  }

  public void addTransferListener (TransferListener listener)
  {
    if (!transferListeners.contains (listener))
      transferListeners.add (listener);
  }

  public void removeTransferListener (TransferListener listener)
  {
    transferListeners.remove (listener);
  }
}