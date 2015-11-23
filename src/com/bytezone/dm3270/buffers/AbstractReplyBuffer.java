package com.bytezone.dm3270.buffers;

import java.util.Optional;

public abstract class AbstractReplyBuffer extends AbstractBuffer implements ReplyBuffer
{
  private Buffer reply;

  public AbstractReplyBuffer ()
  {
    super ();
  }

  public AbstractReplyBuffer (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);
  }

  @Override
  public void setReply (Buffer reply)
  {
    this.reply = reply;
  }

  @Override
  public Optional<Buffer> getReply ()
  {
    return reply == null ? Optional.empty () : Optional.of (reply);
  }
}