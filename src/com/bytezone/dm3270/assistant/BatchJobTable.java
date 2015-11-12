package com.bytezone.dm3270.assistant;

import java.util.Optional;

import com.bytezone.dm3270.assistant.DatasetTable.Justification;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class BatchJobTable extends TableView<BatchJob>
{
  private final ObservableList<BatchJob> batchJobs = FXCollections.observableArrayList ();

  public BatchJobTable ()
  {
    setStyle ("-fx-font-size: 12; -fx-font-family: Monospaced");
    setFixedCellSize (20.0);

    addColumn ("Job ID", 100, Justification.CENTER, "jobNumber");
    addColumn ("Job Name", 150, Justification.LEFT, "jobName");
    addColumn ("Completed", 100, Justification.CENTER, "jobCompleted");
    addColumn ("Cond", 100, Justification.CENTER, "jobConditionCode");
    addColumn ("Output dataset", 200, Justification.LEFT, "jobOutputFile");

    setItems (batchJobs);

    setPlaceholder (new Label ("No jobs have been submitted in this session"));
  }

  private void addColumn (String heading, int width, Justification justification,
      String propertyName)
  {
    TableColumn<BatchJob, String> column = new TableColumn<> (heading);
    column.setPrefWidth (width);
    column
        .setCellValueFactory (new PropertyValueFactory<BatchJob, String> (propertyName));
    getColumns ().add (column);

    if (justification == Justification.CENTER)
      column.setStyle ("-fx-alignment: CENTER;");
  }

  void setOutlist (String jobName, String jobNumber, String outlist)
  {
    batchJobs.stream ().filter (b -> b.matches (jobNumber)).findFirst ()
        .ifPresent (b -> b.setJobOutputFile (outlist));
  }

  public void addBatchJob (BatchJob newBatchJob)
  {
    if (batchJobs.stream ().noneMatch (b -> b.matches (newBatchJob)))
      batchJobs.add (newBatchJob);
  }

  public Optional<BatchJob> getBatchJob (int jobNumber)
  {
    return batchJobs.stream ().filter (b -> b.matches (jobNumber)).findFirst ();
  }
}