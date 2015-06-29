package com.bytezone.dm3270.jobs;

import java.util.prefs.Preferences;

import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.application.WindowSaver;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.FieldManager;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenDetails;
import com.bytezone.dm3270.display.TSOCommandStatusListener;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class JobStage extends Stage implements TSOCommandStatusListener
{
  private final Screen screen;
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private final WindowSaver windowSaver;
  private ConsolePane consolePane;
  private final Button btnHide = new Button ("Hide Window");
  private final Button btnExecute = new Button ("Execute");
  private final JobTable jobTable = new JobTable ();
  private final Label lblCommand = new Label ("Command");
  private final TextField txtCommand = new TextField ();

  private boolean isTSOCommandScreen;
  private Field tsoCommandField;
  private BatchJob selectedBatchJob;

  public JobStage (Screen screen)
  {
    this.screen = screen;
    setTitle ("Batch Jobs");
    windowSaver = new WindowSaver (prefs, this, "JobStage");

    HBox buttonBox = new HBox ();
    btnHide.setPrefWidth (120);
    buttonBox.setAlignment (Pos.CENTER_RIGHT);
    buttonBox.setPadding (new Insets (10, 10, 10, 10));// trbl
    buttonBox.getChildren ().add (btnHide);

    HBox optionsBox = new HBox (10);
    optionsBox.setAlignment (Pos.CENTER_LEFT);
    optionsBox.setPadding (new Insets (10, 10, 10, 10));// trbl
    txtCommand.setEditable (false);
    txtCommand.setPrefWidth (320);
    txtCommand.setFont (Font.font ("Monospaced", 12));
    txtCommand.setFocusTraversable (false);
    optionsBox.getChildren ().addAll (lblCommand, txtCommand, btnExecute);

    BorderPane bottomBorderPane = new BorderPane ();
    bottomBorderPane.setLeft (optionsBox);
    bottomBorderPane.setRight (buttonBox);

    btnHide.setOnAction (e -> hide ());
    btnExecute.setOnAction (e -> execute ());

    BorderPane borderPane = new BorderPane ();
    borderPane.setCenter (jobTable);
    borderPane.setBottom (bottomBorderPane);

    Scene scene = new Scene (borderPane, 500, 500);
    setScene (scene);

    if (!windowSaver.restoreWindow ())
      centerOnScreen ();

    setOnCloseRequest (e -> closeWindow ());

    jobTable.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, newSelection) -> {
          if (newSelection != null)
            select (newSelection);
        });
  }

  public void setConsolePane (ConsolePane consolePane)
  {
    this.consolePane = consolePane;
  }

  private void select (BatchJob batchJob)
  {
    selectedBatchJob = batchJob;
    setText ();
  }

  private void setText ()
  {
    String report = selectedBatchJob.getOutputFile ();
    String command = report == null ? selectedBatchJob.outputCommand ()
        : String.format ("IND$FILE GET %s", report);

    if (!isTSOCommandScreen)
      command = "TSO " + command;

    txtCommand.setText (command);
    setButton ();
  }

  private void setButton ()
  {
    if (selectedBatchJob == null || selectedBatchJob.getJobCompleted () == null)
    {
      btnExecute.setDisable (true);
      return;
    }

    String command = txtCommand.getText ();
    btnExecute.setDisable (tsoCommandField == null || command.isEmpty ());
  }

  private void execute ()
  {
    if (tsoCommandField != null)                             // are we on a suitable
                            // screen?
    {
      tsoCommandField.setText (txtCommand.getText ());
      if (consolePane != null)
        consolePane.sendAID (AIDCommand.AID_ENTER, "ENTR");

      if (selectedBatchJob.getOutputFile () == null)
      {
        selectedBatchJob.setOutputFile (selectedBatchJob.datasetName ());
        jobTable.refresh ();
      }
    }
  }

  public void addBatchJob (BatchJob batchJob)
  {
    jobTable.addJob (batchJob);
  }

  public BatchJob getBatchJob (int jobNumber)
  {
    return jobTable.getBatchJob (jobNumber);
  }

  private void closeWindow ()
  {
    windowSaver.saveWindow ();
    hide ();
  }

  @Override
  public void screenChanged (FieldManager fieldManager)
  {
    ScreenDetails screenDetails = screen.getScreenDetails ();
    this.isTSOCommandScreen = screenDetails.isTSOCommandScreen ();
    this.tsoCommandField = screenDetails.getTSOCommandField ();
    setButton ();
  }

  // ---------------------------------------------------------------------------------//
  // Batch jobs
  // ---------------------------------------------------------------------------------//

  public void batchJobSubmitted (int jobNumber, String jobName)
  {
    BatchJob batchJob = new BatchJob (jobNumber, jobName);
    addBatchJob (batchJob);

    // if (!isShowing ()) // this should be a preference setting
    // show ();
  }

  public void batchJobEnded (int jobNumber, String jobName, String time,
      int conditionCode)
  {
    BatchJob batchJob = getBatchJob (jobNumber);
    if (batchJob != null)
    {
      batchJob.completed (time, conditionCode);
      jobTable.refresh ();                          // temp fix before jdk 8u60
    }
  }
}