package com.bytezone.dm3270.filetransfer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
  private final List<Transfer> transfers = new ArrayList<> ();
  private Transfer currentTransfer;

  private final Screen screen;
  private final Site site;
  private final AssistantStage assistantStage;      // won't be needed soon

  public enum TransferStatus
  {
    READY, PROCESSING, FINISHED
  }

  public TransferManager (Screen screen, Site site, AssistantStage assistantStage)
  {
    this.screen = screen;
    this.site = site;
    this.assistantStage = assistantStage;
    assistantStage.setTransferManager (this);
  }

  // called from TSOCommand.execute()
  public void prepareTransfer (IndFileCommand indFileCommand)
  {
    currentTransfer = new Transfer (indFileCommand);
    fireTransferStatusChanged (TransferStatus.READY, currentTransfer);
  }

  // called from FileTransferOutboundSF.processOpen()
  Optional<Transfer> openTransfer (FileTransferOutboundSF transferRecord)
  {
    if (currentTransfer == null)
    {
      System.out.println ("Null current transfer");
      return Optional.empty ();
    }

    currentTransfer.add (transferRecord);
    fireTransferStatusChanged (TransferStatus.PROCESSING, currentTransfer);

    return Optional.of (currentTransfer);
  }

  // called from FileTransferOutboundSF.processSend0x46()
  // called from FileTransferOutboundSF.processReceive()
  Optional<Transfer> getTransfer (FileTransferOutboundSF transferRecord)
  {
    if (currentTransfer == null)
    {
      System.out.println ("Null current transfer");
      return Optional.empty ();
    }

    currentTransfer.add (transferRecord);
    fireTransferStatusChanged (TransferStatus.PROCESSING, currentTransfer);
    return Optional.of (currentTransfer);
  }

  // called from FileTransferOutboundSF.processClose()
  Optional<Transfer> closeTransfer (FileTransferOutboundSF transferRecord)
  {
    if (currentTransfer == null)
    {
      System.out.println ("Null current transfer");
      return Optional.empty ();
    }

    Transfer transfer = currentTransfer;
    currentTransfer.add (transferRecord);

    // add to the file tree
    if (transfer.isDownloadData ())
    {
      transfers.add (transfer);
      Platform.runLater ( () -> saveFile (transfer));
    }

    closeTransfer ();

    return Optional.of (transfer);
  }

  // called from FileTransferOutboundSF.processDownload()
  void closeTransfer ()
  {
    fireTransferStatusChanged (TransferStatus.FINISHED, currentTransfer);
    if (currentTransfer.isMessage ())
      currentTransfer = null;
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
        currentTransfer = new Transfer (newCommand);
      else
        currentTransfer.compare (newCommand);
    }
  }

  private void saveFile (Transfer transfer)
  {
    String name = transfer.getFileName ().toUpperCase ();
    if (!transfer.hasTLQ ())
    {
      String tlq = screen.getPrefix ();
      if (!tlq.isEmpty ())
        name = tlq + "." + name;
    }

    String siteFolderName = "";
    if (site != null)
    {
      siteFolderName = site.folder.getText ();
      if (!siteFolderName.isEmpty ())
      {
        Path path = Paths.get (System.getProperty ("user.home"), "dm3270", "files",
                               siteFolderName);
        if (!Files.exists (path))
          siteFolderName = "";
      }
      else
        System.out.println ("No folder specified in site record");
    }

    byte[] buffer = transfer.combineDataBuffers ();

    // ReporterNode should be a TransferListener
    ReporterNode reporterNode = assistantStage.getReporterNode ();
    if (siteFolderName.isEmpty ())
      reporterNode.addBuffer (name, buffer);
    else
      reporterNode.addBuffer (name, buffer, siteFolderName);

    fireTransferStatusChanged (TransferStatus.FINISHED, transfer);
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