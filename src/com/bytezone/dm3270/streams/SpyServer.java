package com.bytezone.dm3270.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import com.bytezone.dm3270.application.Console;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.session.Session;
import com.bytezone.dm3270.streams.TelnetSocket.Source;
import com.bytezone.dm3270.utilities.Site;

public class SpyServer implements Runnable
{
  private Socket clientSocket;
  private final Socket serverSocket = new Socket ();
  private final TelnetState telnetState;

  private final String serverURL;
  private final int clientPort;
  private final int serverPort;
  private boolean prevent3270E;

  private ServerSocket clientServerSocket;
  private TelnetSocket clientTelnetSocket;
  private TelnetSocket serverTelnetSocket;
  private final Session session;
  private Screen screen;

  public SpyServer (Site server, int clientPort, Session session, TelnetState telnetState)
  {
    if (server == null)
      throw new IllegalArgumentException ("Server cannot be null or empty");
    if (clientPort <= 0)
      throw new IllegalArgumentException ("Client Port must be a positive integer");
    if (session == null)
      throw new IllegalArgumentException ("Session cannot be null");

    serverURL = server.getURL ();
    serverPort = server.getPort ();
    this.clientPort = clientPort;
    this.session = session;
    this.telnetState = telnetState;
  }

  private void prevent3270E (boolean value)
  {
    prevent3270E = value;
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
              Source.CLIENT, session, Console.Function.SPY, screen, telnetState));
      serverTelnetSocket =
          new TelnetSocket (Source.SERVER, serverSocket, new TelnetListener (
              Source.SERVER, session, Console.Function.SPY, screen, telnetState));

      // TelnetSocket.link() will connect both sockets to each other (bidirectional)
      serverTelnetSocket.link (clientTelnetSocket);

      // stop the session from using tn3270E mode
      System.out.printf ("Prevent 3270E: %s%n", prevent3270E);
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
  }

  public TelnetSocket getListener (Source source)
  {
    return source == Source.CLIENT ? clientTelnetSocket : serverTelnetSocket;
  }

  public void setScreen (Screen screen)
  {
    this.screen = screen;
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