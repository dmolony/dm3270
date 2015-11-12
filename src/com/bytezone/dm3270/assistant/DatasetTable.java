package com.bytezone.dm3270.assistant;

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
    addColumnNumber ("Tracks", 50, "tracksProperty");
    addColumnNumber ("% used", 50, "percentUsedProperty");
    addColumnNumber ("XT", 50, "extentsProperty");
    addColumnString ("Device", 50, Justification.CENTER, "deviceProperty");
    addColumnString ("Dsorg", 50, Justification.LEFT, "dsorgProperty");
    addColumnString ("Recfm", 50, Justification.LEFT, "recfmProperty");
    addColumnNumber ("Lrecl", 50, "lreclProperty");
    addColumnNumber ("Blksize", 70, "blksizeProperty");
    addColumnString ("Created", 100, Justification.CENTER, "createdProperty");
    addColumnString ("Expires", 100, Justification.CENTER, "expiresProperty");
    addColumnString ("Referred", 100, Justification.CENTER, "referredProperty");
    addColumnString ("Catalog", 150, Justification.LEFT, "catalogProperty");

    setPlaceholder (new Label ("No datasets have been seen in this session"));

    setItems (datasets);
  }

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
    Dataset foundDataset = null;
    String datasetName = dataset.getDatasetName ();
    for (Dataset existingDataset : datasets)
      if (existingDataset.getDatasetName ().equals (datasetName))
      {
        foundDataset = existingDataset;
        break;
      }

    if (foundDataset == null)
      datasets.add (dataset);
    else
      foundDataset.merge (dataset);
  }

  public void addMember (Dataset member)
  {
    Dataset foundDataset = null;
    String memberName = member.getDatasetName ();
    int pos = memberName.indexOf ('(');
    String parentName = memberName.substring (0, pos);
    String childName = memberName.substring (pos + 1, memberName.length () - 1);

    for (Dataset existingDataset : datasets)
      if (existingDataset.getDatasetName ().equals (memberName))
      {
        foundDataset = existingDataset;
        break;
      }

    if (foundDataset == null)
    {
      datasets.add (member);
      //      System.out.println ("parent not found");
    }
    else
    {
      //      foundDataset.merge (member);
      //      foundDataset.getChildren ().add (childName);
      //      System.out.println ("parent found");
      //      refresh ();
    }
  }
}