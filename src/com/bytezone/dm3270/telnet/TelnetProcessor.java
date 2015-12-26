package com.bytezone.dm3270.telnet;

public class TelnetProcessor
{
  // command prefix
  public static final byte IAC = (byte) 0xFF;

  // single-byte commands
  public static final byte EOR = (byte) 0xEF;   // End of record
  public static final byte SE = (byte) 0xF0;    // End of subcommmand
  public static final byte NOP = (byte) 0xF1;   // No Operation
  public static final byte IP = (byte) 0xF4;    // Interrupt process

  // double-byte commands
  public static final byte SB = (byte) 0xFA;    // Begin subcommand
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
  private final byte[] data = new byte[16500];      // see also SessionReader
  private int dataPtr;
  private boolean pending;              // last byte was IAC, must check next byte
  private boolean weirdData;            // when stream starts with two IACs
  private byte command;                 // one of DO, DONT, WILL, WONT

  // command processor
  private final TelnetCommandProcessor commandProcessor;

  public TelnetProcessor (TelnetCommandProcessor commandProcessor)
  {
    this.commandProcessor = commandProcessor;
  }

  public void test (byte[] buffer)
  {
    for (byte b : buffer)
      listen (b);
  }

  public void listen (byte... buffer)
  {
    for (byte thisByte : buffer)
    {
      data[dataPtr++] = thisByte;           // store every byte we receive

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
          commandProcessor.processRecord (data, dataPtr);
          reset ();
          continue;
        }

        // next remove any non-telnet data
        if (data[0] != IAC || weirdData)    // some non-telnet data is in the buffer
        {
          dataPtr -= 2;                     // hide IAC and this byte
          commandProcessor.processData (data, dataPtr);
          reset ();

          data[dataPtr++] = IAC;            // drop through and process the new byte
          data[dataPtr++] = thisByte;
        }

        if (thisByte == SB)                 // leave IAC SB in buffer
          continue;

        if (thisByte == SE)
        {
          commandProcessor.processTelnetSubcommand (data, dataPtr);
          reset ();
          continue;
        }

        // known three-byte commands
        if (thisByte == DO || thisByte == DONT || thisByte == WILL | thisByte == WONT)
        {
          command = thisByte;               // save it and wait for the third byte
          continue;
        }

        // known two-byte commands
        if (thisByte == NOP || thisByte == IP)
        {
          commandProcessor.processTelnetCommand (data, dataPtr);
          reset ();
          continue;
        }

        System.err.printf ("Unknown command: %02X%n", thisByte);   // handle error somehow
      }
      else if (command != 0)                // the third byte has arrived (in thisByte)
      {
        commandProcessor.processTelnetCommand (data, dataPtr);
        reset ();
      }
    }
  }

  private void reset ()
  {
    dataPtr = 0;
    command = 0;
    weirdData = false;
  }

  //  public static void main (String[] args)
  //  {
  //    TelnetProcessor tester = new TelnetProcessor (new Processor ());
  //    String line = "-------------------------------------------------";
  //
  //    tester.test (dataInTelnetFormat ());
  //    //    tester.flush ();
  //
  //    System.out.println (line);
  //    tester.test (spy01 ());
  //
  //    System.out.println (line);
  //    tester.test (helloWorld ());
  //
  //    System.out.println (line);
  //    tester.test (spy05 ());
  //  }

  //  static byte[] helloWorld ()
  //  {
  //    try
  //    {
  //      String terminalType = "Hello, World!";
  //      return terminalType.getBytes ("ASCII");
  //    }
  //    catch (UnsupportedEncodingException e)
  //    {
  //      e.printStackTrace ();
  //    }
  //    return null;
  //  }

  //  static byte[] spy01 ()
  //  {
  //    return new byte[] { IAC, DO, 0x18, IAC, WILL, 0x18, IAC, SB, 0x18, 0x01, IAC, SE, IAC,
  //                        SB, 0x18, 0x00, 0x49, 0x42, 0x4D, 0x2D, 0x33, 0x32, 0x37, 0x38,
  //                        0x2D, 0x32, 0x2D, 0x45, IAC, SE, IAC, DO, 0x19, IAC, WILL, 0x19,
  //                        IAC, WILL, 0x19, IAC, DO, 0x19, IAC, DO, 0x00, IAC, WILL, 0x00,
  //                        IAC, WILL, 0x00, IAC, DO, 0x00 };
  //
  //  }
  //
  //  static byte[] spy05 ()
  //  {
  //    return new byte[] { IAC, DO, SB_TN3270E, IAC, WILL, SB_TN3270E, IAC, SB, SB_TN3270E,
  //                        0x08, 0x02, IAC, SE, IAC, SB, SB_TN3270E, 0x02, 0x07, 0x49, 0x42,
  //                        0x4D, 0x2D, 0x33, 0x32, 0x37, 0x38, 0x2D, 0x32, 0x2D, 0x45, IAC,
  //                        SE, IAC, SB, SB_TN3270E, 0x02, 0x04, 0x49, 0x42, 0x4D, 0x2D, 0x33,
  //                        0x32, 0x37, 0x38, 0x2D, 0x32, 0x2D, 0x45, 0x01, 0x54, 0x43, 0x50,
  //                        0x41, 0x30, 0x39, 0x32, 0x36, IAC, SE, IAC, SB, SB_TN3270E, 0x03,
  //                        0x07, 0x00, 0x02, 0x04, IAC, SE, IAC, SB, SB_TN3270E, 0x03, 0x04,
  //                        0x00, 0x02, 0x04, IAC, SE, 0x03, 0x00, 0x00, 0x00, 0x00, 0x31,
  //                        0x01, 0x03, IAC, EOR, IAC, NOP, 0x03, 0x00, 0x00, 0x00, 0x00,
  //                        0x31, 0x01, 0x03, IAC, EOR };
  //  }

  //  static byte[] dataInTelnetFormat ()
  //  {
  //    return new byte[] { IAC, IAC, DO, EOR };
  //  }
}

//class Processor implements TelnetCommandProcessor
//{
//  TelnetState telnetState = new TelnetState ();
//
//  @Override
//  public void processData (byte[] data, int length)
//  {
//    System.out.println ("Data");
//    System.out.println (Dm3270Utility.toHex (data, 0, length, false));
//    System.out.println ();
//  }
//
//  @Override
//  public void processRecord (byte[] data, int length)
//  {
//    assert data[length - 2] == TelnetProcessor.IAC;
//    assert data[length - 1] == TelnetProcessor.EOR;
//
//    System.out.println ("Record");
//    System.out.println (Dm3270Utility.toHex (data, 0, length - 2));
//    System.out.println ();
//  }
//
//  @Override
//  public void processTelnetCommand (byte[] data, int length)
//  {
//    assert data[0] == TelnetProcessor.IAC;
//
//    TelnetCommand telnetCommand = new TelnetCommand (telnetState, data, length);
//
//    System.out.println ("Command ");
//    System.out.println (telnetCommand);
//
//    System.out.println (Dm3270Utility.toHex (data, 0, length, false));
//    System.out.println ();
//  }
//
//  @Override
//  public void processTelnetSubcommand (byte[] data, int length)
//  {
//    assert data[0] == TelnetProcessor.IAC;
//    assert data[1] == TelnetProcessor.SB;
//    assert data[length - 2] == TelnetProcessor.IAC;
//    assert data[length - 1] == TelnetProcessor.SE;
//
//    byte subCommand = data[2];
//    String text;
//    TelnetSubcommand telnetSubcommand;
//
//    if (subCommand == TelnetProcessor.SB_TERMINAL_TYPE)
//    {
//      text = "TERMINAL_TYPE";
//      telnetSubcommand = new TerminalTypeSubcommand (data, 0, length, telnetState);
//    }
//    else if (subCommand == TelnetProcessor.SB_TN3270E)
//    {
//      text = "TN3270-E";
//      telnetSubcommand = new TN3270ExtendedSubcommand (data, 0, length, telnetState);
//    }
//    else
//    {
//      text = "??";
//      telnetSubcommand = null;
//    }
//
//    System.out.println ("Subcommand " + text);
//    System.out.println (telnetSubcommand);
//
//    System.out.println (Dm3270Utility.toHex (data, 0, length, false));
//    System.out.println ();
//
//  }
//}