package com.bytezone.dm3270.assistant;

import com.bytezone.dm3270.assistant.DatasetTable.Justification;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class BatchJobTable extends TableView<BatchJob>
{
  private Callback<TableColumn<BatchJob, String>, TableCell<BatchJob, String>> centreJustified;
  private final ObservableList<BatchJob> batchJobs = FXCollections.observableArrayList ();

  public BatchJobTable ()
  {
    setStyle ("-fx-font-size: 12; -fx-font-family: Monospaced");
    setFixedCellSize (20.0);
    createJustifications ();

    addColumn ("Job #", 100, Justification.CENTER,
               e -> e.getValue ().propertyJobNumber ());
    addColumn ("Job Name", 150, Justification.LEFT,
               e -> e.getValue ().propertyJobName ());
    addColumn ("Completed", 100, Justification.CENTER,
               e -> e.getValue ().propertyJobCompleted ());
    addColumn ("Cond", 80, Justification.CENTER,
               e -> e.getValue ().propertyConditionCode ());
    addColumn ("Job #", 200, Justification.LEFT,
               e -> e.getValue ().propertyOutputFile ());

    setItems (batchJobs);

    setPlaceholder (new Label ("No jobs have been submitted in this session"));
  }

  private TableColumn<BatchJob, String> addColumn (String heading, int width,
      Justification justification,
      Callback<CellDataFeatures<BatchJob, String>, ObservableValue<String>> callback)
  {
    TableColumn<BatchJob, String> column = new TableColumn<> (heading);
    column.setPrefWidth (width);
    column.setCellValueFactory (callback);
    getColumns ().add (column);

    if (justification == Justification.CENTER)
      column.setCellFactory (centreJustified);

    return column;
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

  private void createJustifications ()
  {
    centreJustified = new Callback<TableColumn<BatchJob, String>, //
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
  }
}