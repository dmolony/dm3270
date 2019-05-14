package com.bytezone.dm3270.streams;

import com.bytezone.dm3270.ConnectionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import javax.net.SocketFactory;

public class TerminalServer implements Runnable {

  private final String serverURL;
  private final int serverPort;
  private final SocketFactory socketFactory;
  private int connectionTimeoutMillis;
  private ConnectionListener connectionListener;

  private Socket serverSocket;
  private OutputStream serverOut;

  private final byte[] buffer = new byte[4096];
  private volatile boolean running;

  private final BufferListener telnetListener;

  public TerminalServer(String serverURL, int serverPort, SocketFactory socketFactory,
      BufferListener listener) {
    this.serverPort = serverPort;
    this.serverURL = serverURL;
    this.socketFactory = socketFactory;
    this.telnetListener = listener;
  }

  public void setConnectionTimeoutMillis(int connectionTimeoutMillis) {
    this.connectionTimeoutMillis = connectionTimeoutMillis;
  }

  public void setConnectionListener(ConnectionListener connectionListener) {
    this.connectionListener = connectionListener;
  }

  @Override
  public void run() {
    try {
      try {
        serverSocket = socketFactory.createSocket();
        serverSocket.connect(new InetSocketAddress(serverURL, serverPort), connectionTimeoutMillis);
        if (connectionListener != null) {
          connectionListener.onConnection();
        }
      } catch (IOException ex) {
        handleException(ex);
        return;
      }

      InputStream serverIn = serverSocket.getInputStream();
      serverOut = serverSocket.getOutputStream();

      running = true;
      while (running) {
        int bytesRead = serverIn.read(buffer);
        if (bytesRead < 0) {
          close();
          if (connectionListener != null) {
            connectionListener.onConnectionClosed();
          }
          break;
        }

        byte[] message = new byte[bytesRead];
        System.arraycopy(buffer, 0, message, 0, bytesRead);
        telnetListener.listen(TelnetSocket.Source.SERVER, message, LocalDateTime.now(), true);
      }
    } catch (IOException e) {
      if (running) {
        close();
        handleException(e);
      }
    }
  }

  private void handleException(IOException ex) {
    if (connectionListener != null) {
      connectionListener.onException(ex);
    } else {
      ex.printStackTrace();
    }
  }

  public synchronized void write(byte[] buffer) {
    // the no-op may come here if socket is closed from remote end and client has not been closed
    if (!running && buffer == TelnetState.NO_OP) {
      return;
    }

    try {
      serverOut.write(buffer);
      serverOut.flush();
    } catch (IOException e) {
      handleException(e);
    }
  }

  public void close() {
    try {
      running = false;

      if (serverSocket != null) {
        serverSocket.close();
      }

      if (telnetListener != null) {
        telnetListener.close();
      }
    } catch (IOException e) {
      handleException(e);
    }
  }

  @Override
  public String toString() {
    return String.format("TerminalSocket listening to %s : %d", serverURL, serverPort);
  }

}
