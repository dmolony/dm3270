package com.bytezone.dm3270.assistant;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;

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

    addColumn ("Dataset name", 300, Justification.LEFT,
               e -> e.getValue ().propertyDatasetName ());
    addColumn ("Volume", 70, Justification.LEFT, e -> e.getValue ().propertyVolume ());
    addIntegerColumn ("Tracks", 50, e -> e.getValue ().propertyTracks ());
    addIntegerColumn ("% used", 50, e -> e.getValue ().propertyPercentUsed ());
    addIntegerColumn ("XT", 50, e -> e.getValue ().propertyExtents ());
    addColumn ("Device", 50, Justification.CENTER, e -> e.getValue ().propertyDevice ());
    addColumn ("Dsorg", 50, Justification.LEFT, e -> e.getValue ().propertyDsorg ());
    addColumn ("Recfm", 50, Justification.LEFT, e -> e.getValue ().propertyRecfm ());
    addIntegerColumn ("Lrecl", 50, e -> e.getValue ().propertyLrecl ());
    addIntegerColumn ("Blksize", 70, e -> e.getValue ().propertyBlksize ());
    addColumn ("Created", 100, Justification.CENTER,
               e -> e.getValue ().propertyCreated ());
    addColumn ("Expires", 100, Justification.CENTER,
               e -> e.getValue ().propertyExpires ());
    addColumn ("Referred", 100, Justification.CENTER,
               e -> e.getValue ().propertyReferred ());
    addColumn ("Catalog", 150, Justification.LEFT, e -> e.getValue ().propertyCatalog ());

    setPlaceholder (new Label ("No datasets have been seen in this session"));

    setItems (datasets);
  }

  private void addColumn (String heading, int width, Justification justification,
      Callback<CellDataFeatures<Dataset, String>, ObservableValue<String>> callback)
  {
    TableColumn<Dataset, String> column = new TableColumn<> (heading);
    column.setPrefWidth (width);
    column.setCellValueFactory (callback);
    getColumns ().add (column);

    if (justification == Justification.CENTER)
      column.setStyle ("-fx-alignment: CENTER;");
  }

  private void addIntegerColumn (String heading, int width,
      Callback<CellDataFeatures<Dataset, Number>, ObservableValue<Number>> callback)
  {
    TableColumn<Dataset, Number> column = new TableColumn<> (heading);
    column.setPrefWidth (width);
    column.setCellValueFactory (callback);
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