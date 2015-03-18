package com.bytezone.dm3270.telnet;

import java.io.UnsupportedEncodingException;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.streams.TelnetState;

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

  // state variables
  private final byte[] data = new byte[1024];
  private int dataPtr;
  private boolean pending;
  private boolean weirdData;
  private byte command;

  private final TelnetState telnetState = new TelnetState ();

  public void test (byte[] buffer)
  {
    for (byte b : buffer)
      listen (b);
  }

  public void listen (byte... buffer)
  {
    for (byte thisByte : buffer)
    {
      data[dataPtr++] = thisByte;

      if (thisByte == IAC)
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

        // first check for a valid 3270 data record
        if (thisByte == EOR)
        {
          processRecord ();
          continue;
        }

        // next remove any non-telnet data
        if (data[0] != IAC || weirdData)    // some non-telnet data is in the buffer
        {
          dataPtr -= 2;                     // hide IAC and this byte
          processData ();                   // resets dataPtr

          data[dataPtr++] = IAC;            // drop through and process the new byte
          data[dataPtr++] = thisByte;
        }

        if (thisByte == SB)                 // leave IAC SB in buffer
          continue;

        if (thisByte == SE)
        {
          processSubcommand ();
          continue;
        }

        if (thisByte == DO || thisByte == DONT || thisByte == WILL | thisByte == WONT)
        {
          command = thisByte;
          continue;
        }

        if (thisByte == NOP)
        {
          dataPtr -= 2;                     // ignore IAC NOP
          System.out.println ("NOP\n");
          continue;
        }

        if (thisByte == IP)
        {
          // now what?
          reset ();
          continue;
        }

        System.err.println ("Unknown command");   // handle error somehow
      }
      else if (command != 0)
        processCommand (thisByte);
    }
  }

  private void reset ()
  {
    dataPtr = 0;
    command = 0;
    weirdData = false;
  }

  private void processData ()
  {
    System.out.println ("Data");
    System.out.println (Utility.toHex (data, 0, dataPtr, false));
    System.out.println ();

    reset ();
  }

  private void processCommand (byte b)
  {
    assert data[0] == IAC;
    assert data[1] == command;
    assert data[2] == b;

    TelnetCommand telnetCommand = new TelnetCommand (telnetState, data, dataPtr);

    System.out.println ("Command ");
    System.out.println (telnetCommand);

    System.out.println (Utility.toHex (data, 0, dataPtr, false));
    System.out.println ();

    reset ();
  }

  private void processSubcommand ()
  {
    assert data[0] == IAC;
    assert data[1] == SB;
    assert data[dataPtr - 2] == IAC;
    assert data[dataPtr - 1] == SE;

    byte subCommand = data[2];
    String text;
    TelnetSubcommand telnetSubcommand;

    if (subCommand == SB_TERMINAL_TYPE)
    {
      text = "TERMINAL_TYPE";
      telnetSubcommand = new TerminalTypeSubcommand (data, 0, dataPtr, telnetState);
    }
    else if (subCommand == SB_TN3270E)
    {
      text = "TN3270-E";
      telnetSubcommand = new TN3270ExtendedSubcommand (data, 0, dataPtr, telnetState);
    }
    else
    {
      text = "??";
      telnetSubcommand = null;
    }

    System.out.println ("Subcommand " + text);
    System.out.println (telnetSubcommand);

    System.out.println (Utility.toHex (data, 0, dataPtr, false));
    System.out.println ();

    reset ();
  }

  private void processRecord ()
  {
    assert data[dataPtr - 2] == IAC;
    assert data[dataPtr - 1] == EOR;

    System.out.println ("Record");
    System.out.println (Utility.toHex (data, 0, dataPtr - 2));
    System.out.println ();

    reset ();
  }

  public void flush ()
  {
    if (dataPtr > 0)
      processData ();
  }

  public static void main (String[] args)
  {
    TelnetTester tester = new TelnetTester ();
    String line = "-------------------------------------------------";

    tester.test (dataInTelnetFormat ());
    //    tester.flush ();

    System.out.println (line);
    tester.test (spy01 ());

    System.out.println (line);
    tester.test (helloWorld ());

    System.out.println (line);
    tester.test (spy05 ());
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

  static byte[] spy01 ()
  {
    return new byte[] { //
    IAC, DO, 0x18, IAC, WILL, 0x18, IAC, SB, 0x18, 0x01, IAC, SE, IAC, SB, 0x18, 0x00,
        0x49, 0x42, 0x4D, 0x2D, 0x33, 0x32, 0x37, 0x38, 0x2D, 0x32, 0x2D, 0x45, IAC, SE,
        IAC, DO, 0x19, IAC, WILL, 0x19, IAC, WILL, 0x19, IAC, DO, 0x19, IAC, DO, 0x00,
        IAC, WILL, 0x00, IAC, WILL, 0x00, IAC, DO, 0x00 };

  }

  static byte[] spy05 ()
  {
    return new byte[] { //
    IAC, DO, SB_TN3270E, IAC, WILL, SB_TN3270E, IAC, SB, SB_TN3270E, 0x08, 0x02, IAC, SE,
        IAC, SB, SB_TN3270E, 0x02, 0x07, 0x49, 0x42, 0x4D, 0x2D, 0x33, 0x32, 0x37, 0x38,
        0x2D, 0x32, 0x2D, 0x45, IAC, SE, IAC, SB, SB_TN3270E, 0x02, 0x04, 0x49, 0x42,
        0x4D, 0x2D, 0x33, 0x32, 0x37, 0x38, 0x2D, 0x32, 0x2D, 0x45, 0x01, 0x54, 0x43,
        0x50, 0x41, 0x30, 0x39, 0x32, 0x36, IAC, SE, IAC, SB, SB_TN3270E, 0x03, 0x07,
        0x00, 0x02, 0x04, IAC, SE, IAC, SB, SB_TN3270E, 0x03, 0x04, 0x00, 0x02, 0x04,
        IAC, SE, 0x03, 0x00, 0x00, 0x00, 0x00, 0x31, 0x01, 0x03, IAC, EOR, IAC, NOP,
        0x03, 0x00, 0x00, 0x00, 0x00, 0x31, 0x01, 0x03, IAC, EOR };
  }

  static byte[] dataInTelnetFormat ()
  {
    return new byte[] { IAC, IAC, DO, EOR };
  }
}