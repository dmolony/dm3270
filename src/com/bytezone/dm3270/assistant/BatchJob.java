package com.bytezone.dm3270.assistant;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BatchJob
{
  public final int jobNumber;
  public final String jobName;
  private String timeCompleted;
  private int conditionCode;

  private StringProperty propertyJobNumber;
  private StringProperty propertyJobName;
  private StringProperty propertyJobCompleted;
  private StringProperty propertyConditionCode;
  private StringProperty propertyOutputFile;

  public BatchJob (int jobNumber, String jobName)
  {
    this.jobName = jobName;
    this.jobNumber = jobNumber;

    setJobNumber (String.format ("JOB%05d", jobNumber));
    setJobName (jobName);
  }

  public void completed (String timeCompleted, int conditionCode)
  {
    this.timeCompleted = timeCompleted;
    this.conditionCode = conditionCode;

    setJobCompleted (timeCompleted);
    setJobConditionCode (conditionCode + "");
  }

  public String outputCommand ()
  {
    return String.format ("OUT %s(%s) PRINT(%s)", jobName, getJobNumber (),
                          getJobNumber ());
  }

  public String datasetName ()
  {
    return String.format ("%s.OUTLIST", getJobNumber ());
  }

  // JobNumber

  public void setJobNumber (String value)
  {
    propertyJobNumber ().set (value);
  }

  public String getJobNumber ()
  {
    return propertyJobNumber ().get ();
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
    propertyJobName ().set (value);
  }

  public String getJobName ()
  {
    return propertyJobName ().get ();
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
    propertyJobCompleted ().set (value);
  }

  public String getJobCompleted ()
  {
    return propertyJobCompleted ().get ();
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
    propertyConditionCode ().set (value);
  }

  public String getJobConditionCode ()
  {
    return propertyConditionCode ().get ();
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
    propertyOutputFile ().set (value);
  }

  public String getOutputFile ()
  {
    return propertyOutputFile ().get ();
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

    text.append (String.format ("Job number ... : %d%n", jobNumber));
    text.append (String.format ("Job name ..... : %s%n", jobName));

    if (timeCompleted != null)
    {
      text.append (String.format ("Completed .... : %s%n", timeCompleted));
      text.append (String.format ("Condition .... : %s%n", conditionCode));
    }

    return text.toString ();
  }
}