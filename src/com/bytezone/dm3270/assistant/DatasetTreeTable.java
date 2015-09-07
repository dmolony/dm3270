package com.bytezone.dm3270.assistant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;

public class DatasetTreeTable extends TreeTableView<Dataset>
{
  private final TreeItem<Dataset> root = new TreeItem<> (new Dataset ("Root"));
  private final Map<String, DatasetEntry> entries = new HashMap<> ();

  enum Justification
  {
    LEFT, CENTER, RIGHT
  }

  public DatasetTreeTable ()
  {
    setRoot (root);
    setShowRoot (false);

    setStyle ("-fx-font-size: 12; -fx-font-family: Monospaced");
    setFixedCellSize (20.0);

    addColumn ("Dataset name", 300, Justification.LEFT,
               e -> e.getValue ().getValue ().propertyDatasetName ());
    addColumn ("Volume", 70, Justification.LEFT,
               e -> e.getValue ().getValue ().propertyVolume ());
    addIntegerColumn ("Tracks", 50, e -> e.getValue ().getValue ().propertyTracks ());
    addIntegerColumn ("% used", 50,
                      e -> e.getValue ().getValue ().propertyPercentUsed ());
    addIntegerColumn ("XT", 50, e -> e.getValue ().getValue ().propertyExtents ());
    addColumn ("Device", 50, Justification.CENTER,
               e -> e.getValue ().getValue ().propertyDevice ());
    addColumn ("Dsorg", 50, Justification.LEFT,
               e -> e.getValue ().getValue ().propertyDsorg ());
    addColumn ("Recfm", 50, Justification.LEFT,
               e -> e.getValue ().getValue ().propertyRecfm ());
    addIntegerColumn ("Lrecl", 50, e -> e.getValue ().getValue ().propertyLrecl ());
    addIntegerColumn ("Blksize", 70, e -> e.getValue ().getValue ().propertyBlksize ());
    addColumn ("Created", 100, Justification.CENTER,
               e -> e.getValue ().getValue ().propertyCreated ());
    addColumn ("Expires", 100, Justification.CENTER,
               e -> e.getValue ().getValue ().propertyExpires ());
    addColumn ("Referred", 100, Justification.CENTER,
               e -> e.getValue ().getValue ().propertyReferred ());
    addColumn ("Catalog/ID", 150, Justification.LEFT,
               e -> e.getValue ().getValue ().propertyCatalog ());

    setPlaceholder (new Label ("No datasets have been seen in this session"));
  }

  public void addDataset (Dataset dataset)
  {
    DatasetEntry datasetEntry = entries.get (dataset.getDatasetName ());
    if (datasetEntry == null)
    {
      datasetEntry = new DatasetEntry (dataset);
      entries.put (dataset.getDatasetName (), datasetEntry);
      if (datasetEntry.isPDSMember ())
      {
        DatasetEntry parentEntry = entries.get (datasetEntry.parentName);
        if (parentEntry == null)
        {
          parentEntry = new DatasetEntry (datasetEntry.parentName);
          entries.put (datasetEntry.parentName, parentEntry);
          root.getChildren ().add (parentEntry.treeItem);
        }

        parentEntry.add (dataset);
        parentEntry.treeItem.getChildren ().add (datasetEntry.treeItem);

        // would be good to open the parent
      }
      else
        root.getChildren ().add (datasetEntry.treeItem);
    }
    else
      datasetEntry.dataset.merge (dataset);
  }

  private void addColumn (String heading, int width, Justification justification,
      Callback<CellDataFeatures<Dataset, String>, ObservableValue<String>> callback)
  {
    TreeTableColumn<Dataset, String> column = new TreeTableColumn<> (heading);
    column.setPrefWidth (width);
    column.setCellValueFactory (callback);
    getColumns ().add (column);

    if (justification == Justification.CENTER)
      column.setStyle ("-fx-alignment: CENTER;");
  }

  private void addIntegerColumn (String heading, int width,
      Callback<CellDataFeatures<Dataset, Number>, ObservableValue<Number>> callback)
  {
    TreeTableColumn<Dataset, Number> column = new TreeTableColumn<> (heading);
    column.setPrefWidth (width);
    column.setCellValueFactory (callback);
    getColumns ().add (column);
    column.setStyle ("-fx-alignment: CENTER-RIGHT;");
  }

  class DatasetEntry
  {
    Dataset dataset;
    TreeItem<Dataset> treeItem;

    List<Dataset> members;// only a PDS uses this

    String parentName;// only PDS members use these 2 name fields
    String memberName;

    public DatasetEntry (Dataset dataset)
    {
      // check whether this is a PDS member
      String name = dataset.getDatasetName ();
      int pos = name.indexOf ('(');
      if (pos > 0)
      {
        parentName = name.substring (0, pos);
        memberName = name.substring (pos + 1, name.length () - 1);
      }

      this.dataset = dataset;
      treeItem = new TreeItem<> (dataset);
    }

    // only used when we have a PDS member with no parent
    public DatasetEntry (String name)
    {
      dataset = new Dataset (name);
      parentName = name;
      members = new ArrayList<> ();
      treeItem = new TreeItem<> (dataset);
    }

    public void add (Dataset pdsMember)
    {
      if (members == null)
        members = new ArrayList<> ();
      members.add (pdsMember);
    }

    public boolean isFlatFile ()
    {
      return parentName == null && members == null;
    }

    public boolean isPDS ()
    {
      return members != null;
    }

    public boolean isPDSMember ()
    {
      return parentName != null;
    }
  }
}