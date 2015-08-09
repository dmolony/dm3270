package com.bytezone.dm3270.datasets;

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

    TableColumn<Dataset, String> colDatasetName = new TableColumn<> ("Dataset name");
    colDatasetName.setPrefWidth (100);
    colDatasetName.setCellValueFactory (new PropertyValueFactory<> ("DatasetName"));
    getColumns ().add (colDatasetName);

    setItems (datasets);
  }
}