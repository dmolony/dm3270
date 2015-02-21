package com.bytezone.dm3270.buffers;

public class DefaultBuffer extends AbstractBuffer
{
  public DefaultBuffer (byte[] buffer)
  {
    super (buffer);
  }

  @Override
  public void process ()
  {
    System.out.println ("Nothing to process");
  }

  @Override
  public String toString ()
  {
    return "DefaultBuffer";
  }
}