package com.bytezone.dm3270.assistant;

import java.util.List;

import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.ScreenDetails;
import com.bytezone.dm3270.display.TSOCommandStatusListener;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class DatasetTab extends TransferTab implements TSOCommandStatusListener
{
  private final DatasetTable datasetTable = new DatasetTable ();

  private boolean isTSOCommandScreen;
  private Field tsoCommandField;
  private Dataset selectedDataset;
  private ScreenDetails screenDetails;

  public DatasetTab (TextField text, Button execute)
  {
    super ("Datasets", text, execute);

    //    setTitle ("Datasets");

    //    HBox buttonBox = new HBox ();
    //    btnHide.setPrefWidth (120);
    //    buttonBox.setAlignment (Pos.CENTER_RIGHT);
    //    buttonBox.setPadding (new Insets (10, 10, 10, 10));// trbl
    //    buttonBox.getChildren ().add (btnHide);

    //    HBox optionsBox = new HBox (10);
    //    optionsBox.setAlignment (Pos.CENTER_LEFT);
    //    optionsBox.setPadding (new Insets (10, 10, 10, 10));// trbl
    //    txtCommand.setEditable (false);
    //    txtCommand.setPrefWidth (320);
    //    txtCommand.setFont (Font.font ("Monospaced", 12));
    //    txtCommand.setFocusTraversable (false);
    //    optionsBox.getChildren ().addAll (lblCommand, txtCommand, btnExecute);

    //    BorderPane bottomBorderPane = new BorderPane ();
    //    bottomBorderPane.setLeft (optionsBox);
    //    bottomBorderPane.setRight (buttonBox);

    //    btnHide.setOnAction (e -> closeWindow ());
    //    btnExecute.setOnAction (e -> execute ());

    datasetTable.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, newSelection) -> {
          if (newSelection != null)
            select (newSelection);
        });

    setContent (datasetTable);

    //    BorderPane borderPane = new BorderPane ();
    //    borderPane.setCenter (datasetTable);
    //    borderPane.setBottom (bottomBorderPane);

    //    setOnCloseRequest (e -> closeWindow ());

    //    Scene scene = new Scene (borderPane, 800, 500);// width/height
    //    setScene (scene);

    //    windowSaver = new WindowSaver (prefs, this, "DatasetStage");
    //    windowSaver.restoreWindow ();
  }

  private void select (Dataset dataset)
  {
    selectedDataset = dataset;
    setText ();
  }

  @Override
      void setText ()
  {
    String datasetName = selectedDataset == null ? "" : selectedDataset.getDatasetName ();
    if (datasetName == null || datasetName.isEmpty ())
    {
      txtCommand.setText ("");
      return;
    }

    String prefix = screenDetails == null ? "" : screenDetails.getPrefix ();
    if (!prefix.isEmpty () && datasetName.startsWith (prefix))
    {
      if (datasetName.length () == prefix.length ())
      {
        txtCommand.setText ("");
        return;
      }
      datasetName = datasetName.substring (prefix.length () + 1);
    }
    else
      datasetName = "'" + datasetName + "'";

    String command = String.format ("IND$FILE GET %s", datasetName);

    if (!isTSOCommandScreen)
      command = "TSO " + command;

    txtCommand.setText (command);
    setButton ();
  }

  @Override
      void setButton ()
  {
    if (selectedDataset == null)
    {
      btnExecute.setDisable (true);
      return;
    }

    String command = txtCommand.getText ();
    btnExecute.setDisable (tsoCommandField == null || command.isEmpty ());
  }

  //  private void execute ()
  //  {
  //    if (tsoCommandField != null) // are we on a suitable screen?
  //    {
  //      tsoCommandField.setText (txtCommand.getText ());
  //      if (consolePane != null)
  //        consolePane.sendAID (AIDCommand.AID_ENTER, "ENTR");
  //    }
  //  }

  //  private void closeWindow ()
  //  {
  //    windowSaver.saveWindow ();
  //    hide ();
  //  }

  @Override
  public void screenChanged (ScreenDetails screenDetails)
  {
    this.screenDetails = screenDetails;
    this.isTSOCommandScreen = screenDetails.isTSOCommandScreen ();
    this.tsoCommandField = screenDetails.getTSOCommandField ();

    List<Dataset> datasets = screenDetails.getDatasets ();
    if (datasets != null)
      for (Dataset dataset : datasets)
        datasetTable.addDataset (dataset);

    List<Dataset> members = screenDetails.getMembers ();
    if (members != null)
      for (Dataset dataset : members)
        datasetTable.addMember (dataset);

    datasetTable.refresh ();// temporary fix until 8u60
    setButton ();
  }
}