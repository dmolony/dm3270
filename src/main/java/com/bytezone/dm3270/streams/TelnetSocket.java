package com.bytezone.dm3270.streams;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelnetSocket implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(TelnetSocket.class);

  private static final boolean GENUINE = true;

  private final String name;
  private final Source source;

  private Socket socket;
  private InputStream inputStream;

  private final byte[] buffer = new byte[4096];

  private final BufferListener telnetListener;
  private volatile boolean running;

  public enum Source {
    CLIENT, SERVER
  }

  // Only used by a SpyServer, which creates two SocketListeners. Each SocketListener
  // copies its inputStream to its partner's outputStream after sending a copy to
  // the listener.

  public TelnetSocket(Source source, Socket socket, BufferListener listener)
      throws IOException {
    if (source == null) {
      throw new IllegalArgumentException("Source cannot be null");
    }
    if (socket == null) {
      throw new IllegalArgumentException("Socket cannot be null");
    }
    if (listener == null) {
      throw new IllegalArgumentException("Listener cannot be null");
    }

    this.name = source == Source.CLIENT ? "Client" : "Server";
    this.source = source;
    this.socket = socket;
    this.telnetListener = listener;

    this.inputStream = socket.getInputStream();
  }

  @Override
  public void run() {
    running = true;
    while (running) {
      if (Thread.interrupted()) {
        LOG.debug("TelnetSocket interrupted");
        break;
      }
      try {
        int bytesRead = inputStream.read(buffer);
        if (bytesRead == -1) {
          LOG.debug("{} has no data on input stream", name);
          close();
          return;
        }

        // take a copy of the input buffer and send it to the TelnetListener
        byte[] message = new byte[bytesRead];
        System.arraycopy(buffer, 0, message, 0, message.length);
        telnetListener.listen(source, message, LocalDateTime.now(), GENUINE);
      } catch (IOException e) {
        if (running) {
          LOG.error("{} closing due to IOException", name, e);
        } else {
          LOG.debug("{} quitting", name, e);
        }
        close();
        return;
      }
    }

    LOG.debug("{} closing - bye everyone", name);
    close();
  }

  private void close() {
    running = false;

    try {
      if (socket != null) {
        socket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    socket = null;
    inputStream = null;
  }

  @Override
  public String toString() {
    return String.format("TelnetSocket: Source=%s, name=%s", source, name);
  }

}
