package com.bytezone.dm3270.buffers;

import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.display.Screen;

public abstract class AbstractTN3270Command extends AbstractReplyBuffer
{
  protected final ScreenHandler screenHandler;
  protected final Screen screen;

  public AbstractTN3270Command (ScreenHandler screenHandler, Screen screen)
  {
    this.screenHandler = screenHandler;
    this.screen = screen;
  }

  public AbstractTN3270Command (byte[] buffer, int offset, int length,
      ScreenHandler screenHandler, Screen screen)
  {
    super (buffer, offset, length);
    this.screenHandler = screenHandler;
    this.screen = screen;
  }
}