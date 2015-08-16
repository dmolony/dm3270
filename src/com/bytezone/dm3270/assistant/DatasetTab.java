package com.bytezone.dm3270.assistant;

import java.util.List;

import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenDetails;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class DatasetTab extends TransferTab implements ScreenChangeListener
{
  private final DatasetTable datasetTable = new DatasetTable ();
  private final Screen screen;

  private boolean isTSOCommandScreen;
  private Field tsoCommandField;
  private Dataset selectedDataset;
  //  private ScreenDetails screenDetails;

  public DatasetTab (Screen screen, TextField text, Button execute)
  {
    super ("Datasets", text, execute);

    this.screen = screen;

    datasetTable.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, newSelection) -> {
          if (newSelection != null)
            select (newSelection);
        });

    setContent (datasetTable);
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

    ScreenDetails screenDetails = screen.getScreenDetails ();
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

  @Override
  public void screenChanged ()
  {
    //    this.screenDetails = screenDetails;
    ScreenDetails screenDetails = screen.getScreenDetails ();
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