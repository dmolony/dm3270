package com.bytezone.dm3270.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import com.bytezone.dm3270.application.Mainframe;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.telnet.TelnetCommand;
import com.bytezone.dm3270.telnet.TelnetSubcommand;
import com.bytezone.dm3270.telnet.TerminalTypeSubcommand;

import javafx.application.Platform;

public class MainframeServer implements Runnable
{
  private final int port;
  private final byte[] buffer = new byte[4096];
  private byte[] tempBuffer;
  private volatile boolean running;

  private InputStream clientIn;
  private OutputStream clientOut;
  private ServerSocket clientServerSocket;
  private Socket clientSocket;

  private Mainframe mainframe;

  public MainframeServer (int port)
  {
    this.port = port;
  }

  public void setStage (Mainframe mainframe)
  {
    this.mainframe = mainframe;     // MainframeStage
  }

  @Override
  public void run ()
  {
    try
    {
      clientServerSocket = new ServerSocket (port);     // usually 5555
      clientSocket = clientServerSocket.accept ();      // blocks

      clientIn = clientSocket.getInputStream ();
      clientOut = clientSocket.getOutputStream ();

      writeAll (TelnetCommand.IAC, TelnetCommand.DO, TelnetSubcommand.TERMINAL_TYPE);
      readAtLeast (1);

      writeAll (TelnetCommand.IAC, TelnetCommand.SB, TelnetSubcommand.TERMINAL_TYPE,
                 TerminalTypeSubcommand.OPTION_SEND, TelnetCommand.IAC, TelnetCommand.SE);
      readAtLeast (1);

      writeAll (TelnetCommand.IAC, TelnetCommand.DO, TelnetSubcommand.EOR);
      writeAll (TelnetCommand.IAC, TelnetCommand.WILL, TelnetSubcommand.EOR);
      readAtLeast (6);

      writeAll (TelnetCommand.IAC, TelnetCommand.DO, TelnetSubcommand.BINARY);
      writeAll (TelnetCommand.IAC, TelnetCommand.WILL, TelnetSubcommand.BINARY);
      readAtLeast (6);

      // send Query to find out what the terminal supports
      byte[] cmd = { (byte) 0xF3, 0x00, 0x06, 0x40, 0x00,    //
                     (byte) 0xF1, (byte) 0xC0, 0x00, 0x05,   // note WCC = 0xC0
                     0x01, (byte) 0xFF, (byte) 0xFF,         // note double FF
                     0x02, (byte) 0xFF, (byte) 0xEF };

      write (cmd);

      running = true;
      while (running)
      {
        if (Thread.interrupted ())
        {
          System.out.println ("MainframeServer interrupted");
          break;
        }
        int bytesRead = clientIn.read (buffer);     // assumes all in one buffer !!
        if (mainframe != null && buffer[0] != TelnetCommand.IAC)
        {
          bytesRead = sanitise (buffer, bytesRead);       // remove 0xFF bytes
          Command command = Command.getReply (buffer, 0, bytesRead);
          Platform.runLater ( () -> mainframe.receiveCommand (command));
        }
      }
    }
    catch (SocketException e)     // caused by closing the clientServerSocket
    {
      System.out.println ("Connection attempt cancelled");
    }
    catch (IOException e)
    {
      e.printStackTrace ();
      close ();
    }

    System.out.println ("Mainframe Server closed");
  }

  private int sanitise (byte[] buffer, int bytesRead)
  {
    if (tempBuffer != null)
    {
      // prepend it to buffer
      byte[] newBuffer = new byte[tempBuffer.length + bytesRead];
      System.arraycopy (tempBuffer, 0, newBuffer, 0, tempBuffer.length);
      System.arraycopy (buffer, 0, newBuffer, tempBuffer.length, bytesRead);

      tempBuffer = null;
      buffer = newBuffer;
      bytesRead = newBuffer.length;
    }

    if (buffer[bytesRead - 1] != (byte) 0xEF && buffer[bytesRead - 2] != (byte) 0xFF)
    {
      System.out.println ("Unfinished buffer");
      tempBuffer = new byte[bytesRead];
      System.arraycopy (buffer, 0, tempBuffer, 0, bytesRead);
      return 0;
    }

    bytesRead -= 2;                               // ignore the 0xFF 0xEF at the end
    byte lastByte = 0;
    byte IAC = (byte) 0xFF;
    int ptr = 0;

    for (int i = 0; i < bytesRead; i++)
    {
      if (ptr != i)
        buffer[ptr] = buffer[i];

      if (buffer[i] == IAC & lastByte == IAC)     // doubled-up 0xFF
        lastByte = 0;                             // don't flag it again
      else
      {
        ptr++;
        lastByte = buffer[i];
      }
    }

    return ptr;
  }

  private void readAtLeast (int bytesToRead) throws IOException
  {
    while (bytesToRead > 0)
      bytesToRead -= clientIn.read (buffer);      // blocks
  }

  private void writeAll (byte... buffer) throws IOException
  {
    write (buffer);
  }

  public void write (byte[] buffer)
  {
    if (clientOut != null)
    {
      try
      {
        clientOut.write (buffer);
        clientOut.flush ();
      }
      catch (IOException e)
      {
        e.printStackTrace ();
      }
    }
  }

  public void sendCommand (Command command)
  {
    byte[] buffer = command.getTelnetData ();
    write (buffer);
  }

  public void close ()
  {
    running = false;

    if (clientSocket != null)
      try
      {
        clientSocket.close ();
        clientSocket = null;
      }
      catch (IOException e)
      {
        e.printStackTrace ();
      }

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
  }
}