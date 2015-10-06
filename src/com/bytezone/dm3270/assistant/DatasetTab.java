package com.bytezone.dm3270.assistant;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenDetails;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;

public class DatasetTab extends AbstractTransferTab
{
  private Dataset selectedDataset;

  private final boolean useTable = false;
  private final DatasetTable datasetTable = new DatasetTable ();
  private final DatasetTreeTable datasetTreeTable = new DatasetTreeTable ();
  private ScreenDetails screenDetails;

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
    fireDatasetSelected (selectedDataset);
  }

  @Override
  public void screenChanged (ScreenDetails screenDetails)
  {
    this.screenDetails = screenDetails;
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
    {
      setText ();
      setButton ();
    }
  }

  @Override
  public void keyboardStatusChanged (KeyboardStatusChangedEvent evt)
  {
    if (isSelected ())
      setButton ();
  }

  @Override
  protected void setText ()
  {
    if (selectedDataset == null)
    {
      eraseCommand ();
      return;
    }

    String datasetName = selectedDataset.getDatasetName ();
    int pos1 = datasetName.indexOf (".CNTL");
    int pos2 = datasetName.indexOf (".JCL");
    boolean jclMember = (pos1 > 0 || pos2 > 0) && datasetName.endsWith (")");
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

    String tsoPrefix = screenDetails.isTSOCommandScreen () ? "" : "TSO ";
    String options = jclMember ? " ASCII CRLF" : "";

    String command =
        String.format ("%sIND$FILE GET %s%s", tsoPrefix, datasetName, options);
    txtCommand.setText (command);
  }

  @Override
  protected void setButton ()
  {
    btnExecute.setDisable (screen.isKeyboardLocked ()
        || screenDetails.getTSOCommandField () == null);
  }

  private final Set<DatasetSelectionListener> selectionListeners = new HashSet<> ();

  void fireDatasetSelected (Dataset dataset)
  {
    for (DatasetSelectionListener listener : selectionListeners)
      listener.datasetSelected (dataset);
  }

  void addDatasetSelectionListener (DatasetSelectionListener listener)
  {
    selectionListeners.add (listener);
  }

  void removeDatasetSelectionListener (DatasetSelectionListener listener)
  {
    selectionListeners.remove (listener);
  }
}