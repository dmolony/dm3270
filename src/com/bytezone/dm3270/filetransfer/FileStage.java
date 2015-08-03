package com.bytezone.dm3270.filetransfer;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import com.bytezone.dm3270.application.WindowSaver;
import com.bytezone.dm3270.display.ScreenDetails;
import com.bytezone.dm3270.display.TSOCommandStatusListener;
import com.bytezone.reporter.application.NodeSelectionListener;
import com.bytezone.reporter.application.ReporterNode;
import com.bytezone.reporter.application.TreePanel;
import com.bytezone.reporter.application.TreePanel.FileNode;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FileStage extends Stage
    implements TSOCommandStatusListener, NodeSelectionListener
{
  private final List<Transfer> transfers = new ArrayList<> ();
  private Transfer currentTransfer;
  private final WindowSaver windowSaver;

  private ReporterNode reporterNode;
  private FileNode fileNode;

  public FileStage (Preferences prefs)
  {
    setTitle ("Report display");

    try
    {
      reporterNode = new ReporterNode (prefs);
      setScene (new Scene (reporterNode.getRootNode (), 800, 592));
      reporterNode.getTreePanel ().getTree ().requestFocus ();
      reporterNode.getTreePanel ().addNodeSelectionListener (this);
    }
    catch (NoClassDefFoundError e)
    {
      System.out.println ("ReporterNode class not available");
    }

    if (reporterNode == null)
    {

    }

    setOnCloseRequest (e -> closeWindow ());

    windowSaver = new WindowSaver (prefs, this, "FileTransferStage");
    windowSaver.restoreWindow ();
  }

  public void addTransfer (Transfer transfer)
  {
    if (transfer.isData ())
    {
      transfers.add (transfer);
      Platform.runLater ( () -> addBuffer (transfer));
    }
  }

  private void addBuffer (Transfer transfer)
  {
    if (reporterNode == null)
      return;

    TreePanel treePanel = reporterNode.getTreePanel ();
    treePanel.addBuffer (transfer.getFileName (), transfer.combineDataBuffers ());
    if (!this.isShowing ())
      show ();
  }

  private void closeWindow ()
  {
    windowSaver.saveWindow ();
    hide ();
  }

  // called from FileTransferOutboundSF.processOpen()
  public Transfer openTransfer (FileTransferOutboundSF transferRecord)
  {
    //    if (currentTransfer != null)
    //      addTransfer (currentTransfer);
    assert currentTransfer == null;

    currentTransfer = new Transfer ();
    currentTransfer.add (transferRecord);

    //    System.out.printf ("Node %s inbound %s%n", reporterNode,
    //                       currentTransfer.isInbound ());
    //    if (reporterNode != null && currentTransfer.isInbound ())
    //    {
    //      System.out.println ("setting buffer");
    //      currentTransfer.setTransferBuffer (fileNode.getBuffer ());
    //    }

    return currentTransfer;
  }

  // called from FileTransferOutboundSF.processOpen()
  public void setBuffer (Transfer transfer)
  {
    if (fileNode == null)
      System.out.println ("No file selected to transfer");
    else
      transfer.setTransferBuffer (fileNode.getBuffer ());
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

    addTransfer (currentTransfer);
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
    //    System.out.println (screenDetails);
  }

  @Override
  public void nodeSelected (FileNode fileNode)
  {
    //    System.out.println (fileNode);
    this.fileNode = fileNode;
  }
}