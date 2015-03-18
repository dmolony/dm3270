package com.bytezone.dm3270.telnet;

import java.io.UnsupportedEncodingException;

import com.bytezone.dm3270.application.Utility;

public class TelnetTester
{
  // command prefix
  public static final byte IAC = (byte) 0xFF;

  // single-byte commands
  public static final byte EOR = (byte) 0xEF;   // End of record
  public static final byte SE = (byte) 0xF0;    // End of subcommmand
  public static final byte NOP = (byte) 0xF1;   // No Operation
  public static final byte IP = (byte) 0xF4;    // Interrupt process
  public static final byte SB = (byte) 0xFA;    // Begin subcommand

  // double-byte commands
  public static final byte WILL = (byte) 0xFB;
  public static final byte WONT = (byte) 0xFC;
  public static final byte DO = (byte) 0xFD;
  public static final byte DONT = (byte) 0xFE;

  // subcommands
  public static final byte SB_BINARY = 0x00;
  public static final byte SB_TERMINAL_TYPE = 0x18;
  public static final byte SB_EOR = 0x19;
  public static final byte SB_TN3270E = 0x28;

  public static final byte OPTION_IS = 0;
  public static final byte OPTION_SEND = 1;

  // testing streams
  static byte[] t1 = { IAC, DO, SB_EOR };
  static byte[] t2 = { IAC, DONT, SB_TN3270E };
  static byte[] t3 = { IAC, NOP };
  static byte[] t4 = { IAC, SB, SB_TN3270E, 0x03, 0x07, 0x00, 0x02, 0x04, IAC, SE };
  static byte[] t5 = { (byte) 0xC1, (byte) 0xC2, IAC, EOR };

  private final byte[] data = new byte[1024];
  private int dataPtr;
  private boolean pending;
  private boolean weirdData;

  private byte command;

  public void test (byte[] buffer)
  {
    for (byte b : buffer)
      listen (b);
  }

  public void listen (byte... buffer)
  {
    for (byte b : buffer)
    {
      System.out.printf ("Byte %02X%n", b);
      data[dataPtr++] = b;

      if (b == IAC)
      {
        if (pending)                        // previous byte might have been an IAC
        {
          pending = false;                  // treat it as a data 0xFF
          --dataPtr;                        // remove the second one
          if (dataPtr == 1)                 // if there is just that data 0xFF in the
            weirdData = true;               // buffer, then flag it
        }
        else
          pending = true;                   // this byte might be an IAC
        continue;
      }

      if (pending)                          // previous byte really was an IAC
      {
        pending = false;

        if (b == EOR)
        {
          processRecord ();
          continue;
        }

        if (data[0] != IAC || weirdData)    // some non-telnet data is in the buffer
        {
          dataPtr -= 2;                     // remove IAC and this byte
          processData ();
          data[dataPtr++] = IAC;            // drop through and process the new byte
          data[dataPtr++] = b;
        }

        if (b == NOP)
        {
          dataPtr -= 2;                     // ignore IAC NOP
          System.out.println ("NOP\n");
          continue;
        }

        if (b == IP)
        {
          // now what?
          dataPtr = 0;
          continue;
        }

        if (b == SB)                        // leave IAC SB in buffer
          continue;

        if (b == SE)
        {
          processSubcommand ();
          continue;
        }

        if (b == DO || b == DONT || b == WILL | b == WONT)
        {
          command = b;
          continue;
        }
        //        else
        //          System.out.println ("Unknown command");   // handle error somehow
      }
      else
      {
        if (command != 0)
          processCommand (b);
      }
    }
  }

  private void processData ()
  {
    System.out.println ("Data");
    System.out.println (Utility.toHex (data, 0, dataPtr, false));
    System.out.println ();

    dataPtr = 0;
    command = 0;
    weirdData = false;
  }

  private void processCommand (byte b)
  {
    assert data[0] == IAC;
    assert data[1] == command;
    assert data[2] == b;

    System.out.print ("Command ");
    System.out.printf ("%02X %02X%n", command, b);

    String text;
    if (command == DO)
      text = "DO";
    else if (command == DONT)
      text = "DONT";
    else if (command == WILL)
      text = "WILL";
    else if (command == WONT)
      text = "WONT";
    else
      text = "??";

    String text2;
    if (b == SB_EOR)
      text2 = "EOR";
    else if (b == SB_BINARY)
      text2 = "BINARY";
    else if (b == SB_TERMINAL_TYPE)
      text2 = "TERMINAL_TYPE";
    else if (b == SB_TN3270E)
      text2 = "TN3270E";
    else
      text2 = "??";

    System.out.println (text + " " + text2);
    System.out.println ();

    dataPtr = 0;
    command = 0;
    weirdData = false;
  }

  private void processSubcommand ()
  {
    assert data[0] == IAC;
    assert data[1] == SB;
    assert data[dataPtr - 2] == IAC;
    assert data[dataPtr - 1] == SE;

    String text;
    byte subCommand = data[2];
    if (subCommand == SB_TERMINAL_TYPE)
      text = "TERMINAL_TYPE";
    else if (subCommand == SB_TN3270E)
      text = "TN3270-E";
    else
      text = "??";

    System.out.println ("Subcommand " + text);
    System.out.println (Utility.toHex (data, 0, dataPtr, false));
    System.out.println ();

    dataPtr = 0;
    command = 0;
    weirdData = false;
  }

  private void processRecord ()
  {
    assert data[dataPtr - 2] == IAC;
    assert data[dataPtr - 1] == EOR;

    System.out.println ("Record");
    System.out.println (Utility.toHex (data, 0, dataPtr - 2));
    System.out.println ();

    dataPtr = 0;
    command = 0;
    weirdData = false;
  }

  public void flush ()
  {
    if (dataPtr > 0)
      processData ();
  }

  public static void main (String[] args)
  {
    TelnetTester tester = new TelnetTester ();

    tester.test (t1);
    tester.test (t2);
    tester.test (isTerminalType ());
    tester.test (t3);
    tester.test (helloWorld ());
    tester.test (t1);
    tester.test (t5);
    tester.test (dataInTelnetFormat ());
    tester.test (t4);
    tester.test (helloWorld ());

    tester.flush ();
  }

  static byte[] helloWorld ()
  {
    try
    {
      String terminalType = "Hello, World!";
      return terminalType.getBytes ("ASCII");
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }
    return null;
  }

  static byte[] isTerminalType ()
  {
    try
    {
      String terminalType = "IBM-3278-2-E";
      byte[] header = { IAC, SB, SB_TERMINAL_TYPE, OPTION_IS };
      byte[] terminal = terminalType.getBytes ("ASCII");
      byte[] reply = new byte[header.length + terminal.length + 2];

      System.arraycopy (header, 0, reply, 0, header.length);
      System.arraycopy (terminal, 0, reply, header.length, terminal.length);
      reply[reply.length - 2] = TelnetCommand.IAC;
      reply[reply.length - 1] = TelnetCommand.SE;
      return reply;
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }
    return null;
  }

  static byte[] dataInTelnetFormat ()
  {
    return new byte[] { IAC, IAC, DO, EOR };
  }
}