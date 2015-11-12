package com.bytezone.dm3270.assistant;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BatchJob
{
  private final int jobNumber;

  private StringProperty jobNumberProperty;
  private StringProperty jobNameProperty;
  private StringProperty jobCompletedProperty;
  private StringProperty jobConditionCodeProperty;
  private StringProperty jobOutputFileProperty;

  public BatchJob (int jobNumber, String jobName)
  {
    this.jobNumber = jobNumber;

    setJobNumber (String.format ("JOB%05d", jobNumber));
    setJobName (jobName);
  }

  public boolean matches (BatchJob batchJob)
  {
    return this.jobNumber == batchJob.jobNumber;
  }

  public boolean matches (int jobNumber)
  {
    return this.jobNumber == jobNumber;
  }

  public boolean matches (String jobNumber)
  {
    return jobNumberProperty.getValue ().equals (jobNumber);
  }

  public void completed (String timeCompleted, int conditionCode)
  {
    setJobCompleted (timeCompleted);
    setJobConditionCode (conditionCode + "");
  }

  public void failed (String timeCompleted)
  {
    setJobCompleted (timeCompleted);
    setJobConditionCode ("JCL ERROR");
  }

  public String outputCommand ()
  {
    return String.format ("OUT %s(%s) PRINT(%s)", jobNameProperty.getValue (),
                          jobNumberProperty.getValue (), jobNumberProperty.getValue ());
  }

  public String datasetName ()
  {
    return String.format ("%s.OUTLIST", getJobNumber ());
  }

  // ---------------------------------------------------------------------------------//
  // JobNumber
  // ---------------------------------------------------------------------------------//
  public void setJobNumber (String value)
  {
    jobNumberProperty ().setValue (value);
  }

  public String getJobNumber ()
  {
    return jobNumberProperty ().getValue ();
  }

  StringProperty jobNumberProperty ()
  {
    if (jobNumberProperty == null)
      jobNumberProperty = new SimpleStringProperty ();
    return jobNumberProperty;
  }

  // ---------------------------------------------------------------------------------//
  // JobName
  // ---------------------------------------------------------------------------------//
  public void setJobName (String value)
  {
    jobNameProperty ().setValue (value);
  }

  public String getJobName ()
  {
    return jobNameProperty ().getValue ();
  }

  public StringProperty jobNameProperty ()
  {
    if (jobNameProperty == null)
      jobNameProperty = new SimpleStringProperty ();
    return jobNameProperty;
  }

  // ---------------------------------------------------------------------------------//
  // JobCompleted
  // ---------------------------------------------------------------------------------//
  public void setJobCompleted (String value)
  {
    jobCompletedProperty ().setValue (value);
  }

  public String getJobCompleted ()
  {
    return jobCompletedProperty ().getValue ();
  }

  public StringProperty jobCompletedProperty ()
  {
    if (jobCompletedProperty == null)
      jobCompletedProperty = new SimpleStringProperty ();
    return jobCompletedProperty;
  }

  // ---------------------------------------------------------------------------------//
  // JobConditionCode
  // ---------------------------------------------------------------------------------//
  public void setJobConditionCode (String value)
  {
    jobConditionCodeProperty ().setValue (value);
  }

  public String getJobConditionCode ()
  {
    return jobConditionCodeProperty ().getValue ();
  }

  public StringProperty jobConditionCodeProperty ()
  {
    if (jobConditionCodeProperty == null)
      jobConditionCodeProperty = new SimpleStringProperty ();
    return jobConditionCodeProperty;
  }

  // ---------------------------------------------------------------------------------//
  // JobOutputFile
  // ---------------------------------------------------------------------------------//
  public void setJobOutputFile (String value)
  {
    jobOutputFileProperty ().setValue (value);
  }

  public String getJobOutputFile ()
  {
    return jobOutputFileProperty ().getValue ();
  }

  public StringProperty jobOutputFileProperty ()
  {
    if (jobOutputFileProperty == null)
      jobOutputFileProperty = new SimpleStringProperty ();
    return jobOutputFileProperty;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Job number ... : %s%n", getJobNumber ()));
    text.append (String.format ("Job name ..... : %s%n", getJobName ()));

    if (jobCompletedProperty != null)
    {
      text.append (String.format ("Completed .... : %s%n", getJobCompleted ()));
      text.append (String.format ("Condition .... : %s%n", getJobConditionCode ()));
    }

    return text.toString ();
  }
}