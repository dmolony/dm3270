package com.bytezone.dm3270.buffers;

import com.bytezone.dm3270.display.Screen;

public abstract class AbstractTN3270Command extends AbstractReplyBuffer
{
  protected final Screen screen;

  public AbstractTN3270Command (Screen screen)
  {
    this.screen = screen;
  }

  public AbstractTN3270Command (byte[] buffer, int offset, int length, Screen screen)
  {
    super (buffer, offset, length);
    this.screen = screen;
  }
}