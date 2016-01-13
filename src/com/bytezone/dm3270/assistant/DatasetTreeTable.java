package com.bytezone.dm3270.assistant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;

public class DatasetTreeTable extends DefaultTreeTable<Dataset>
{
  private final TreeItem<Dataset> root = new TreeItem<> (new Dataset ("Root"));
  private final Map<String, DatasetEntry> entries = new HashMap<> ();

  public DatasetTreeTable ()
  {
    setRoot (root);
    setShowRoot (false);

    // NB propertyName must have a corresponding method xxxProperty() in Dataset
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
    addColumnString ("Referred", 100, Justification.CENTER, "referredDate");
    addColumnString ("Catalog/ID", 150, Justification.LEFT, "catalog");

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
          root.getChildren ().add (parentEntry.treeItem);                 // 1st level
        }

        parentEntry.add (dataset);
        parentEntry.treeItem.getChildren ().add (datasetEntry.treeItem);  // 2nd level

        // would be good to open the parent
      }
      else
        root.getChildren ().add (datasetEntry.treeItem);                  // 1st level
    }
  }

  class DatasetEntry
  {
    Dataset dataset;
    TreeItem<Dataset> treeItem;

    List<Dataset> members;            // only a PDS uses this

    String parentName;                // only PDS members use these 2 name fields
    String memberName;

    DatasetEntry (Dataset dataset)
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
    DatasetEntry (String name)
    {
      dataset = new Dataset (name);
      parentName = name;
      members = new ArrayList<> ();           // so isPDS() returns true
      treeItem = new TreeItem<> (dataset);
    }

    void add (Dataset pdsMember)
    {
      if (members == null)
        members = new ArrayList<> ();
      members.add (pdsMember);
    }

    boolean isFlatFile ()
    {
      return parentName == null && members == null;
    }

    boolean isPDS ()
    {
      return members != null;
    }

    boolean isPDSMember ()
    {
      return parentName != null;
    }
  }
}