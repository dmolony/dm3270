package com.bytezone.dm3270.filetransfer;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import com.bytezone.dm3270.application.WindowSaver;
import com.bytezone.dm3270.display.ScreenDetails;
import com.bytezone.dm3270.display.TSOCommandStatusListener;
import com.bytezone.reporter.application.ReporterNode;
import com.bytezone.reporter.application.TreePanel;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FileStage extends Stage implements TSOCommandStatusListener
{
  private final List<Transfer> transfers = new ArrayList<> ();
  private Transfer currentTransfer;
  private final WindowSaver windowSaver;
  private ReporterNode reporterNode;

  public FileStage (Preferences prefs)
  {
    setTitle ("Report display");

    try
    {
      reporterNode = new ReporterNode (prefs);
      setScene (new Scene (reporterNode.getRootNode (), 800, 592));
      reporterNode.getTreePanel ().getTree ().requestFocus ();
    }
    catch (NoClassDefFoundError e)
    {
      System.out.println ("ReporterNode class not available");
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

  public Transfer openTransfer (FileTransferOutboundSF transferRecord)
  {
    if (currentTransfer != null)
      addTransfer (currentTransfer);

    currentTransfer = new Transfer ();
    currentTransfer.add (transferRecord);
    return currentTransfer;
  }

  public Transfer getTransfer ()
  {
    return currentTransfer;
  }

  public Transfer closeTransfer (FileTransferOutboundSF transferRecord)
  {
    if (currentTransfer == null)
    {
      System.out.println ("Null");
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
  }
}