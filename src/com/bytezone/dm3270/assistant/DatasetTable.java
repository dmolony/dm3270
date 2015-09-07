package com.bytezone.dm3270.assistant;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
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
    setStyle ("-fx-font-size: 12; -fx-font-family: Monospaced");
    setFixedCellSize (20.0);

    //    createJustifications ();

    addColumn ("Dataset name", 300, Justification.LEFT,
               e -> e.getValue ().propertyDatasetName ());
    addColumn ("Volume", 70, Justification.LEFT, e -> e.getValue ().propertyVolume ());
    addColumn ("Tracks", 50, Justification.RIGHT, e -> e.getValue ().propertyTracks ());
    addColumn ("% used", 50, Justification.RIGHT,
               e -> e.getValue ().propertyPercentUsed ());
    addColumn ("XT", 50, Justification.RIGHT, e -> e.getValue ().propertyExtents ());
    addColumn ("Device", 50, Justification.CENTER, e -> e.getValue ().propertyDevice ());
    addColumn ("Dsorg", 50, Justification.LEFT, e -> e.getValue ().propertyDsorg ());
    addColumn ("Recfm", 50, Justification.LEFT, e -> e.getValue ().propertyRecfm ());
    addColumn ("Lrecl", 50, Justification.RIGHT, e -> e.getValue ().propertyLrecl ());
    addColumn ("Blksize", 70, Justification.RIGHT, e -> e.getValue ().propertyBlksize ());
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

  private TableColumn<Dataset, String> addColumn (String heading, int width,
      Justification justification,
      Callback<CellDataFeatures<Dataset, String>, ObservableValue<String>> callback)
  {
    TableColumn<Dataset, String> column = new TableColumn<> (heading);
    column.setPrefWidth (width);
    column.setCellValueFactory (callback);
    getColumns ().add (column);

    if (justification == Justification.CENTER)
      //      column.setCellFactory (centreJustified);
      column.setStyle ("-fx-alignment: CENTER;");
    else
      if (justification == Justification.RIGHT)
        //      column.setCellFactory (rightJustified);
        column.setStyle ("-fx-alignment: CENTER-RIGHT;");

    return column;
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

    //    System.out.println (parentName + "--" + childName);

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

  //  private void createJustifications ()
  //  {
  //    centreJustified =
  //        new Callback<TableColumn<Dataset, String>, TableCell<Dataset, String>> ()
  //        {
  //          @Override
  //          public TableCell<Dataset, String> call (TableColumn<Dataset, String> p)
  //          {
  //            TableCell<Dataset, String> cell = new TableCell<Dataset, String> ()
  //            {
  //              @Override
  //              public void updateItem (String item, boolean empty)
  //              {
  //                super.updateItem (item, empty);
  //                setText (empty ? null : getItem () == null ? "" : getItem ().toString ());
  //                setGraphic (null);
  //              }
  //            };
  //
  //            cell.setStyle ("-fx-alignment: center;");
  //            return cell;
  //          }
  //        };
  //
  //    rightJustified =
  //        new Callback<TableColumn<Dataset, String>, TableCell<Dataset, String>> ()
  //        {
  //          @Override
  //          public TableCell<Dataset, String> call (TableColumn<Dataset, String> p)
  //          {
  //            TableCell<Dataset, String> cell = new TableCell<Dataset, String> ()
  //            {
  //              @Override
  //              public void updateItem (String item, boolean empty)
  //              {
  //                super.updateItem (item, empty);
  //                setText (empty ? null : getItem () == null ? "" : getItem ().toString ());
  //                setGraphic (null);
  //              }
  //            };
  //
  //            cell.setStyle ("-fx-alignment: center-right;");
  //            return cell;
  //          }
  //        };
  //  }
}