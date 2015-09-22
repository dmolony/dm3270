package com.bytezone.dm3270.assistant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenDetails;
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

  public FilesTab (Screen screen, TextField text, Button execute, Preferences prefs)
  {
    super ("Local Files", screen, text, execute);

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

  public void addTransfer (Transfer transfer)
  {
    if (transfer.isSendData ())
    {
      transfers.add (transfer);
      Platform.runLater ( () -> addBuffer (transfer));
    }
  }

  private void addBuffer (Transfer transfer)
  {
    reporterNode.addBuffer (transfer.getFileName (), transfer.combineDataBuffers ());
  }

  public byte[] getCurrentFileBuffer ()
  {
    FileNode fileNode = reporterNode.getSelectedNode ();
    if (fileNode == null)
    {
      System.out.println ("No file selected to transfer");
      return null;
    }
    else
      return fileNode.getReportData ().getBuffer ();
  }

  // called from FileTransferOutboundSF.processOpen()
  // Save the current transfer so it is available to subsequent FileTransferOutboundSF
  // commands.
  public void openTransfer (Transfer transfer)
  {
    currentTransfer = transfer;
  }

  public Transfer getTransfer (FileTransferOutboundSF transferRecord)
  {
    currentTransfer.add (transferRecord);
    return currentTransfer;
  }

  public Transfer closeTransfer (FileTransferOutboundSF transferRecord)
  {
    if (currentTransfer == null)
    {
      System.out.println ("Null current transfer");
      return null;
    }

    Transfer transfer = currentTransfer;
    currentTransfer.add (transferRecord);

    addTransfer (currentTransfer);// add to the file tree
    currentTransfer = null;

    return transfer;
  }

  public void closeTransfer ()
  {
    currentTransfer = null;
  }

  @Override
  public void screenChanged (ScreenDetails screenDetails)
  {
    if (isSelected ())
      setText ();
  }

  @Override
  public void keyboardStatusChanged (KeyboardStatusChangedEvent evt)
  {
    if (isSelected ())
      setText ();
  }

  @Override
  protected void setText ()
  {
    ScreenDetails screenDetails = screen.getScreenDetails ();
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
    btnExecute.setDisable (screen.isKeyboardLocked ()
        || screenDetails.getTSOCommandField () == null);
  }

  private final Set<FileSelectionListener> selectionListeners = new HashSet<> ();

  void fireFileSelected (String filename)
  {
    for (FileSelectionListener listener : selectionListeners)
      listener.fileSelected (filename);
  }

  void addFileSelectionListener (FileSelectionListener listener)
  {
    selectionListeners.add (listener);
    if (currentFileNode != null)
      listener.fileSelected (currentFileNode.toString ());
  }

  void removeFileSelectionListener (FileSelectionListener listener)
  {
    selectionListeners.remove (listener);
  }
}