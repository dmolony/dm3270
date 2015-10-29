package com.bytezone.dm3270.buffers;

public abstract class AbstractTN3270Command extends AbstractReplyBuffer
{
  public AbstractTN3270Command ()
  {
    super ();
  }

  public AbstractTN3270Command (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);
  }
}