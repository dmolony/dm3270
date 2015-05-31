package com.bytezone.dm3270.jobs;

public class BatchJob
{
  public final int jobNumber;
  public final String jobName;
  private String timeCompleted;
  private int conditionCode;

  public BatchJob (int jobNumber, String jobName)
  {
    this.jobName = jobName;
    this.jobNumber = jobNumber;
  }

  public void completed (String timeCompleted, int conditionCode)
  {
    this.timeCompleted = timeCompleted;
    this.conditionCode = conditionCode;
  }

  public String outputCommand ()
  {
    return String.format ("OUTPUT % PRINT(%s.JOB%d)", jobName, jobName, jobNumber);
  }

  public String datasetName ()
  {
    return String.format ("%s.JOB%s.OUTLIST", jobName, jobName, jobNumber);
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