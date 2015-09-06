package com.bytezone.dm3270.assistant;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenDetails;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class BatchJobTab extends AbstractTransferTab
    implements ScreenChangeListener, BatchJobListener
{
  private static final Pattern outlistPattern = Pattern
      .compile ("(TSO )?OUT ([A-Z0-9]{2,8})\\((JOB(\\d+))\\) PRINT\\(([A-Z0-9]+)\\)");
  private final BatchJobTable jobTable = new BatchJobTable ();

  private BatchJob selectedBatchJob;

  public BatchJobTab (Screen screen, TextField text, Button execute)
  {
    super ("Batch Jobs", screen, text, execute);

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
    fireJobSelected (batchJob);
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
    if (isSelected ())
      setText ();
  }

  @Override
      void setText ()
  {
    ScreenDetails screenDetails = screen.getScreenDetails ();
    if (selectedBatchJob == null || selectedBatchJob.getJobCompleted () == null)
    {
      eraseCommand ();
      return;
    }

    String report = selectedBatchJob.getOutputFile ();
    String tsoPrefix = screenDetails.isTSOCommandScreen () ? "" : "TSO ";
    String ascii = " ASCII CRLF";

    String command = report == null
        ? String.format ("%s%s", tsoPrefix, selectedBatchJob.outputCommand ())
        : String.format ("%sIND$FILE GET %s%s", tsoPrefix, report, ascii);

    txtCommand.setText (command);
    btnExecute.setDisable (screenDetails.isKeyboardLocked ()
        || screenDetails.getTSOCommandField () == null);
  }

  // ---------------------------------------------------------------------------------//
  // BatchJobListener
  // ---------------------------------------------------------------------------------//

  @Override
  public void batchJobSubmitted (int jobNumber, String jobName)
  {
    BatchJob batchJob = new BatchJob (jobNumber, jobName);
    addBatchJob (batchJob);
  }

  @Override
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

  @Override
  public void batchJobFailed (int jobNumber, String jobName, String time)
  {
    BatchJob batchJob = getBatchJob (jobNumber);
    if (batchJob != null)
    {
      batchJob.failed (time);
      jobTable.refresh ();// temp fix before jdk 8u60
    }
  }

  // ---------------------------------------------------------------------------------//
  // Batch job selection listeners
  // ---------------------------------------------------------------------------------//

  private final Set<BatchJobSelectionListener> selectionListeners = new HashSet<> ();

  void fireJobSelected (BatchJob job)
  {
    for (BatchJobSelectionListener listener : selectionListeners)
      listener.jobSelected (job);
  }

  void addJobSelectionListener (BatchJobSelectionListener listener)
  {
    selectionListeners.add (listener);
  }

  void removeJobSelectionListener (BatchJobSelectionListener listener)
  {
    selectionListeners.remove (listener);
  }
}