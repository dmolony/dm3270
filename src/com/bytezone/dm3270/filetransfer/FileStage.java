package com.bytezone.dm3270.filetransfer;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import com.bytezone.dm3270.application.WindowSaver;
import com.bytezone.dm3270.display.ScreenDetails;
import com.bytezone.dm3270.display.TSOCommandStatusListener;
import com.bytezone.dm3270.filetransfer.Transfer.TransferContents;
import com.bytezone.dm3270.filetransfer.Transfer.TransferType;
import com.bytezone.reporter.application.ReporterNode;
import com.bytezone.reporter.application.TreePanel;
import com.bytezone.reporter.application.TreePanel.FileNode;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class FileStage extends Stage implements TSOCommandStatusListener//, NodeSelectionListener
{
  private final List<Transfer> transfers = new ArrayList<> ();
  private Transfer currentTransfer;
  private final WindowSaver windowSaver;

  private ReporterNode reporterNode;
  private final BorderPane borderPane = new BorderPane ();
  private MenuBar menuBar;

  private HBox transferPanel;
  private final TextField txtDatasetName = new TextField ();
  private final Label lblTransfer = new Label ("Transfer file");
  private boolean isTransferPanelVisible;

  public FileStage (Preferences prefs)
  {
    setTitle ("Report display");

    try
    {
      reporterNode = new ReporterNode (prefs);
      borderPane.setCenter (reporterNode.getRootNode ());
      setScene (new Scene (borderPane, 800, 592));
      reporterNode.getTreePanel ().getTree ().requestFocus ();

      menuBar = reporterNode.getMenuBar ();
      menuBar.getMenus ().add (getTransferMenu ());
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

  private Menu getTransferMenu ()
  {
    Menu menuTransfer = new Menu ("Transfer");

    MenuItem menuItemUpload = getMenuItem ("Upload file", e -> uploadFile (), KeyCode.U);
    MenuItem menuItemDownload =
        getMenuItem ("Download file", e -> downloadFile (), KeyCode.D);
    menuTransfer.getItems ().addAll (menuItemUpload, menuItemDownload);

    return menuTransfer;
  }

  private MenuItem getMenuItem (String text, EventHandler<ActionEvent> eventHandler,
      KeyCode keyCode)
  {
    MenuItem menuItem = new MenuItem (text);
    menuItem.setOnAction (eventHandler);
    if (keyCode != null)
      menuItem.setAccelerator (new KeyCodeCombination (keyCode,
          KeyCombination.SHORTCUT_DOWN));
    return menuItem;
  }

  public void addTransfer (Transfer transfer)
  {
    System.out.println (transfer);
    if (transfer.getTransferContents () == TransferContents.DATA
        && transfer.getTransferType () == TransferType.SEND)
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

  public byte[] getCurrentFileBuffer ()
  {
    FileNode fileNode = reporterNode.getSelectedNode ();
    if (fileNode == null)
    {
      System.out.println ("No file selected to transfer");
      return null;
    }
    else
    {
      System.out.println ("File to transfer: " + fileNode);
      return fileNode.getBuffer ();
    }
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

  private void uploadFile ()
  {
    if (transferPanel == null)
    {
      transferPanel = new HBox (10);
      transferPanel.setPadding (new Insets (10, 10, 10, 10));// trbl
      transferPanel.setAlignment (Pos.CENTER_LEFT);
      transferPanel.getChildren ().addAll (lblTransfer, txtDatasetName);
    }

    if (isTransferPanelVisible)
    {
      borderPane.setBottom (null);
      isTransferPanelVisible = false;
    }
    else
    {
      borderPane.setBottom (transferPanel);
      isTransferPanelVisible = true;
    }
  }

  private void downloadFile ()
  {

  }

  @Override
  public void screenChanged (ScreenDetails screenDetails)
  {
    System.out.println (screenDetails);
  }
}