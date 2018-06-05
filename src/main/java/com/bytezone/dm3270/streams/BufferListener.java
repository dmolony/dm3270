package com.bytezone.dm3270.streams;

import java.time.LocalDateTime;

public interface BufferListener {

  void listen(TelnetSocket.Source targetRole, byte[] message, LocalDateTime dateTime,
              boolean genuine);

  void close();

}
