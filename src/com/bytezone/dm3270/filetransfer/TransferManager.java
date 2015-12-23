package com.bytezone.dm3270.filetransfer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
  private final AssistantStage assistantStage;
  private IndFileCommand indFileCommand;            // spotted while observing
  private IndFileCommand intendedIndFileCommand;    // set by the instigator

  public TransferManager (Screen screen, Site site, AssistantStage assistantStage)
  {
    this.screen = screen;
    this.site = site;
    this.assistantStage = assistantStage;
    assistantStage.setTransferManager (this);
  }

  public void setIndFileCommand (IndFileCommand indFileCommand)
  {
    intendedIndFileCommand = indFileCommand;    // should contain a byte[]
  }

  // called from FileTransferOutboundSF.processOpen()
  Optional<byte[]> getCurrentFileBuffer ()
  {
    if (intendedIndFileCommand == null || intendedIndFileCommand.getBuffer () == null)
      return Optional.empty ();
    return Optional.of (intendedIndFileCommand.getBuffer ());
  }

  // called from FileTransferOutboundSF.processOpen()
  void openTransfer (Transfer transfer)
  {
    currentTransfer = transfer;     // save it for subsequent calls
    transfer.setTransferCommand (intendedIndFileCommand);
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
    currentTransfer = null;

    return Optional.of (transfer);
  }

  // called from FileTransferOutboundSF.processDownload()
  void closeTransfer ()
  {
    currentTransfer = null;
  }

  public IndFileCommand getIndFileCommand ()
  {
    return indFileCommand;
  }

  @Override
  public void tsoCommand (String command)
  {
    if (command.startsWith ("IND$FILE") || command.startsWith ("TSO IND$FILE"))
      indFileCommand = new IndFileCommand (command);
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

    // this should be sent to a listener
    ReporterNode reporterNode = assistantStage.getReporterNode ();
    if (siteFolderName.isEmpty ())
      reporterNode.addBuffer (name, buffer);
    else
      reporterNode.addBuffer (name, buffer, siteFolderName);
  }
}