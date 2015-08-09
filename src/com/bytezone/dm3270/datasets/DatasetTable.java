package com.bytezone.dm3270.datasets;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class DatasetTable extends TableView<Dataset>
{
  private final ObservableList<Dataset> datasets = FXCollections.observableArrayList ();

  public DatasetTable ()
  {
    setStyle ("-fx-font-size: 11;");
    setFixedCellSize (20.0);

    addColumn ("DatasetName", "Dataset name", 300);
    addColumn ("Tracks", "Tracks", 50);
    addColumn ("Cylinders", "Cylinders", 50);
    addColumn ("Extents", "Extents", 50);
    addColumn ("PercentUsed", "% used", 50);
    addColumn ("Volume", "Volume", 70);
    addColumn ("Extents", "XT", 50);
    addColumn ("DSORG", "DSORG", 50);
    addColumn ("RECFM", "RECFM", 50);
    addColumn ("LRECL", "LRECL", 50);
    addColumn ("BLKSIZE", "BLKSIZE", 50);

    setItems (datasets);
  }

  private void addColumn (String id, String heading, int width)
  {
    TableColumn<Dataset, String> column = new TableColumn<> (heading);
    column.setPrefWidth (width);
    column.setCellValueFactory (new PropertyValueFactory<> (id));
    getColumns ().add (column);
  }

  public void addDataset (Dataset dataset)
  {
    Dataset foundDataset = null;
    for (Dataset existingDataset : datasets)
      if (existingDataset.getDatasetName ().equals (dataset.getDatasetName ()))
      {
        foundDataset = existingDataset;
        break;
      }

    if (foundDataset == null)
      datasets.add (dataset);
    else
    {
      foundDataset.merge (dataset);
      refresh ();
    }
  }

  // this is a workaround until jdk 8u60 is released
  public void refresh ()
  {
    Dataset selectedDataset = getSelectionModel ().getSelectedItem ();
    List<Dataset> tempDatasets = new ArrayList<> ();

    tempDatasets.addAll (datasets);
    datasets.clear ();
    datasets.addAll (tempDatasets);

    if (selectedDataset != null)
      getSelectionModel ().select (selectedDataset);
    else if (datasets.size () == 1)
      getSelectionModel ().select (datasets.get (0));
  }
}