package com.bytezone.dm3270.buffers;

import java.util.Optional;

public interface ReplyBuffer extends Buffer
{
  public Optional<Buffer> getReply ();

  public void setReply (Buffer reply);
}