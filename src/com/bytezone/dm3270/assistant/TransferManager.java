package com.bytezone.dm3270.assistant;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bytezone.dm3270.application.Site;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.filetransfer.FileTransferOutboundSF;
import com.bytezone.dm3270.filetransfer.Transfer;
import com.bytezone.reporter.application.FileNode;
import com.bytezone.reporter.application.ReporterNode;

import javafx.application.Platform;

public class TransferManager
{
  private final List<Transfer> transfers = new ArrayList<> ();
  private Transfer currentTransfer;

  private final Screen screen;
  private final Site site;
  private final ReporterNode reporterNode;

  public TransferManager (Screen screen, Site site, AssistantStage assistantStage)
  {
    this.screen = screen;
    this.site = site;
    assistantStage.setTransferManager (this);
    this.reporterNode = assistantStage.getReporterNode ();
  }

  private void addTransfer (Transfer transfer)
  {
    if (transfer.isSendData ())
    {
      transfers.add (transfer);
      Platform.runLater ( () -> addBuffer (transfer));
    }
  }

  private void addBuffer (Transfer transfer)
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

    if (siteFolderName.isEmpty ())
      reporterNode.addBuffer (name, buffer);
    else
      reporterNode.addBuffer (name, buffer, siteFolderName);
  }

  // called from AssistantStage.getCurrentFileBuffer()
  public Optional<byte[]> getCurrentFileBuffer ()
  {
    FileNode fileNode = reporterNode.getSelectedNode ();
    if (fileNode == null)
    {
      System.out.println ("No fileNode selected in FilesTab.getCurrentFileBuffer()");
      return Optional.empty ();
    }
    else
      return Optional.of (fileNode.getReportData ().getBuffer ());
  }

  // called from FileTransferOutboundSF.processOpen()
  // Save the current transfer so it is available to subsequent FileTransferOutboundSF
  // commands.
  public void openTransfer (Transfer transfer)
  {
    currentTransfer = transfer;
  }

  public Optional<Transfer> getTransfer (FileTransferOutboundSF transferRecord)
  {
    if (currentTransfer == null)
    {
      System.out.println ("Null current transfer");
      return Optional.empty ();
    }

    currentTransfer.add (transferRecord);
    return Optional.of (currentTransfer);
  }

  public Optional<Transfer> closeTransfer (FileTransferOutboundSF transferRecord)
  {
    if (currentTransfer == null)
    {
      System.out.println ("Null current transfer");
      return Optional.empty ();
    }

    Transfer transfer = currentTransfer;
    currentTransfer.add (transferRecord);

    addTransfer (currentTransfer);                // add to the file tree
    currentTransfer = null;

    return Optional.of (transfer);
  }

  public void closeTransfer ()
  {
    currentTransfer = null;
  }
}