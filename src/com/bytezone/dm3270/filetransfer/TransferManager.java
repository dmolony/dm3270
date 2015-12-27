package com.bytezone.dm3270.filetransfer;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.bytezone.dm3270.application.Site;
import com.bytezone.dm3270.assistant.AssistantStage;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.TSOCommandListener;
import com.bytezone.reporter.application.ReporterNode;

import javafx.application.Platform;

public class TransferManager implements TSOCommandListener
{
  private Transfer currentTransfer;

  private final Screen screen;
  private final Site site;
  private final AssistantStage assistantStage;      // won't be needed soon

  public enum TransferStatus
  {
    READY, OPEN, PROCESSING, FINISHED
  }

  public TransferManager (Screen screen, Site site, AssistantStage assistantStage)
  {
    this.screen = screen;
    this.site = site;
    this.assistantStage = assistantStage;
    assistantStage.setTransferManager (this);
  }

  // called from ScreenPacker.addTSOCommand()
  @Override
  public void tsoCommand (String command)
  {
    if (command.startsWith ("IND$FILE") || command.startsWith ("TSO IND$FILE"))
    {
      IndFileCommand newCommand = new IndFileCommand (command);
      //      indFileCommand.compareWith (indFileCommand);

      // check for a user-initiated IND$FILE command
      // If it is a download, we can either keep it as a temporary buffer, or ask
      // the user for a filename. If it is an upload we will have to ask for the
      // source filename.
      // a program-initiated IND$FILE command will already have the filenames
      if (currentTransfer == null)
        currentTransfer = new Transfer (newCommand, site, screen.getPrefix ());
      else
        currentTransfer.compare (newCommand);
    }
  }

  // called from TSOCommand.execute()
  public void prepareTransfer (IndFileCommand indFileCommand)
  {
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

    // add to the file tree
    if (transfer.isDownloadAndIsData ())
      Platform.runLater ( () -> saveFile (transfer));

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

  // this is done on the EDT
  private void saveFile (Transfer transfer)
  {
    byte[] buffer = transfer.combineDataBuffers ();

    // ReporterNode should be a TransferListener
    ReporterNode reporterNode = assistantStage.getReporterNode ();
    if (transfer.siteFolderName.isEmpty ())
      reporterNode.addBuffer (transfer.datasetName, buffer);
    else
      reporterNode.addBuffer (transfer.datasetName, buffer, transfer.siteFolderName);
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
    if (transferListeners.contains (listener))
      transferListeners.remove (listener);
  }
}