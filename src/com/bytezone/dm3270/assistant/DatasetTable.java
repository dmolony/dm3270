package com.bytezone.dm3270.assistant;

import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class DatasetTable extends TableView<Dataset>
{
  private final ObservableList<Dataset> datasets = FXCollections.observableArrayList ();

  enum Justification
  {
    LEFT, CENTER, RIGHT
  }

  public DatasetTable ()
  {
    setStyle ("-fx-font-size: 12; -fx-font-family: Monospaced");
    setFixedCellSize (20.0);

    addColumnString ("Dataset name", 300, Justification.LEFT, "datasetName");
    addColumnString ("Volume", 70, Justification.LEFT, "volume");
    addColumnNumber ("Tracks", 50, "tracks");
    addColumnNumber ("% used", 50, "percentUsed");
    addColumnNumber ("XT", 50, "extents");
    addColumnString ("Device", 50, Justification.CENTER, "device");
    addColumnString ("Dsorg", 50, Justification.LEFT, "dsorg");
    addColumnString ("Recfm", 50, Justification.LEFT, "recfm");
    addColumnNumber ("Lrecl", 50, "lrecl");
    addColumnNumber ("Blksize", 70, "blksize");
    addColumnString ("Created", 100, Justification.CENTER, "created");
    addColumnString ("Expires", 100, Justification.CENTER, "expires");
    addColumnString ("Referred", 100, Justification.CENTER, "referred");
    addColumnString ("Catalog", 150, Justification.LEFT, "catalog");

    setPlaceholder (new Label ("No datasets have been seen in this session"));

    setItems (datasets);
  }

  // NB propertyName must have a corresponding method xxxProperty in Dataset
  private void addColumnString (String heading, int width, Justification justification,
      String propertyName)
  {
    TableColumn<Dataset, String> column = new TableColumn<> (heading);
    column.setPrefWidth (width);
    column.setCellValueFactory (new PropertyValueFactory<Dataset, String> (propertyName));
    getColumns ().add (column);

    if (justification == Justification.CENTER)
      column.setStyle ("-fx-alignment: CENTER;");
  }

  private void addColumnNumber (String heading, int width, String propertyName)
  {
    TableColumn<Dataset, Number> column = new TableColumn<> (heading);
    column.setPrefWidth (width);
    column.setCellValueFactory (new PropertyValueFactory<Dataset, Number> (propertyName));
    getColumns ().add (column);
    column.setStyle ("-fx-alignment: CENTER-RIGHT;");
  }

  public void addDataset (Dataset dataset)
  {
    String datasetName = dataset.getDatasetName ();

    Optional<Dataset> dataset2 = datasets.stream ()
        .filter (d -> d.getDatasetName ().equals (datasetName)).findAny ();
    if (dataset2.isPresent ())
      dataset2.get ().merge (dataset);
    else
      datasets.add (dataset);
  }

  public void addMember (Dataset member)
  {
    Dataset foundDataset = null;
    String memberName = member.getDatasetName ();

    for (Dataset existingDataset : datasets)
      if (existingDataset.getDatasetName ().equals (memberName))
      {
        foundDataset = existingDataset;
        break;
      }

    if (foundDataset == null)
      datasets.add (member);
    else
      foundDataset.merge (member);
  }
}