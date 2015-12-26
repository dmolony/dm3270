package com.bytezone.dm3270.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;

import com.bytezone.dm3270.streams.TelnetSocket.Source;
import com.bytezone.dm3270.utilities.Dm3270Utility;

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
  private final boolean debug = false;

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
        if (Thread.interrupted ())
        {
          System.out.println ("TerminalServer interrupted");
          break;
        }

        bytesRead = serverIn.read (buffer);
        if (bytesRead < 0)
        {
          close ();
          break;
        }

        if (Thread.currentThread ().isInterrupted ())
          System.out.println ("TerminalServer was interrupted!");

        if (debug)
        {
          System.out.println (toString ());
          System.out.println ("reading:");
          System.out.println (Dm3270Utility.toHex (buffer, 0, bytesRead));
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

  synchronized void write (byte[] buffer)
  {
    if (serverOut == null)
    {
      // the no-op may come here if the program is not closed after disconnection
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

    if (debug)
    {
      System.out.println (toString ());
      System.out.println ("writing:");
      System.out.println (Dm3270Utility.toHex (buffer));
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

      if (telnetListener != null)
        telnetListener.close ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }

  @Override
  public String toString ()
  {
    return String.format ("TerminalSocket listening to %s : %d", serverURL, serverPort);
  }
}