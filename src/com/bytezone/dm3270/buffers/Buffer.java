package com.bytezone.dm3270.buffers;

public interface Buffer
{
  public abstract byte[] getData ();

  public abstract byte[] getTelnetData ();

  public abstract int size ();

  public abstract void process ();
}