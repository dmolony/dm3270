package com.bytezone.dm3270.filetransfer;

import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import com.bytezone.dm3270.application.WindowSaver;
import com.bytezone.dm3270.display.ScreenDetails;
import com.bytezone.dm3270.display.TSOCommandStatusListener;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class FileStage extends Stage implements TSOCommandStatusListener
{
  private final static String OS = System.getProperty ("os.name");
  private final static boolean SYSTEM_MENUBAR = OS != null && OS.startsWith ("Mac");
  private static final int PAGE_SIZE = 66;

  private final TabPane tabPane = new TabPane ();
  private final List<Transfer> transfers = new ArrayList<> ();
  private Transfer currentTransfer;
  private final WindowSaver windowSaver;

  private final Button btnHide = new Button ("Hide Window");

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

  private final MenuBar menuBar = new MenuBar ();

  public FileStage (Preferences prefs)
  {
    setTitle ("Report display");
    windowSaver = new WindowSaver (prefs, this, "FileTransferStage");

    tabPane.setSide (Side.TOP);
    tabPane.setTabClosingPolicy (TabClosingPolicy.UNAVAILABLE);
    tabPane.setPrefSize (500, 500);                             // width, height

    HBox buttonBox = new HBox (10);
    btnHide.setPrefWidth (100);

    buttonBox.setAlignment (Pos.CENTER_RIGHT);
    buttonBox.setPadding (new Insets (10, 10, 10, 10));           // trbl

    HBox optionsBox = new HBox (10);
    optionsBox.setAlignment (Pos.CENTER_LEFT);
    optionsBox.setPadding (new Insets (10, 10, 10, 10));         // trbl
    txtPageSize.setPrefWidth (43);
    txtLineSize.setPrefWidth (43);
    txtTotalLines.setPrefWidth (60);

    txtPageSize.setEditable (false);
    txtTotalLines.setEditable (false);
    txtPageSize.setFocusTraversable (false);
    txtTotalLines.setFocusTraversable (false);

    optionsBox.getChildren ().addAll (lblPageSize, txtPageSize, lblLineSize, txtLineSize,
                                      lblHasCRLF, chkCRLF, lblHasASA, chkHasASACodes,
                                      lblHasASCII, chkASCII, lblTotalLines,
                                      txtTotalLines);

    BorderPane bottomBorderPane = new BorderPane ();
    bottomBorderPane.setLeft (optionsBox);
    bottomBorderPane.setRight (buttonBox);

    menuBar.getMenus ().addAll (getFileMenu ());

    BorderPane topBorderPane = new BorderPane ();
    topBorderPane.setTop (menuBar);
    if (SYSTEM_MENUBAR)
      menuBar.useSystemMenuBarProperty ().set (true);

    btnHide.setOnAction (e -> closeWindow ());
    setOnCloseRequest (e -> closeWindow ());

    BorderPane borderPane = new BorderPane ();
    borderPane.setTop (topBorderPane);
    borderPane.setCenter (tabPane);
    borderPane.setBottom (bottomBorderPane);

    Scene scene = new Scene (borderPane);
    setScene (scene);

    if (!windowSaver.restoreWindow ())
      centerOnScreen ();

    tabPane.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldTab, newTab) -> select ((FileTab) newTab));
  }

  private Menu getFileMenu ()
  {
    Menu menuFile = new Menu ("File");

    MenuItem menuItemOpen = getMenuItem ("Open...", e -> openFile (), KeyCode.O);
    MenuItem menuItemSave = getMenuItem ("Save...", e -> saveFile (), KeyCode.S);
    MenuItem menuItemPrint = getMenuItem ("Page setup", e -> pageSetup (), null);
    MenuItem menuItemPageSetup = getMenuItem ("Print", e -> printFile (), KeyCode.P);
    MenuItem menuItemClose = getMenuItem ("Close window", e -> closeWindow (), KeyCode.W);

    menuFile.getItems ().addAll (menuItemOpen, menuItemSave, menuItemPageSetup,
                                 menuItemPrint, menuItemClose);

    return menuFile;
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

  private void openFile ()
  {

  }

  private void pageSetup ()
  {
    SwingUtilities.invokeLater (new Runnable ()
    {
      @Override
      public void run ()
      {
        java.awt.print.PrinterJob printerJob = java.awt.print.PrinterJob.getPrinterJob ();

        PageFormat pageFormat = printerJob.defaultPage ();
        printerJob.pageDialog (pageFormat);
      }
    });
  }

  private void printFile ()
  {
    FileTab fileTab = getSelectedTab ();
    if (fileTab == null)
      return;

    SwingUtilities.invokeLater (new Runnable ()
    {
      @Override
      public void run ()
      {
        java.awt.print.PrinterJob printerJob = java.awt.print.PrinterJob.getPrinterJob ();

        if (printerJob.printDialog ())
        {
          printerJob.setPrintable (fileTab.report);
          try
          {
            printerJob.print ();
          }
          catch (PrinterException e)
          {
            e.printStackTrace ();
          }
        }
      }
    });
  }

  private void saveFile ()
  {
    FileTab fileTab = getSelectedTab ();
    if (fileTab == null)
      return;

    System.out.println ("Save:  " + fileTab.getTitle ());
  }

  private FileTab getSelectedTab ()
  {
    return (FileTab) tabPane.getSelectionModel ().getSelectedItem ();
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

    FileTab tab = new FileTab (new FileStructure (transfer));
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

  @Override
  public void screenChanged (ScreenDetails screenDetails)
  {
  }

  class FileTab extends Tab
  {
    final FileStructure fileStructure;
    private final LinePrinter linePrinter;
    TextArea textArea = new TextArea ();
    final Report report;

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
      report = new Report (fileStructure, textArea.getText ());
    }

    public String getTitle ()
    {
      return fileStructure.transfer.getFileName ();
    }
  }
}