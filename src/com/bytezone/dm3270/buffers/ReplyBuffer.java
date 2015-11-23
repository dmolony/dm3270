package com.bytezone.dm3270.buffers;

public interface ReplyBuffer extends Buffer
{
  public abstract Buffer getReply ();

  public void setReply (Buffer reply);
}