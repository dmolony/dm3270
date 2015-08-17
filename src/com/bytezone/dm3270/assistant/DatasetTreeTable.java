package com.bytezone.dm3270.assistant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.util.Callback;

public class DatasetTreeTable extends TreeTableView<Dataset>
{
  private Callback<TreeTableColumn<Dataset, String>, //
  TreeTableCell<Dataset, String>> centreJustified;
  private Callback<TreeTableColumn<Dataset, String>, //
  TreeTableCell<Dataset, String>> rightJustified;

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

    createJustifications ();

    addColumn ("DatasetName", "Dataset name", 300, Justification.LEFT);
    addColumn ("Tracks", "Tracks", 50, Justification.RIGHT);
    addColumn ("PercentUsed", "% used", 50, Justification.RIGHT);
    addColumn ("Extents", "XT", 50, Justification.RIGHT);
    addColumn ("Device", "Device", 50, Justification.CENTER);
    addColumn ("Volume", "Volume", 70, Justification.LEFT);
    addColumn ("Dsorg", "Dsorg", 50, Justification.LEFT);
    addColumn ("Recfm", "Recfm", 50, Justification.LEFT);
    addColumn ("Lrecl", "Lrecl", 50, Justification.RIGHT);
    addColumn ("Blksize", "Blksize", 70, Justification.RIGHT);
    addColumn ("Created", "Created", 100, Justification.CENTER);
    addColumn ("Expires", "Expires", 100, Justification.CENTER);
    addColumn ("Referred", "Referred", 100, Justification.CENTER);
    addColumn ("Catalog", "Catalog", 150, Justification.LEFT);

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
      }
      else
        root.getChildren ().add (datasetEntry.treeItem);
    }
    else
      datasetEntry.dataset.merge (dataset);
  }

  private void addColumn (String id, String heading, int width,
      Justification justification)
  {
    TreeTableColumn<Dataset, String> column = new TreeTableColumn<> (heading);
    column.setPrefWidth (width);
    column.setCellValueFactory (new TreeItemPropertyValueFactory<> (id));
    getColumns ().add (column);

    if (justification == Justification.CENTER)
      column.setCellFactory (centreJustified);
    else if (justification == Justification.RIGHT)
      column.setCellFactory (rightJustified);
  }

  private void createJustifications ()
  {
    centreJustified =
        new Callback<TreeTableColumn<Dataset, String>, TreeTableCell<Dataset, String>> ()
        {
          @Override
          public TreeTableCell<Dataset, String> call (TreeTableColumn<Dataset, String> p)
          {
            TreeTableCell<Dataset, String> cell = new TreeTableCell<Dataset, String> ()
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
        new Callback<TreeTableColumn<Dataset, String>, TreeTableCell<Dataset, String>> ()
        {
          @Override
          public TreeTableCell<Dataset, String> call (TreeTableColumn<Dataset, String> p)
          {
            TreeTableCell<Dataset, String> cell = new TreeTableCell<Dataset, String> ()
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

  // this is a workaround until jdk 8u60 is released
  public void refresh ()
  {
    //    setRoot (null);
    //    setRoot (root);
    setShowRoot (true);
    setShowRoot (false);
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