package com.bytezone.dm3270.assistant;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BatchJob
{
  private final int jobNumber;

  private StringProperty propertyJobNumber;
  private StringProperty propertyJobName;
  private StringProperty propertyJobCompleted;
  private StringProperty propertyJobConditionCode;
  private StringProperty propertyJobOutputFile;

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
    return propertyJobNumber.getValue ().equals (jobNumber);
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
    return String.format ("OUT %s(%s) PRINT(%s)", propertyJobName.getValue (),
                          propertyJobNumber.getValue (), propertyJobNumber.getValue ());
  }

  public String datasetName ()
  {
    return String.format ("%s.OUTLIST", getJobNumber ());
  }

  // JobNumber

  public void setJobNumber (String value)
  {
    propertyJobNumber ().setValue (value);
  }

  public String getJobNumber ()
  {
    return propertyJobNumber ().getValue ();
  }

  StringProperty propertyJobNumber ()
  {
    if (propertyJobNumber == null)
      propertyJobNumber = new SimpleStringProperty (this, "JobNumber");
    return propertyJobNumber;
  }

  // JobName

  public void setJobName (String value)
  {
    propertyJobName ().setValue (value);
  }

  public String getJobName ()
  {
    return propertyJobName ().getValue ();
  }

  StringProperty propertyJobName ()
  {
    if (propertyJobName == null)
      propertyJobName = new SimpleStringProperty (this, "JobName");
    return propertyJobName;
  }

  // JobCompleted

  public void setJobCompleted (String value)
  {
    propertyJobCompleted ().setValue (value);
  }

  public String getJobCompleted ()
  {
    return propertyJobCompleted ().getValue ();
  }

  StringProperty propertyJobCompleted ()
  {
    if (propertyJobCompleted == null)
      propertyJobCompleted = new SimpleStringProperty (this, "JobCompleted");
    return propertyJobCompleted;
  }

  // JobConditionCode

  public void setJobConditionCode (String value)
  {
    propertyJobConditionCode ().setValue (value);
  }

  public String getJobConditionCode ()
  {
    return propertyJobConditionCode ().getValue ();
  }

  public StringProperty propertyJobConditionCode ()
  {
    if (propertyJobConditionCode == null)
      propertyJobConditionCode = new SimpleStringProperty (this, "JobConditionCode");
    return propertyJobConditionCode;
  }

  // OutputFile

  public void setJobOutputFile (String value)
  {
    propertyJobOutputFile ().setValue (value);
  }

  public String getJobOutputFile ()
  {
    return propertyJobOutputFile ().getValue ();
  }

  public StringProperty propertyJobOutputFile ()
  {
    if (propertyJobOutputFile == null)
      propertyJobOutputFile = new SimpleStringProperty (this, "JobOutputFile");
    return propertyJobOutputFile;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Job number ... : %s%n", getJobNumber ()));
    text.append (String.format ("Job name ..... : %s%n", getJobName ()));

    if (propertyJobCompleted != null)
    {
      text.append (String.format ("Completed .... : %s%n", getJobCompleted ()));
      text.append (String.format ("Condition .... : %s%n", getJobConditionCode ()));
    }

    return text.toString ();
  }
}