package com.bytezone.dm3270.streams;

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

  @Override
  public void run() {
    try {
      serverSocket = socketFactory.createSocket();
      serverSocket.connect(new InetSocketAddress(serverURL, serverPort));

      InputStream serverIn = serverSocket.getInputStream();
      serverOut = serverSocket.getOutputStream();

      running = true;
      while (running) {
        int bytesRead = serverIn.read(buffer);
        if (bytesRead < 0) {
          close();
          break;
        }

        byte[] message = new byte[bytesRead];
        System.arraycopy(buffer, 0, message, 0, bytesRead);
        telnetListener.listen(TelnetSocket.Source.SERVER, message, LocalDateTime.now(), true);
      }
    } catch (IOException e) {
      if (running) {
        e.printStackTrace();
        close();
      }
    }
  }

  synchronized void write(byte[] buffer) {
    if (serverOut == null) {
      // the no-op may come here if the program is not closed after disconnection
      System.out.println("serverOut is null in TerminalServer");
      return;
    }

    try {
      serverOut.write(buffer);
      serverOut.flush();
    } catch (IOException e) {
      e.printStackTrace();
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
      e.printStackTrace();
    }
  }

  @Override
  public String toString() {
    return String.format("TerminalSocket listening to %s : %d", serverURL, serverPort);
  }

}
