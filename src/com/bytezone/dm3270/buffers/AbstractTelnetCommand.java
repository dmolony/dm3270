package com.bytezone.dm3270.buffers;

import com.bytezone.dm3270.streams.TelnetState;

public abstract class AbstractTelnetCommand extends AbstractReplyBuffer
{
  protected TelnetState telnetState;

  public AbstractTelnetCommand (byte[] buffer, int offset, int length,
      TelnetState telnetState)
  {
    super (buffer, offset, length);
    this.telnetState = telnetState;
  }

  @Override
  public byte[] getTelnetData ()
  {
    return data;        // do not expand anything, do not append EOR bytes
  }
}