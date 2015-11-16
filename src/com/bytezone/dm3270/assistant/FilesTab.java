package com.bytezone.dm3270.assistant;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.prefs.Preferences;

import com.bytezone.dm3270.application.Site;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.filetransfer.FileTransferOutboundSF;
import com.bytezone.dm3270.filetransfer.Transfer;
import com.bytezone.reporter.application.FileNode;
import com.bytezone.reporter.application.NodeSelectionListener;
import com.bytezone.reporter.application.ReporterNode;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;

public class FilesTab extends AbstractTransferTab implements NodeSelectionListener
{
  private final List<Transfer> transfers = new ArrayList<> ();
  private Transfer currentTransfer;

  private final ReporterNode reporterNode;
  private FileNode currentFileNode;

  public FilesTab (Screen screen, Site site, TextField text, Button execute,
      Preferences prefs)
  {
    super ("Local Files", screen, site, text, execute);

    reporterNode = new ReporterNode (prefs);
    reporterNode.addNodeSelectionListener (this);
    reporterNode.requestFocus ();

    currentFileNode = reporterNode.getSelectedNode ();

    setContent (reporterNode);
  }

  @Override
  public void nodeSelected (FileNode fileNode)
  {
    currentFileNode = fileNode;
    setText ();
    fireFileSelected (currentFileNode.toString ());
  }

  MenuBar getMenuBar ()
  {
    return reporterNode.getMenuBar ();
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
    String name = transfer.getFileName ();
    if (!transfer.hasTLQ ())
    {
      String tlq = screen.getPrefix ();
      System.out.printf ("Name: %s, hasTLQ: %s, TLQ: %s%n", name, transfer.hasTLQ (),
                         tlq);
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
    else
      System.out.println ("Current site unknown");

    if (siteFolderName.isEmpty ())
      reporterNode.addBuffer (name.toUpperCase (), transfer.combineDataBuffers ());
    else
      reporterNode.addBuffer (name.toUpperCase (), transfer.combineDataBuffers (),
                              siteFolderName);
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

  @Override
  protected void setText ()
  {
    if (currentFileNode == null)
    {
      eraseCommand ();
      return;
    }

    String fileName = currentFileNode.toString ().toUpperCase ();
    if (fileName.endsWith (".TXT"))
      fileName = fileName.substring (0, fileName.length () - 4);

    String command = "IND$FILE PUT " + fileName;
    if (currentFileNode.isAscii ())
      command += " ASCII CRLF";

    if (!screenDetails.isTSOCommandScreen ())
      command = "TSO " + command;

    txtCommand.setText (command);
  }

  // ---------------------------------------------------------------------------------//
  // Listener events
  // ---------------------------------------------------------------------------------//

  private final Set<FileSelectionListener> selectionListeners = new HashSet<> ();

  void fireFileSelected (String filename)
  {
    selectionListeners.forEach (l -> l.fileSelected (filename));
  }

  void addFileSelectionListener (FileSelectionListener listener)
  {
    if (!selectionListeners.contains (listener))
    {
      selectionListeners.add (listener);
      if (currentFileNode != null)
        listener.fileSelected (currentFileNode.toString ());
    }
  }

  void removeFileSelectionListener (FileSelectionListener listener)
  {
    if (selectionListeners.contains (listener))
      selectionListeners.remove (listener);
  }
}