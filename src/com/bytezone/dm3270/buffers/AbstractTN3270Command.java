package com.bytezone.dm3270.buffers;

import com.bytezone.dm3270.application.ScreenHandler;

public abstract class AbstractTN3270Command extends AbstractReplyBuffer
{
  protected final ScreenHandler screenHandler;

  public AbstractTN3270Command (ScreenHandler screenHandler)
  {
    this.screenHandler = screenHandler;
  }

  public AbstractTN3270Command (byte[] buffer, int offset, int length,
      ScreenHandler screenHandler)
  {
    super (buffer, offset, length);
    this.screenHandler = screenHandler;
  }
}