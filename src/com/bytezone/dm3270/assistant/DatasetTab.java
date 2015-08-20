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

    if (isSelected ())
      setText ();
  }

  @Override
      void setText ()
  {
    ScreenDetails screenDetails = screen.getScreenDetails ();
    if (selectedDataset == null || screenDetails.getTSOCommandField () == null)
    {
      txtCommand.setText ("");
      btnExecute.setDisable (true);
      return;
    }

    String datasetName = selectedDataset.getDatasetName ();
    String prefix = screenDetails == null ? "" : screenDetails.getPrefix ();
    assert prefix != null;
    if (!prefix.isEmpty () && datasetName.startsWith (prefix))
    {
      if (datasetName.length () == prefix.length ())
      {
        txtCommand.setText ("");
        btnExecute.setDisable (true);
        return;
      }
      datasetName = datasetName.substring (prefix.length () + 1);
    }
    else
      datasetName = "'" + datasetName + "'";

    String tsoPrefix = screenDetails.isTSOCommandScreen () ? "" : "TSO ";
    String command = String.format ("%sIND$FILE GET %s", tsoPrefix, datasetName);

    txtCommand.setText (command);
    btnExecute.setDisable (false);
  }
}