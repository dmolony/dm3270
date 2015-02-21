package com.bytezone.dm3270.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import com.bytezone.dm3270.session.Session;
import com.bytezone.dm3270.streams.TelnetSocket.Source;

public class SpyServer implements Runnable
{
  private Socket clientSocket;
  private final Socket serverSocket = new Socket ();

  private final String serverURL;
  private final int clientPort;
  private final int serverPort;
  private boolean prevent3270E;

  private ServerSocket clientServerSocket;
  private TelnetSocket clientTelnetSocket;
  private TelnetSocket serverTelnetSocket;
  private final Session session;

  public SpyServer (String serverURL, int serverPort, int clientPort, Session session)
  {
    if (serverURL == null || serverURL.isEmpty ())
      throw new IllegalArgumentException ("Server cannot be null or empty");
    if (serverPort <= 0)
      throw new IllegalArgumentException ("Server Port must be a positive integer");
    if (clientPort <= 0)
      throw new IllegalArgumentException ("Client Port must be a positive integer");
    if (session == null)
      throw new IllegalArgumentException ("Session cannot be null");

    this.serverURL = serverURL;
    this.serverPort = serverPort;
    this.clientPort = clientPort;
    this.session = session;
  }

  public void prevent3270E (boolean value)
  {
    prevent3270E = value;
  }

  public Session getSession ()
  {
    return session;
  }

  public void writeToClientSocket (byte[] buffer)
  {
    serverTelnetSocket.listen (buffer, false);  // not genuine
    clientTelnetSocket.write (buffer, true);    // skip the next reply
  }

  @Override
  public void run ()
  {
    try
    {
      // wait for a tn3270 client to connect to us
      clientServerSocket = new ServerSocket (clientPort);
      clientSocket = clientServerSocket.accept ();      // blocks

      // leave a message on the terminal screen while we connect to the MF
      String message = "Connecting to " + serverURL + ":" + serverPort;
      OutputStream clientOut = clientSocket.getOutputStream ();
      clientOut.write (message.getBytes ("ASCII"));
      clientOut.flush ();

      // now connect to the real mainframe
      serverSocket.connect (new InetSocketAddress (serverURL, serverPort));

      // create two SocketListeners and link them to each other
      clientTelnetSocket =
          new TelnetSocket (Source.CLIENT, clientSocket, new TelnetListener (
              Source.CLIENT, session));
      serverTelnetSocket =
          new TelnetSocket (Source.SERVER, serverSocket, new TelnetListener (
              Source.SERVER, session));

      serverTelnetSocket.link (clientTelnetSocket);
      clientTelnetSocket.link (serverTelnetSocket);

      // stop the session from using tn3270E mode
      serverTelnetSocket.prevent3270E (prevent3270E);

      // start up the two listeners, each in its own thread 
      new Thread (clientTelnetSocket).start ();
      new Thread (serverTelnetSocket).start ();

      // we're done now, not waiting for any more connections
      clientServerSocket.close ();
      clientServerSocket = null;
    }
    catch (SocketException e)     // caused by closing the clientServerSocket
    {
      System.out.println ("tata");
    }
    catch (IOException e)
    {
      e.printStackTrace ();
      close ();
    }

    System.out.println ("SpyServer closed");
  }

  public TelnetSocket getListener (Source source)
  {
    return source == Source.CLIENT ? clientTelnetSocket : serverTelnetSocket;
  }

  public void close ()
  {
    if (clientServerSocket != null)
      try
      {
        clientServerSocket.close ();
        clientServerSocket = null;
      }
      catch (IOException e)
      {
        e.printStackTrace ();
      }

    if (clientTelnetSocket != null)
    {
      clientTelnetSocket.close ();
      clientTelnetSocket = null;
    }

    if (serverTelnetSocket != null)
    {
      serverTelnetSocket.close ();
      serverTelnetSocket = null;
    }
  }
}