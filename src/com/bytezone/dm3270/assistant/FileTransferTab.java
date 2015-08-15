package com.bytezone.dm3270.assistant;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.ScreenDetails;
import com.bytezone.dm3270.display.TSOCommandStatusListener;
import com.bytezone.dm3270.filetransfer.FileTransferOutboundSF;
import com.bytezone.dm3270.filetransfer.Transfer;
import com.bytezone.dm3270.filetransfer.Transfer.TransferContents;
import com.bytezone.dm3270.filetransfer.Transfer.TransferType;
import com.bytezone.reporter.application.NodeSelectionListener;
import com.bytezone.reporter.application.ReporterNode;
import com.bytezone.reporter.application.TreePanel;
import com.bytezone.reporter.application.TreePanel.FileNode;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class FileTransferTab extends TransferTab
    implements TSOCommandStatusListener, NodeSelectionListener
{
  private final List<Transfer> transfers = new ArrayList<> ();
  private Transfer currentTransfer;

  private ReporterNode reporterNode;
  private final BorderPane borderPane = new BorderPane ();

  private boolean isTSOCommandScreen;
  private Field tsoCommandField;

  public FileTransferTab (TextField text, Button execute, Preferences prefs)
  {
    super ("Report Display", text, execute);

    try
    {
      reporterNode = new ReporterNode (prefs);
      borderPane.setCenter (reporterNode.getRootNode ());
      //      setScene (new Scene (borderPane, 800, 592));
      reporterNode.getTreePanel ().getTree ().requestFocus ();
      reporterNode.getTreePanel ().addNodeSelectionListener (this);

      //      menuBar = reporterNode.getMenuBar ();
      //      menuBar.getMenus ().add (getTransferMenu ());

      //      optionsBox = new HBox (10);
      //      optionsBox.setAlignment (Pos.CENTER_LEFT);
      //      optionsBox.setPadding (new Insets (10, 10, 10, 10));// trbl
      //      txtCommand.setEditable (false);
      //      txtCommand.setPrefWidth (400);
      //      txtCommand.setFont (Font.font ("Monospaced", 12));
      //      txtCommand.setFocusTraversable (false);
      //      optionsBox.getChildren ().addAll (lblCommand, txtCommand, btnExecute);
      setContent (borderPane);
    }
    catch (NoClassDefFoundError e)
    {
      System.out.println ("ReporterNode class not available");
    }

    if (reporterNode == null)
    {

    }

    //    setOnCloseRequest (e -> closeWindow ());

    //    windowSaver = new WindowSaver (prefs, this, "FileTransferStage");
    //    windowSaver.restoreWindow ();
  }

  //  private Menu getTransferMenu ()
  //  {
  //    Menu menuTransfer = new Menu ("Transfer");
  //
  //    MenuItem menuItemUpload = getMenuItem ("Upload file", e -> uploadFile (), KeyCode.U);
  //    MenuItem menuItemDownload =
  //        getMenuItem ("Download file", e -> downloadFile (), KeyCode.D);
  //    menuTransfer.getItems ().addAll (menuItemUpload, menuItemDownload);
  //
  //    return menuTransfer;
  //  }

  //  private MenuItem getMenuItem (String text, EventHandler<ActionEvent> eventHandler,
  //      KeyCode keyCode)
  //  {
  //    MenuItem menuItem = new MenuItem (text);
  //    menuItem.setOnAction (eventHandler);
  //    if (keyCode != null)
  //      menuItem.setAccelerator (new KeyCodeCombination (keyCode,
  //          KeyCombination.SHORTCUT_DOWN));
  //    return menuItem;
  //  }

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

  @Override
      void setText ()
  {
    //    String report = selectedBatchJob.getOutputFile ();
    //    String command = report == null ? selectedBatchJob.outputCommand ()
    //        : String.format ("IND$FILE GET %s", report);
    String command = "IND$FILE PUT " + reporterNode.getSelectedNode ();

    if (!isTSOCommandScreen)
      command = "TSO " + command;

    txtCommand.setText (command);
    setButton ();
  }

  @Override
      void setButton ()
  {
    String command = txtCommand.getText ();
    btnExecute.setDisable (tsoCommandField == null || command.isEmpty ());
  }

  @Override
  public void screenChanged (ScreenDetails screenDetails)
  {
    isTSOCommandScreen = screenDetails.isTSOCommandScreen ();
    tsoCommandField = screenDetails.getTSOCommandField ();
    setText ();
  }

  @Override
  public void nodeSelected (FileNode fileNode)
  {
    setText ();
  }
}