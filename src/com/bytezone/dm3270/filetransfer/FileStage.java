package com.bytezone.dm3270.filetransfer;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import com.bytezone.dm3270.application.WindowSaver;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class FileStage extends Stage
{
  private static final int PAGE_SIZE = 66;

  private final TabPane tabPane = new TabPane ();
  private final List<Transfer> transfers = new ArrayList<> ();
  private Transfer currentTransfer;
  private final WindowSaver windowSaver;
  private final Button hideButton = new Button ("Hide Window");

  private final Label lblLineSize = new Label ("Line size");
  private final Label lblPageSize = new Label ("Page size");
  private final Label lblHasASA = new Label ("ASA");
  private final Label lblHasCRLF = new Label ("CR/LF");
  private final Label lblHasASCII = new Label ("ASCII");
  private final Label lblTotalLines = new Label ("Lines");

  private final TextField txtLineSize = new TextField ();
  private final TextField txtPageSize = new TextField ();
  private final CheckBox chkHasASACodes = new CheckBox ();
  private final CheckBox chkCRLF = new CheckBox ();
  private final CheckBox chkASCII = new CheckBox ();
  private final TextField txtTotalLines = new TextField ();

  public FileStage (Preferences prefs)
  {
    setTitle ("Report display");
    windowSaver = new WindowSaver (prefs, this, "FileTransferStage");

    tabPane.setSide (Side.TOP);
    tabPane.setTabClosingPolicy (TabClosingPolicy.UNAVAILABLE);
    tabPane.setPrefSize (500, 500);                             // width, height

    HBox buttonBox = new HBox ();
    hideButton.setPrefWidth (150);
    buttonBox.setAlignment (Pos.CENTER_RIGHT);
    buttonBox.setPadding (new Insets (10, 10, 10, 10));         // trbl
    buttonBox.getChildren ().add (hideButton);

    HBox optionsBox = new HBox (10);
    optionsBox.setAlignment (Pos.CENTER_LEFT);
    optionsBox.setPadding (new Insets (10, 10, 10, 10));         // trbl
    txtPageSize.setPrefWidth (43);
    txtLineSize.setPrefWidth (43);
    txtTotalLines.setPrefWidth (60);
    optionsBox.getChildren ().addAll (lblPageSize, txtPageSize, lblLineSize, txtLineSize,
                                      lblHasCRLF, chkCRLF, lblHasASA, chkHasASACodes,
                                      lblHasASCII, chkASCII, lblTotalLines,
                                      txtTotalLines);

    BorderPane bottomBorderPane = new BorderPane ();
    bottomBorderPane.setLeft (optionsBox);
    bottomBorderPane.setRight (buttonBox);

    hideButton.setOnAction (e -> hide ());

    BorderPane borderPane = new BorderPane ();
    borderPane.setCenter (tabPane);
    borderPane.setBottom (bottomBorderPane);

    Scene scene = new Scene (borderPane);
    setScene (scene);

    if (!windowSaver.restoreWindow ())
      centerOnScreen ();

    setOnCloseRequest (e -> closeWindow ());

    tabPane.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldTab, newTab) -> select ((FileTab) newTab));
  }

  private void select (FileTab tab)
  {
    FileStructure fileStructure = tab.fileStructure;
    txtLineSize.setText (fileStructure.lineSize + "");
    txtPageSize.setText (PAGE_SIZE + "");
    chkHasASACodes.selectedProperty ().setValue (fileStructure.hasASA);
    chkCRLF.selectedProperty ().setValue (fileStructure.hasCRLF);
    chkASCII.selectedProperty ().setValue (fileStructure.encoding.equals ("UTF8"));
    txtTotalLines.setText (fileStructure.lines.size () + "");
  }

  public void addTransfer (Transfer transfer)
  {
    if (!transfer.isData ())
      return;

    transfers.add (transfer);

    FileTab tab = new FileTab (new FileStructure (transfer.combineDataBuffers ()));
    tab.setText (transfer.getFileName ());

    Platform.runLater ( () -> tabPane.getTabs ().add (tab));
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

  class FileTab extends Tab
  {
    final FileStructure fileStructure;
    private final LinePrinter linePrinter;
    TextArea textArea = new TextArea ();

    public FileTab (FileStructure fileStructure)
    {
      this.fileStructure = fileStructure;
      linePrinter = new LinePrinter (PAGE_SIZE, fileStructure);
      linePrinter.printBuffer ();

      textArea.setEditable (false);
      textArea.setFont (Font.font ("Monospaced", 12));
      textArea.setText (linePrinter.getOutput ());

      setContent (textArea);
      textArea.positionCaret (0);
    }
  }
}