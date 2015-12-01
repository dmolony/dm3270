package com.bytezone.dm3270.assistant;

import java.util.Optional;

import com.bytezone.dm3270.utilities.DefaultTable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;

public class BatchJobTable extends DefaultTable<BatchJob>
{
  private final ObservableList<BatchJob> batchJobs = FXCollections.observableArrayList ();

  public BatchJobTable ()
  {
    addColumnString ("Job ID", 100, Justification.CENTER, "jobNumber");
    addColumnString ("Job Name", 150, Justification.LEFT, "jobName");
    addColumnString ("Completed", 100, Justification.CENTER, "jobCompleted");
    addColumnString ("Cond", 100, Justification.CENTER, "jobConditionCode");
    addColumnString ("Output dataset", 200, Justification.LEFT, "jobOutputFile");

    setItems (batchJobs);

    setPlaceholder (new Label ("No jobs have been submitted in this session"));
  }

  void setOutlist (String jobName, String jobNumber, String outlist)
  {
    batchJobs.stream ().filter (b -> b.matches (jobNumber)).findAny ()
        .ifPresent (b -> b.setJobOutputFile (outlist));
  }

  public void addBatchJob (BatchJob newBatchJob)
  {
    if (batchJobs.stream ().noneMatch (b -> b.matches (newBatchJob)))
      batchJobs.add (newBatchJob);
  }

  public Optional<BatchJob> getBatchJob (int jobNumber)
  {
    return batchJobs.stream ().filter (b -> b.matches (jobNumber)).findAny ();
  }
}