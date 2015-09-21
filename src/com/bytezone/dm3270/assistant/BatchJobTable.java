package com.bytezone.dm3270.assistant;

import com.bytezone.dm3270.assistant.DatasetTable.Justification;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class BatchJobTable extends TableView<BatchJob>
{
  private final ObservableList<BatchJob> batchJobs = FXCollections.observableArrayList ();

  public BatchJobTable ()
  {
    setStyle ("-fx-font-size: 12; -fx-font-family: Monospaced");
    setFixedCellSize (20.0);

    addColumn ("Job ID", 100, Justification.CENTER,
               e -> e.getValue ().propertyJobNumber ());
    addColumn ("Job Name", 150, Justification.LEFT,
               e -> e.getValue ().propertyJobName ());
    addColumn ("Completed", 100, Justification.CENTER,
               e -> e.getValue ().propertyJobCompleted ());
    addColumn ("Cond", 100, Justification.CENTER,
               e -> e.getValue ().propertyConditionCode ());
    addColumn ("Output dataset", 200, Justification.LEFT,
               e -> e.getValue ().propertyOutputFile ());

    setItems (batchJobs);

    setPlaceholder (new Label ("No jobs have been submitted in this session"));
  }

  private void addColumn (String heading, int width, Justification justification,
      Callback<CellDataFeatures<BatchJob, String>, ObservableValue<String>> callback)
  {
    TableColumn<BatchJob, String> column = new TableColumn<> (heading);
    column.setPrefWidth (width);
    column.setCellValueFactory (callback);
    getColumns ().add (column);

    if (justification == Justification.CENTER)
      column.setStyle ("-fx-alignment: CENTER;");
  }

  void setOutlist (String jobName, String jobNumber, String outlist)
  {
    for (BatchJob batchJob : batchJobs)
      if (batchJob.matches (jobNumber))
      {
        batchJob.setOutputFile (outlist);
        break;
      }
  }

  public void addBatchJob (BatchJob newBatchJob)
  {
    for (BatchJob batchJob : batchJobs)
      if (batchJob.matches (newBatchJob))
        return;

    batchJobs.add (newBatchJob);
  }

  public BatchJob getBatchJob (int jobNumber)
  {
    for (BatchJob batchJob : batchJobs)
      if (batchJob.matches (jobNumber))
        return batchJob;

    return null;
  }
}