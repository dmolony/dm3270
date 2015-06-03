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
  private final Label lblStatus = new Label ("TSO Status");
  private final TextField txtStatus = new TextField ();

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
    txtStatus.setEditable (false);
    optionsBox.getChildren ().addAll (lblStatus, txtStatus);

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
  }

  public void addBatchJob (BatchJob batchJob)
  {
    jobTable.addJob (batchJob);
  }

  public BatchJob getBatchJob (int jobNumber)
  {
    return jobTable.getBatchJob (jobNumber);
  }

  public void refreshJobTable ()
  {
    jobTable.refresh ();
  }

  private void closeWindow ()
  {
    windowSaver.saveWindow ();
    hide ();
  }

  @Override
  public void screenChanged (boolean isTSOCommandScreen, Field tsoCommandField)
  {
    txtStatus.setText (isTSOCommandScreen ? "TSO Command Screen"
        : tsoCommandField == null ? "TSO Unavailable" : "TSO Command Field");
  }
}