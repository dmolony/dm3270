package com.bytezone.dm3270.assistant;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BatchJob
{
  private final int jobNumber;

  private StringProperty propertyJobNumber;
  private StringProperty propertyJobName;
  private StringProperty propertyJobCompleted;
  private StringProperty propertyConditionCode;
  private StringProperty propertyOutputFile;
  private IntegerProperty propertyRandom;

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

  public StringProperty propertyJobNumber ()
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

  public StringProperty propertyJobName ()
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

  public StringProperty propertyJobCompleted ()
  {
    if (propertyJobCompleted == null)
      propertyJobCompleted = new SimpleStringProperty (this, "JobCompleted");
    return propertyJobCompleted;
  }

  // ConditionCode

  public void setJobConditionCode (String value)
  {
    propertyConditionCode ().setValue (value);
  }

  public String getJobConditionCode ()
  {
    return propertyConditionCode ().getValue ();
  }

  public StringProperty propertyConditionCode ()
  {
    if (propertyConditionCode == null)
      propertyConditionCode = new SimpleStringProperty (this, "JobConditionCode");
    return propertyConditionCode;
  }

  // OutputFile

  public void setOutputFile (String value)
  {
    propertyOutputFile ().setValue (value);
  }

  public String getOutputFile ()
  {
    return propertyOutputFile ().getValue ();
  }

  public StringProperty propertyOutputFile ()
  {
    if (propertyOutputFile == null)
      propertyOutputFile = new SimpleStringProperty (this, "OutputFile");
    return propertyOutputFile;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Job number ... : %s%n", propertyJobNumber.getValue ()));
    text.append (String.format ("Job name ..... : %s%n", propertyJobName.getValue ()));

    if (propertyJobCompleted != null)
    {
      text.append (String.format ("Completed .... : %s%n",
                                  propertyJobCompleted.getValue ()));
      text.append (String.format ("Condition .... : %s%n",
                                  propertyConditionCode.getValue ()));
    }

    return text.toString ();
  }
}