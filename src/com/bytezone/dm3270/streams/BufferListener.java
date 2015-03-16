package com.bytezone.dm3270.streams;

import java.time.LocalDateTime;

import com.bytezone.dm3270.streams.TelnetSocket.Source;

public interface BufferListener
{
  public void listen (Source targetRole, byte[] message, LocalDateTime dateTime,
      boolean genuine);

  public void close ();
}