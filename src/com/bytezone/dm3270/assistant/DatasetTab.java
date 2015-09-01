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
  private Dataset selectedDataset;

  private final boolean useTable = false;
  private final DatasetTable datasetTable = new DatasetTable ();
  private final DatasetTreeTable datasetTreeTable = new DatasetTreeTable ();

  public DatasetTab (Screen screen, TextField text, Button execute)
  {
    super ("Datasets", screen, text, execute);

    if (useTable)
    {
      datasetTable.getSelectionModel ().selectedItemProperty ()
          .addListener ( (obs, oldSelection, newSelection) -> {
            if (newSelection != null)
              select (newSelection);
          });
      setContent (datasetTable);
    }
    else
    {
      datasetTreeTable.getSelectionModel ().selectedItemProperty ()
          .addListener ( (obs, oldSelection, newSelection) -> {
            if (newSelection != null)
              select (newSelection);
          });
      setContent (datasetTreeTable);
    }
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
        if (useTable)
          datasetTable.addDataset (dataset);
        else
          datasetTreeTable.addDataset (dataset);
      }

    List<Dataset> members = screenDetails.getMembers ();
    if (members != null)
      for (Dataset dataset : members)
      {
        if (useTable)
          datasetTable.addMember (dataset);
        else
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
      eraseCommand ();
      return;
    }

    String datasetName = selectedDataset.getDatasetName ();
    String prefix = screenDetails == null ? "" : screenDetails.getPrefix ();

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

    int pos = datasetName.indexOf (".CNTL");
    boolean jclMember = pos > 0 && datasetName.endsWith (")");

    String tsoPrefix = screenDetails.isTSOCommandScreen () ? "" : "TSO ";
    String options = jclMember ? " ASCII CRLF" : "";

    String command =
        String.format ("%sIND$FILE GET %s%s", tsoPrefix, datasetName, options);
    txtCommand.setText (command);
    btnExecute.setDisable (screenDetails.isKeyboardLocked ());
  }
}