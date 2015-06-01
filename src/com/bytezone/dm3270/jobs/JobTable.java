package com.bytezone.dm3270.jobs;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class JobTable extends TableView<BatchJob>
{
  private final ObservableList<BatchJob> batchJobs = FXCollections.observableArrayList ();

  public JobTable ()
  {
    setStyle ("-fx-font-size: 11;");
    setFixedCellSize (20.0);

    TableColumn<BatchJob, String> colJobNumber = new TableColumn<> ("Job #");
    TableColumn<BatchJob, String> colJobName = new TableColumn<> ("Job name");
    TableColumn<BatchJob, String> colJobCompleted = new TableColumn<> ("Completed");
    TableColumn<BatchJob, Integer> colJobCC = new TableColumn<> ("Cond");

    colJobNumber.setPrefWidth (100);
    colJobNumber.setCellValueFactory (new PropertyValueFactory<> ("JobNumber"));

    colJobName.setPrefWidth (150);
    colJobName.setCellValueFactory (new PropertyValueFactory<> ("JobName"));

    colJobCompleted.setPrefWidth (100);
    colJobCompleted.setCellValueFactory (new PropertyValueFactory<> ("JobCompleted"));

    colJobCC.setPrefWidth (50);
    colJobCC.setCellValueFactory (new PropertyValueFactory<> ("JobConditionCode"));

    getColumns ().add (colJobNumber);
    getColumns ().add (colJobName);
    getColumns ().add (colJobCompleted);
    getColumns ().add (colJobCC);

    setItems (batchJobs);
  }

  public void addJob (BatchJob batchJob)
  {
    batchJobs.add (batchJob);
  }

  public BatchJob getBatchJob (int jobNumber)
  {
    for (BatchJob batchJob : batchJobs)
      if (batchJob.jobNumber == jobNumber)
        return batchJob;
    return null;
  }

  // this is a workaround until jdk 8u60 is released
  public void refresh ()
  {
    List<BatchJob> jobs = new ArrayList<> ();
    jobs.addAll (batchJobs);
    batchJobs.clear ();
    batchJobs.addAll (jobs);
  }
}