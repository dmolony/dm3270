package com.bytezone.dm3270.assistant;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenWatcher;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.TreeItem;

public class DatasetTab extends AbstractTransferTab
{
  private static final Pattern jclPattern = Pattern.compile (".*\\.(CNTL|JCL)[.(].*\\)");
  private static final Pattern procPattern =
      Pattern.compile (".*\\.(PROC|PARM)LIB[.(].*\\)");
  private Dataset selectedDataset;

  private final boolean useTable = false;
  private final DatasetTable datasetTable = new DatasetTable ();
  private final DatasetTreeTable datasetTreeTable = new DatasetTreeTable ();

  public DatasetTab (Screen screen, TSOCommand tsoCommand)
  {
    super ("Datasets", screen, tsoCommand);

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
  public void screenChanged (ScreenWatcher screenDetails)
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
    String prefix = screenWatcher == null ? "" : screenWatcher.getPrefix ();

    Matcher matcher1 = jclPattern.matcher (datasetName);
    Matcher matcher2 = procPattern.matcher (datasetName);
    boolean useCrlf = matcher1.matches () || matcher2.matches ();

    if (!prefix.isEmpty () && datasetName.startsWith (prefix))
    {
      if (datasetName.length () == prefix.length ())
      {
        tsoCommand.txtCommand.setText ("");
        tsoCommand.btnExecute.setDisable (true);
        return;
      }
      datasetName = datasetName.substring (prefix.length () + 1);
    }
    else
      datasetName = "'" + datasetName + "'";

    String tsoPrefix = screenWatcher.isTSOCommandScreen () ? "" : "TSO ";
    String options = useCrlf ? " ASCII CRLF" : "";

    String command =
        String.format ("%sIND$FILE GET %s%s", tsoPrefix, datasetName, options);
    tsoCommand.txtCommand.setText (command);
    setButton ();
  }

  // ---------------------------------------------------------------------------------//
  // ISPF Dataset Commands
  // ---------------------------------------------------------------------------------//

  // B - Browse data set        C - Catalog data set        F - Free unused space
  // E - Edit data set          U - Uncatalog data set      = - Repeat last command
  // D - Delete data set        P - Print entire data set
  // R - Rename data set        X - Exclude dataset
  // I - Data set information   M - Display member list
  // S - Information (short)    Z - Compress data set       TSO command or CLIST

  // SAVE LIST --> HLQ.LIST.DATASETS or HLQ.LIST.MEMEBERS

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