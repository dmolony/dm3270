package com.bytezone.dm3270.assistant;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class JobTable extends TableView<BatchJob>
{
  private final ObservableList<BatchJob> batchJobs = FXCollections.observableArrayList ();

  public JobTable ()
  {
    setStyle ("-fx-font-size: 12; -fx-font-family: Monospaced");
    setFixedCellSize (20.0);

    TableColumn<BatchJob, String> colJobNumber = new TableColumn<> ("Job #");
    TableColumn<BatchJob, String> colJobName = new TableColumn<> ("Job Name");
    TableColumn<BatchJob, String> colJobCompleted = new TableColumn<> ("Completed");
    TableColumn<BatchJob, String> colJobCC = new TableColumn<> ("Cond");
    TableColumn<BatchJob, String> colOutputFile = new TableColumn<> ("Output Dataset");

    colJobNumber.setPrefWidth (100);
    colJobNumber.setCellValueFactory (new PropertyValueFactory<> ("JobNumber"));

    colJobName.setPrefWidth (150);
    colJobName.setCellValueFactory (new PropertyValueFactory<> ("JobName"));

    colJobCompleted.setPrefWidth (100);
    colJobCompleted.setCellValueFactory (new PropertyValueFactory<> ("JobCompleted"));

    colJobCC.setPrefWidth (80);
    colJobCC.setCellValueFactory (new PropertyValueFactory<> ("JobConditionCode"));

    colOutputFile.setPrefWidth (200);
    colOutputFile.setCellValueFactory (new PropertyValueFactory<> ("OutputFile"));

    getColumns ().add (colJobNumber);
    getColumns ().add (colJobName);
    getColumns ().add (colJobCompleted);
    getColumns ().add (colJobCC);
    getColumns ().add (colOutputFile);

    setItems (batchJobs);

    Callback<TableColumn<BatchJob, String>, //
    TableCell<BatchJob, String>> centreJustified =
        new Callback<TableColumn<BatchJob, String>, //
        TableCell<BatchJob, String>> ()
        {
          @Override
          public TableCell<BatchJob, String> call (TableColumn<BatchJob, String> p)
          {
            TableCell<BatchJob, String> cell = new TableCell<BatchJob, String> ()
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

    colJobNumber.setCellFactory (centreJustified);
    colJobCompleted.setCellFactory (centreJustified);
    colJobCC.setCellFactory (centreJustified);

    setPlaceholder (new Label ("No jobs have been submitted in this session"));
  }

  public void addJob (BatchJob batchJob)
  {
    batchJobs.add (batchJob);
  }

  void setOutlist (String jobName, String jobNumber, String outlist)
  {
    for (BatchJob batchJob : batchJobs)
      if (batchJob.getJobNumber ().equals (jobNumber))
      {
        batchJob.setOutputFile (outlist);
        refresh ();// temp
        break;
      }
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
    BatchJob selectedBatchJob = getSelectionModel ().getSelectedItem ();
    List<BatchJob> tempJobs = new ArrayList<> ();

    tempJobs.addAll (batchJobs);
    batchJobs.clear ();
    batchJobs.addAll (tempJobs);

    if (selectedBatchJob != null)
      getSelectionModel ().select (selectedBatchJob);
    else if (batchJobs.size () == 1)
      getSelectionModel ().select (batchJobs.get (0));
  }
}