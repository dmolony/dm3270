package com.bytezone.dm3270.assistant;

import java.util.HashMap;
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

  private final Map<String, Dataset> datasets = new HashMap<> ();
  private final TreeItem<Dataset> root = new TreeItem<> (new Dataset ("Root"));

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
    //    addColumn ("Cylinders", "Cyls", 50, Justification.RIGHT);
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
    Dataset foundDataset = datasets.get (dataset.getDatasetName ());

    if (foundDataset == null)
    {
      datasets.put (dataset.getDatasetName (), dataset);
      root.getChildren ().add (new TreeItem<> (dataset));
    }
    else
      foundDataset.merge (dataset);
  }

  public void addMember (Dataset member)
  {
    String memberName = member.getDatasetName ();
    int pos = memberName.indexOf ('(');
    String parentName = memberName.substring (0, pos);
    String childName = memberName.substring (pos + 1, memberName.length () - 1);

    Dataset foundDataset = datasets.get (memberName);
    if (foundDataset == null)
    {
      datasets.put (memberName, member);// add child (full name) to list
      Dataset parent = datasets.get (parentName);
      TreeItem<Dataset> parentTreeItem = null;
      if (parent == null)
      {
        parent = new Dataset (parentName);
        datasets.put (parentName, parent);
        parentTreeItem = new TreeItem<> (parent);
        root.getChildren ().add (parentTreeItem);
      }
      else
        for (TreeItem<Dataset> treeItem : root.getChildren ())
          if (treeItem.getValue ().getDatasetName ().equals (parentName))
          {
            parentTreeItem = treeItem;
            break;
          }

      // add child to parent
      if (parentTreeItem == null)
        System.out.println ("null parent");
      else
        parentTreeItem.getChildren ().add (new TreeItem<> (member));
    }
    else
      foundDataset.merge (member);
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
}