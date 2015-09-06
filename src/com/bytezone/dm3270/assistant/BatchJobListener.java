package com.bytezone.dm3270.assistant;

public interface BatchJobListener
{
  public void batchJobSubmitted (int jobNumber, String jobName);

  public void batchJobEnded (int jobNumber, String jobName, String time,
      int conditionCode);

  public void batchJobFailed (int jobNumber, String jobName, String time);
}