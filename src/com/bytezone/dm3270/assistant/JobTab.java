package com.bytezone.dm3270.assistant;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenDetails;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class JobTab extends TransferTab implements ScreenChangeListener
{
  private static final Pattern outlistPattern = Pattern
      .compile ("(TSO )?OUT ([A-Z0-9]{2,8})\\((JOB(\\d+))\\) PRINT\\(([A-Z0-9]+)\\)");
  private final JobTable jobTable = new JobTable ();
  private final Screen screen;

  private boolean isTSOCommandScreen;
  private Field tsoCommandField;

  private BatchJob selectedBatchJob;

  public JobTab (Screen screen, TextField text, Button execute)
  {
    super ("Batch Jobs", text, execute);

    this.screen = screen;

    jobTable.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, newSelection) -> {
          if (newSelection != null)
            select (newSelection);
        });

    setContent (jobTable);
  }

  private void select (BatchJob batchJob)
  {
    selectedBatchJob = batchJob;
    setText ();
  }

  public void addBatchJob (BatchJob batchJob)
  {
    jobTable.addJob (batchJob);
  }

  public BatchJob getBatchJob (int jobNumber)
  {
    return jobTable.getBatchJob (jobNumber);
  }

  void tsoCommand (String command)
  {
    Matcher matcher = outlistPattern.matcher (command);
    if (matcher.matches ())
    {
      String jobName = matcher.group (2);
      String jobNumber = matcher.group (3);
      String outlist = matcher.group (5) + ".OUTLIST";
      jobTable.setOutlist (jobName, jobNumber, outlist);
    }
  }

  @Override
  public void screenChanged ()
  {
    ScreenDetails screenDetails = screen.getScreenDetails ();
    isTSOCommandScreen = screenDetails.isTSOCommandScreen ();
    tsoCommandField = screenDetails.getTSOCommandField ();

    if (isSelected ())
      setText ();
  }

  @Override
      void setText ()
  {
    if (selectedBatchJob == null || selectedBatchJob.getJobCompleted () == null)
    {
      txtCommand.setText ("");
      btnExecute.setDisable (true);
      return;
    }

    String report = selectedBatchJob.getOutputFile ();
    String command = report == null ? selectedBatchJob.outputCommand ()
        : String.format ("IND$FILE GET %s", report);

    if (!isTSOCommandScreen)
      command = "TSO " + command;

    txtCommand.setText (command);
    btnExecute.setDisable (tsoCommandField == null || command.isEmpty ());
  }

  // ---------------------------------------------------------------------------------//
  // Batch jobs
  // ---------------------------------------------------------------------------------//

  public void batchJobSubmitted (int jobNumber, String jobName)
  {
    BatchJob batchJob = new BatchJob (jobNumber, jobName);
    addBatchJob (batchJob);
  }

  public void batchJobEnded (int jobNumber, String jobName, String time,
      int conditionCode)
  {
    BatchJob batchJob = getBatchJob (jobNumber);
    if (batchJob != null)
    {
      batchJob.completed (time, conditionCode);
      jobTable.refresh ();// temp fix before jdk 8u60
    }
  }

  public void batchJobFailed (int jobNumber, String jobName, String time)
  {
    BatchJob batchJob = getBatchJob (jobNumber);
    if (batchJob != null)
    {
      batchJob.failed (time);
      jobTable.refresh ();// temp fix before jdk 8u60
    }
  }
}