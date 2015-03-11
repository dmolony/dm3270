package com.bytezone.dm3270.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;

import com.bytezone.dm3270.streams.TelnetSocket.Source;

public class TerminalServer implements Runnable
{
  private final int serverPort;
  private final String serverURL;
  private final Socket serverSocket = new Socket ();
  private InputStream serverIn;
  private OutputStream serverOut;

  private final byte[] buffer = new byte[4096];
  private int bytesRead;
  private volatile boolean running;

  private final BufferListener telnetListener;

  public TerminalServer (String serverURL, int serverPort, BufferListener listener)
  {
    this.serverPort = serverPort;
    this.serverURL = serverURL;
    this.telnetListener = listener;
  }

  @Override
  public void run ()
  {
    try
    {
      serverSocket.connect (new InetSocketAddress (serverURL, serverPort));

      serverIn = serverSocket.getInputStream ();
      serverOut = serverSocket.getOutputStream ();

      running = true;
      while (running)
      {
        bytesRead = serverIn.read (buffer);
        if (bytesRead < 0)
        {
          System.out.println ("Closing server socket in TerminalServer");
          running = false;
          serverSocket.close ();
          break;
        }

        byte[] message = new byte[bytesRead];
        System.arraycopy (buffer, 0, message, 0, bytesRead);
        telnetListener.listen (Source.SERVER, message, LocalDateTime.now (), true);
      }
    }
    catch (IOException e)
    {
      if (running)
      {
        e.printStackTrace ();
        close ();
      }
    }
  }

  public void write (byte[] buffer)
  {
    if (serverOut == null)
    {
      System.out.println ("serverOut is null in TerminalServer");
      return;
    }

    try
    {
      serverOut.write (buffer);
      serverOut.flush ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }

  public void close ()
  {
    try
    {
      running = false;
      serverIn = null;
      serverOut = null;

      if (serverSocket != null)
        serverSocket.close ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }
}