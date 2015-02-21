package com.bytezone.dm3270.buffers;

import com.bytezone.dm3270.application.ScreenHandler;

public abstract class AbstractOrder extends AbstractBuffer
{
  protected final ScreenHandler screenHandler;

  public AbstractOrder (byte[] buffer, int offset, int length, ScreenHandler screenHandler)
  {
    super (buffer, offset, length);
    this.screenHandler = screenHandler;
  }
}