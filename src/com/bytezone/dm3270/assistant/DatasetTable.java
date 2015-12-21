package com.bytezone.dm3270.assistant;

import java.util.Optional;

import com.bytezone.dm3270.utilities.DefaultTable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;

public class DatasetTable extends DefaultTable<Dataset>
{
  private final ObservableList<Dataset> datasets = FXCollections.observableArrayList ();

  public DatasetTable ()
  {
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

  public void addDataset (Dataset dataset)
  {
    String datasetName = dataset.getDatasetName ();

    Optional<Dataset> dataset2 = datasets.stream ()
        .filter (d -> d.getDatasetName ().equals (datasetName)).findAny ();
    if (!dataset2.isPresent ())
      datasets.add (dataset);
    //    else
    //      dataset2.get ().merge (dataset);
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
    //    else
    //      foundDataset.merge (member);
  }
}