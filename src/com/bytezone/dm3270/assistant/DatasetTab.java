package com.bytezone.dm3270.assistant;

import java.util.List;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenDetails;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;

public class DatasetTab extends TransferTab implements ScreenChangeListener
{
  private final DatasetTable datasetTable = new DatasetTable ();
  private final DatasetTreeTable datasetTreeTable = new DatasetTreeTable ();
  private final Screen screen;
  private Dataset selectedDataset;

  public DatasetTab (Screen screen, TextField text, Button execute)
  {
    super ("Datasets", text, execute);

    this.screen = screen;

    datasetTable.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, newSelection) -> {
          if (newSelection != null)
            select (newSelection);
        });

    datasetTreeTable.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, newSelection) -> {
          if (newSelection != null)
            select (newSelection);
        });

    setContent (datasetTreeTable);
  }

  private void select (Dataset dataset)
  {
    selectedDataset = dataset;
    setText ();
  }

  private void select (TreeItem<Dataset> treeItem)
  {
    selectedDataset = treeItem.getValue ();
    setText ();
  }

  @Override
  public void screenChanged ()
  {
    ScreenDetails screenDetails = screen.getScreenDetails ();

    List<Dataset> datasets = screenDetails.getDatasets ();
    if (datasets != null)
      for (Dataset dataset : datasets)
      {
        datasetTable.addDataset (dataset);
        datasetTreeTable.addDataset (dataset);
      }

    List<Dataset> members = screenDetails.getMembers ();
    if (members != null)
      for (Dataset dataset : members)
      {
        datasetTable.addMember (dataset);
        datasetTreeTable.addDataset (dataset);
      }

    setText ();
  }

  @Override
      void setText ()
  {
    String datasetName = selectedDataset == null ? "" : selectedDataset.getDatasetName ();
    if (datasetName == null || datasetName.isEmpty ())
    {
      txtCommand.setText ("");
      btnExecute.setDisable (true);
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

    if (!screenDetails.isTSOCommandScreen ())
      command = "TSO " + command;

    txtCommand.setText (command);

    if (selectedDataset == null)
    {
      btnExecute.setDisable (true);
      return;
    }

    //    ScreenDetails screenDetails = screen.getScreenDetails ();
    //    String command = txtCommand.getText ();
    btnExecute
        .setDisable (screenDetails.getTSOCommandField () == null || command.isEmpty ());
  }
}