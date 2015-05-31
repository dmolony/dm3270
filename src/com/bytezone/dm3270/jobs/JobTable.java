package com.bytezone.dm3270.jobs;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class JobTable extends TableView<BatchJob>
{
  public JobTable ()
  {
    setStyle ("-fx-font-size: 11;");
    setFixedCellSize (20.0);

    TableColumn<BatchJob, String> colJobNumber = new TableColumn<> ("Job #");
    TableColumn<BatchJob, String> colJobName = new TableColumn<> ("Job name");
    TableColumn<BatchJob, String> colJobFinishedTime =
        new TableColumn<> ("Time finished");
    TableColumn<BatchJob, String> colJobCC = new TableColumn<> ("CC");

    colJobNumber.setPrefWidth (100);
    colJobNumber.setCellValueFactory (new PropertyValueFactory<> ("jobNumber"));

    colJobName.setPrefWidth (150);
    colJobName.setCellValueFactory (new PropertyValueFactory<> ("jobName"));

    colJobFinishedTime.setPrefWidth (100);
    colJobFinishedTime.setCellValueFactory (new PropertyValueFactory<> ("jobFinished"));

    colJobCC.setPrefWidth (50);
    colJobCC.setCellValueFactory (new PropertyValueFactory<> ("jobConditionCode"));

    getColumns ().add (colJobNumber);
    getColumns ().add (colJobName);
    getColumns ().add (colJobFinishedTime);
    getColumns ().add (colJobCC);
  }
}