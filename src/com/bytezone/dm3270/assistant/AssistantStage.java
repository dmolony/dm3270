package com.bytezone.dm3270.assistant;

import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.application.KeyboardStatusListener;
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

public class AssistantStage extends Stage implements ScreenChangeListener,
    TSOCommandListener, KeyboardStatusListener, BatchJobListener
{
  private final static String OS = System.getProperty ("os.name");
  private final static boolean SYSTEM_MENUBAR = OS != null && OS.startsWith ("Mac");

  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private final WindowSaver windowSaver;
  private final MenuBar menuBar = new MenuBar ();

  private final TSOCommand tsoCommand;
  private final Button btnHide = new Button ("Hide Window");

  private final TabPane tabPane = new TabPane ();
  private final DatasetTab datasetTab;
  private final BatchJobTab jobTab;
  private final FilesTab fileTab;
  private final TransfersTab transfersTab;
  private final CommandsTab commandsTab;
  private final List<ScreenChangeListener> screenChangeListeners;

  public AssistantStage (Screen screen)
  {
    setTitle ("File Transfers");

    setOnCloseRequest (e -> closeWindow ());
    btnHide.setOnAction (e -> closeWindow ());

    tsoCommand = new TSOCommand (screen);
    datasetTab = new DatasetTab (screen, tsoCommand.txtCommand, tsoCommand.btnExecute);
    jobTab = new BatchJobTab (screen, tsoCommand.txtCommand, tsoCommand.btnExecute);
    fileTab = new FilesTab (screen, tsoCommand.txtCommand, tsoCommand.btnExecute, prefs);
    commandsTab = new CommandsTab (screen, tsoCommand.txtCommand, tsoCommand.btnExecute);
    transfersTab =
        new TransfersTab (screen, tsoCommand.txtCommand, tsoCommand.btnExecute);
    tabPane.getTabs ().addAll (datasetTab, jobTab, fileTab, commandsTab, transfersTab);
    tabPane.setTabMinWidth (80);

    screenChangeListeners =
        Arrays.asList (datasetTab, jobTab, fileTab, commandsTab, transfersTab);

    datasetTab.addDatasetSelectionListener (transfersTab);
    fileTab.addFileSelectionListener (transfersTab);
    jobTab.addJobSelectionListener (transfersTab);
    fileTab.getCurrentFileBuffer ();

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

    menuBar.setUseSystemMenuBar (SYSTEM_MENUBAR);

    Scene scene = new Scene (borderPane, 800, 500);// width/height
    setScene (scene);

    setOnCloseRequest (e -> closeWindow ());

    windowSaver = new WindowSaver (prefs, this, "DatasetStage");
    windowSaver.restoreWindow ();

    tabPane.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, newSelection) -> {
          if (newSelection != null)
            select (newSelection);
        });

    tabPane.getSelectionModel ().select (datasetTab);
  }

  private void select (Tab tab)
  {
    ((AbstractTransferTab) tab).setText ();
  }

  public void setConsolePane (ConsolePane consolePane)
  {
    tsoCommand.setConsolePane (consolePane);
  }

  public void closeWindow ()
  {
    windowSaver.saveWindow ();
    hide ();
  }

  @Override
  public void batchJobSubmitted (int jobNumber, String jobName)
  {
    jobTab.batchJobSubmitted (jobNumber, jobName);
  }

  @Override
  public void batchJobEnded (int jobNumber, String jobName, String time,
      int conditionCode)
  {
    jobTab.batchJobEnded (jobNumber, jobName, time, conditionCode);
  }

  @Override
  public void batchJobFailed (int jobNumber, String jobName, String time)
  {
    jobTab.batchJobFailed (jobNumber, jobName, time);
  }

  public void openTransfer (Transfer transfer)
  {
    fileTab.openTransfer (transfer);
  }

  public Transfer getTransfer (FileTransferOutboundSF transferRecord)
  {
    return fileTab.getTransfer (transferRecord);
  }

  public void closeTransfer ()
  {
    fileTab.closeTransfer ();
  }

  public Transfer closeTransfer (FileTransferOutboundSF transferRecord)
  {
    return fileTab.closeTransfer (transferRecord);
  }

  public byte[] getCurrentFileBuffer ()
  {
    return fileTab.getCurrentFileBuffer ();
  }

  @Override
  public void screenChanged ()
  {
    for (ScreenChangeListener listener : screenChangeListeners)
      listener.screenChanged ();
  }

  @Override
  public void keyboardStatusChanged (KeyboardStatusChangedEvent evt)
  {
    screenChanged ();
  }

  @Override
  public void tsoCommand (String command)
  {
    jobTab.tsoCommand (command);
    commandsTab.tsoCommand (command);
  }
}