package com.bytezone.dm3270.buffers;

import com.bytezone.dm3270.application.OldScreenHandler;

public abstract class AbstractOrder extends AbstractBuffer
{
  protected final OldScreenHandler screenHandler;

  public AbstractOrder (byte[] buffer, int offset, int length, OldScreenHandler screenHandler)
  {
    super (buffer, offset, length);
    this.screenHandler = screenHandler;
  }
}