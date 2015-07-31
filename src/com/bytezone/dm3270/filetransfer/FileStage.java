package com.bytezone.dm3270.filetransfer;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import com.bytezone.dm3270.application.WindowSaver;
import com.bytezone.dm3270.display.ScreenDetails;
import com.bytezone.dm3270.display.TSOCommandStatusListener;
import com.bytezone.reporter.application.ReporterScene;
import com.bytezone.reporter.application.TreePanel;

import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class FileStage extends Stage implements TSOCommandStatusListener
{
  private final List<Transfer> transfers = new ArrayList<> ();
  private Transfer currentTransfer;
  private final WindowSaver windowSaver;
  private final ReporterScene reporterScene;

  public FileStage (Preferences prefs)
  {
    setTitle ("Report display");

    BorderPane borderPane = new BorderPane ();
    reporterScene = new ReporterScene (prefs, borderPane);

    setScene (reporterScene);
    setOnCloseRequest (e -> closeWindow ());

    windowSaver = new WindowSaver (prefs, this, "FileTransferStage");
    windowSaver.restoreWindow ();
  }

  public void addTransfer (Transfer transfer)
  {
    if (!transfer.isData ())
      return;

    transfers.add (transfer);
    TreePanel treePanel = reporterScene.getTreePanel ();

    Platform.runLater ( () -> treePanel.addBuffer (transfer.getFileName (),
                                                   transfer.combineDataBuffers ()));
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