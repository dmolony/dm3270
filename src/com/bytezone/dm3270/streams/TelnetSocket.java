package com.bytezone.dm3270.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalDateTime;

import com.bytezone.dm3270.telnet.TelnetCommand;
import com.bytezone.dm3270.telnet.TelnetSubcommand;

public class TelnetSocket implements Runnable
{
  private static final boolean GENUINE = true;
  private static final boolean MITM = false;

  private final String name;
  private final Source source;

  private Socket socket;
  private InputStream inputStream;
  private OutputStream outputStream;

  private final byte[] buffer = new byte[4096];
  private int bytesRead;

  private final BufferListener telnetListener;
  private volatile boolean running;

  private TelnetSocket partner;
  private boolean prevent3270E;

  public enum Source
  {
    CLIENT, SERVER
  }

  // Only used by a SpyServer, which creates two SocketListeners. Each SocketListener
  // copies its inputStream to its partner's outputStream after sending a copy to
  // the listener.

  public TelnetSocket (Source source, Socket socket, BufferListener listener)
      throws IOException
  {
    if (socket == null)
      throw new IllegalArgumentException ("Socket cannot be null");
    if (listener == null)
      throw new IllegalArgumentException ("Listener cannot be null");

    this.name = source == Source.CLIENT ? "Client" : "Server";
    this.source = source;
    this.socket = socket;
    this.telnetListener = listener;

    this.inputStream = socket.getInputStream ();
    this.outputStream = socket.getOutputStream ();
  }

  public void link (TelnetSocket partner)
  {
    this.partner = partner;
  }

  public void prevent3270E (boolean value)
  {
    if (source != Source.SERVER)
      throw new IllegalStateException ("Only a SERVER listener can do that");
    prevent3270E = value;
  }

  @Override
  public void run ()
  {
    running = true;
    while (running)
    {
      try
      {
        bytesRead = inputStream.read (buffer);      // blocks
        if (bytesRead == -1)
        {
          System.out.println (name + " has no data on input stream");
          close ();
          return;
        }

        // take a copy of the input buffer and send it to the TelnetListener
        byte[] message = new byte[bytesRead];
        System.arraycopy (buffer, 0, message, 0, message.length);
        telnetListener.listen (source, message, LocalDateTime.now (), GENUINE);

        if (prevent3270E && fakeReplySent ())   // did we reject a request for 3270-E?
          continue;

        if (partner != null)
          partner.write (message);              // write to partner's OutputStream
      }
      catch (IOException e)
      {
        if (running)
          System.out.println (name + " closing due to IOException: " + e);
        else
          System.out.println (name + " quitting");
        close ();
        return;
      }
    }

    System.out.println (name + " closing - bye everyone");
    close ();
  }

  private void write (byte[] buffer)
  {
    try
    {
      outputStream.write (buffer);
      outputStream.flush ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }

  private boolean fakeReplySent () throws IOException
  {
    // If the server sends a request for us to DO 3270E and we don't want to, then
    // send a fake WONT reply instead of passing on the request.
    if (source == Source.SERVER                     //
        && bytesRead == 3                           //
        && buffer[0] == TelnetCommand.IAC           //
        && buffer[1] == TelnetCommand.DO            //
        && buffer[2] == TelnetSubcommand.TN3270E)
    {
      byte[] reply = new byte[3];

      reply[0] = TelnetCommand.IAC;
      reply[1] = TelnetCommand.WONT;
      reply[2] = TelnetSubcommand.TN3270E;

      write (reply);     // reply directly, don't send it to partner

      // send a ManInTheMiddle notification on behalf of the client
      telnetListener.listen (partner.source, reply, LocalDateTime.now (), MITM);

      return true;
    }
    return false;
  }

  public void close ()
  {
    running = false;

    try
    {
      if (socket != null)
        socket.close ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }

    socket = null;
    inputStream = null;
    outputStream = null;
  }
}