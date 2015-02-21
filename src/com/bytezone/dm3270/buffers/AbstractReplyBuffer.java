package com.bytezone.dm3270.buffers;

public abstract class AbstractReplyBuffer extends AbstractBuffer implements ReplyBuffer
{
  protected Buffer reply;

  public AbstractReplyBuffer ()
  {
    super ();
  }

  public AbstractReplyBuffer (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);
  }

  @Override
  public Buffer getReply ()
  {
    return reply;
  }
}