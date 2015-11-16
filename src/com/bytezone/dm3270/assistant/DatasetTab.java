package com.bytezone.dm3270.assistant;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.application.Site;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenDetails;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;

public class DatasetTab extends AbstractTransferTab
{
  private static final Pattern pattern = Pattern.compile (".*\\.(CNTL|JCL)[.(].*\\)");
  private Dataset selectedDataset;

  private final boolean useTable = false;
  private final DatasetTable datasetTable = new DatasetTable ();
  private final DatasetTreeTable datasetTreeTable = new DatasetTreeTable ();

  public DatasetTab (Screen screen, Site site, TextField text, Button execute)
  {
    super ("Datasets", screen, site, text, execute);

    if (useTable)
    {
      ReadOnlyObjectProperty<Dataset> property =
          datasetTable.getSelectionModel ().selectedItemProperty ();
      property.addListener ( (obs, oldSelection, newSelection) -> select (newSelection));
      setContent (datasetTable);
    }
    else
    {
      ReadOnlyObjectProperty<TreeItem<Dataset>> property =
          datasetTreeTable.getSelectionModel ().selectedItemProperty ();
      property.addListener ( (obs, oldSelection, newSelection) -> select (newSelection));
      setContent (datasetTreeTable);
    }
  }

  private void select (Dataset dataset)
  {
    if (dataset == null)
      return;
    selectedDataset = dataset;
    setText ();
    fireDatasetSelected (selectedDataset);
  }

  private void select (TreeItem<Dataset> treeItem)
  {
    if (treeItem == null)
      return;
    selectedDataset = treeItem.getValue ();
    setText ();
    fireDatasetSelected (selectedDataset);
  }

  @Override
  public void screenChanged (ScreenDetails screenDetails)
  {
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

    super.screenChanged (screenDetails);
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
    String prefix = screenDetails == null ? "" : screenDetails.getPrefix ();

    Matcher matcher = pattern.matcher (datasetName);
    boolean jclMember = matcher.matches ();

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
    setButton ();
  }

  // ---------------------------------------------------------------------------------//
  // DatasetSelectionListener
  // ---------------------------------------------------------------------------------//

  private final Set<DatasetSelectionListener> selectionListeners = new HashSet<> ();

  void fireDatasetSelected (Dataset dataset)
  {
    selectionListeners.forEach (l -> l.datasetSelected (dataset));
  }

  void addDatasetSelectionListener (DatasetSelectionListener listener)
  {
    if (!selectionListeners.contains (listener))
      selectionListeners.add (listener);
  }

  void removeDatasetSelectionListener (DatasetSelectionListener listener)
  {
    if (selectionListeners.contains (listener))
      selectionListeners.remove (listener);
  }
}