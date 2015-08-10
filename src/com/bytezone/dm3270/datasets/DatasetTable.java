package com.bytezone.dm3270.datasets;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class DatasetTable extends TableView<Dataset>
{
  private final ObservableList<Dataset> datasets = FXCollections.observableArrayList ();
  Callback<TableColumn<Dataset, String>, TableCell<Dataset, String>> centreJustified;
  Callback<TableColumn<Dataset, String>, TableCell<Dataset, String>> rightJustified;

  enum Justification
  {
    LEFT, CENTER, RIGHT
  }

  public DatasetTable ()
  {
    //    setStyle ("-fx-font-size: 11;");
    setStyle ("-fx-font-size: 11; -fx-font-family: Monospaced");
    setFixedCellSize (20.0);

    createJustifications ();

    addColumn ("DatasetName", "Dataset name", 300, Justification.LEFT);
    addColumn ("Tracks", "Tracks", 50, Justification.RIGHT);
    addColumn ("Cylinders", "Cyls", 50, Justification.RIGHT);
    addColumn ("PercentUsed", "% used", 50, Justification.RIGHT);
    addColumn ("Extents", "XT", 50, Justification.RIGHT);
    addColumn ("Device", "Device", 50, Justification.CENTER);
    addColumn ("Volume", "Volume", 70, Justification.LEFT);
    addColumn ("Dsorg", "Dsorg", 50, Justification.LEFT);
    addColumn ("Recfm", "Recfm", 50, Justification.LEFT);
    addColumn ("Lrecl", "Lrecl", 50, Justification.RIGHT);
    addColumn ("Blksize", "Blksize", 70, Justification.RIGHT);

    setItems (datasets);
  }

  private void addColumn (String id, String heading, int width,
      Justification justification)
  {
    TableColumn<Dataset, String> column = new TableColumn<> (heading);
    column.setPrefWidth (width);
    column.setCellValueFactory (new PropertyValueFactory<> (id));
    getColumns ().add (column);

    if (justification == Justification.CENTER)
      column.setCellFactory (centreJustified);
    else if (justification == Justification.RIGHT)
      column.setCellFactory (rightJustified);
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

  private void createJustifications ()
  {
    centreJustified =
        new Callback<TableColumn<Dataset, String>, TableCell<Dataset, String>> ()
        {
          @Override
          public TableCell<Dataset, String> call (TableColumn<Dataset, String> p)
          {
            TableCell<Dataset, String> cell = new TableCell<Dataset, String> ()
            {
              @Override
              public void updateItem (String item, boolean empty)
              {
                super.updateItem (item, empty);
                setText (empty ? null : getItem () == null ? "" : getItem ().toString ());
                setGraphic (null);
              }
            };

            cell.setStyle ("-fx-alignment: center;");
            return cell;
          }
        };

    rightJustified =
        new Callback<TableColumn<Dataset, String>, TableCell<Dataset, String>> ()
        {
          @Override
          public TableCell<Dataset, String> call (TableColumn<Dataset, String> p)
          {
            TableCell<Dataset, String> cell = new TableCell<Dataset, String> ()
            {
              @Override
              public void updateItem (String item, boolean empty)
              {
                super.updateItem (item, empty);
                setText (empty ? null : getItem () == null ? "" : getItem ().toString ());
                setGraphic (null);
              }
            };

            cell.setStyle ("-fx-alignment: center-right;");
            return cell;
          }
        };
  }
}