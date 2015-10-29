package com.bytezone.dm3270.buffers;

import com.bytezone.dm3270.display.Screen;

public class DefaultBuffer extends AbstractBuffer
{
  public DefaultBuffer (byte[] buffer)
  {
    super (buffer);
  }

  @Override
  public void process (Screen screen)
  {
    System.out.println ("Nothing to process");
  }

  @Override
  public String toString ()
  {
    return "DefaultBuffer";
  }
}