package com.bytezone.dm3270.assistant;

import java.util.prefs.Preferences;

import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.application.WindowSaver;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.TSOCommandListener;
import com.bytezone.dm3270.filetransfer.FileTransferOutboundSF;
import com.bytezone.dm3270.filetransfer.Transfer;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AssistantStage extends Stage
    implements ScreenChangeListener, TSOCommandListener
{
  private final static String OS = System.getProperty ("os.name");
  private final static boolean SYSTEM_MENUBAR = OS != null && OS.startsWith ("Mac");

  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private final WindowSaver windowSaver;
  private final MenuBar menuBar = new MenuBar ();
  private final Screen screen;

  private final TSOCommand tsoCommand;
  private final Button btnHide = new Button ("Hide Window");

  private final TabPane tabPane = new TabPane ();
  private final DatasetTab datasetTab;
  private final JobTab jobTab;
  private final FileTransferTab fileTransferTab;

  public AssistantStage (Screen screen)
  {
    setTitle ("Session Details");

    this.screen = screen;

    setOnCloseRequest (e -> closeWindow ());
    btnHide.setOnAction (e -> closeWindow ());

    tsoCommand = new TSOCommand (screen);
    datasetTab = new DatasetTab (screen, tsoCommand.txtCommand, tsoCommand.btnExecute);
    jobTab = new JobTab (screen, tsoCommand.txtCommand, tsoCommand.btnExecute);
    fileTransferTab =
        new FileTransferTab (screen, tsoCommand.txtCommand, tsoCommand.btnExecute, prefs);
    tabPane.getTabs ().addAll (datasetTab, jobTab, fileTransferTab);

    AnchorPane anchorPane = new AnchorPane ();
    AnchorPane.setLeftAnchor (tsoCommand.getBox (), 10.0);
    AnchorPane.setBottomAnchor (tsoCommand.getBox (), 10.0);
    AnchorPane.setTopAnchor (tsoCommand.getBox (), 10.0);
    AnchorPane.setTopAnchor (btnHide, 10.0);
    AnchorPane.setBottomAnchor (btnHide, 10.0);
    AnchorPane.setRightAnchor (btnHide, 10.0);
    anchorPane.getChildren ().addAll (tsoCommand.getBox (), btnHide);

    BorderPane borderPane = new BorderPane ();
    borderPane.setTop (menuBar);
    borderPane.setCenter (tabPane);
    borderPane.setBottom (anchorPane);

    if (SYSTEM_MENUBAR)
      menuBar.useSystemMenuBarProperty ().set (true);

    Scene scene = new Scene (borderPane, 800, 500);// width/height
    setScene (scene);

    windowSaver = new WindowSaver (prefs, this, "DatasetStage");
    windowSaver.restoreWindow ();

    tabPane.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, newSelection) -> {
          if (newSelection != null)
            select (newSelection);
        });
  }

  private void select (Tab tab)
  {
    ((TransferTab) tab).setText ();
  }

  public void setConsolePane (ConsolePane consolePane)
  {
    tsoCommand.setConsolePane (consolePane);
  }

  private void closeWindow ()
  {
    windowSaver.saveWindow ();
    hide ();
  }

  @Override
  public void screenChanged ()
  {
    //    ScreenDetails screenDetails = screen.getScreenDetails ();
    //    tsoCommand.screenChanged ();
    datasetTab.screenChanged ();
    jobTab.screenChanged ();
  }

  public void batchJobSubmitted (int jobNumber, String jobName)
  {
    jobTab.batchJobSubmitted (jobNumber, jobName);
  }

  public void batchJobEnded (int jobNumber, String jobName, String time,
      int conditionCode)
  {
    jobTab.batchJobEnded (jobNumber, jobName, time, conditionCode);
  }

  public void openTransfer (Transfer transfer)
  {
    fileTransferTab.openTransfer (transfer);
  }

  public Transfer getTransfer (FileTransferOutboundSF transferRecord)
  {
    return fileTransferTab.getTransfer (transferRecord);
  }

  public void closeTransfer ()
  {
    fileTransferTab.closeTransfer ();
  }

  public Transfer closeTransfer (FileTransferOutboundSF transferRecord)
  {
    return fileTransferTab.closeTransfer (transferRecord);
  }

  public byte[] getCurrentFileBuffer ()
  {
    return fileTransferTab.getCurrentFileBuffer ();
  }

  @Override
  public void tsoCommand (String command)
  {
    jobTab.tsoCommand (command);
  }
}