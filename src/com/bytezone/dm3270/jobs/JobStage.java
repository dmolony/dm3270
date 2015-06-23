package com.bytezone.dm3270.jobs;

import java.util.prefs.Preferences;

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

import com.bytezone.dm3270.application.WindowSaver;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.TSOCommandStatusListener;

public class JobStage extends Stage implements TSOCommandStatusListener
{
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private final WindowSaver windowSaver;
  private final Button hideButton = new Button ("Hide Window");
  private final JobTable jobTable = new JobTable ();
  private final Label lblCommand = new Label ("Command");
  private final TextField txtCommand = new TextField ();

  private boolean isTSOCommandScreen;
  private Field tsoCommandField;
  private BatchJob selectedBatchJob;

  public JobStage ()
  {
    setTitle ("Batch Jobs");
    windowSaver = new WindowSaver (prefs, this, "JobStage");

    HBox buttonBox = new HBox ();
    hideButton.setPrefWidth (150);
    buttonBox.setAlignment (Pos.CENTER_RIGHT);
    buttonBox.setPadding (new Insets (10, 10, 10, 10));         // trbl
    buttonBox.getChildren ().add (hideButton);

    HBox optionsBox = new HBox (10);
    optionsBox.setAlignment (Pos.CENTER_LEFT);
    optionsBox.setPadding (new Insets (10, 10, 10, 10));         // trbl
    txtCommand.setEditable (false);
    //    txtStatus.setDisable (true);
    txtCommand.setPrefWidth (300);
    txtCommand.setFont (Font.font ("Monospaced", 12));
    txtCommand.setFocusTraversable (false);
    optionsBox.getChildren ().addAll (lblCommand, txtCommand);

    BorderPane bottomBorderPane = new BorderPane ();
    bottomBorderPane.setLeft (optionsBox);
    bottomBorderPane.setRight (buttonBox);

    hideButton.setOnAction (e -> hide ());

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

  private void select (BatchJob batchJob)
  {
    selectedBatchJob = batchJob;
    setText ();
  }

  private void setText ()
  {
    if (selectedBatchJob == null || (tsoCommandField == null && !isTSOCommandScreen))
    {
      txtCommand.setText ("");
      return;
    }

    String command =
        String.format ("OUTPUT %s PRINT(XYZ.%d)", selectedBatchJob.jobName,
                       selectedBatchJob.jobNumber);

    if (tsoCommandField != null)
      command = "TSO " + command;
    txtCommand.setText (command);
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
  public void screenChanged (boolean isTSOCommandScreen, Field tsoCommandField)
  {
    //    txtCommand.setText (isTSOCommandScreen ? "TSO Command Screen"
    //        : tsoCommandField == null ? "TSO Unavailable" : "TSO Command Field");
    this.isTSOCommandScreen = isTSOCommandScreen;
    this.tsoCommandField = tsoCommandField;
    setText ();
  }

  // ---------------------------------------------------------------------------------//
  // Batch jobs
  // ---------------------------------------------------------------------------------//

  public void batchJobSubmitted (int jobNumber, String jobName)
  {
    BatchJob batchJob = new BatchJob (jobNumber, jobName);
    addBatchJob (batchJob);
  }

  public void
      batchJobEnded (int jobNumber, String jobName, String time, int conditionCode)
  {
    BatchJob batchJob = getBatchJob (jobNumber);
    if (batchJob != null)
    {
      batchJob.completed (time, conditionCode);
      jobTable.refresh ();            // temp fix before jdk 8u60
    }
  }
}