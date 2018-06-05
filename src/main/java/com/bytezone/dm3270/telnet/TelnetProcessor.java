package com.bytezone.dm3270.telnet;

public class TelnetProcessor {

  // single-byte commands
  public static final byte EOR = (byte) 0xEF;   // End of record

  // command prefix
  private static final byte IAC = (byte) 0xFF;

  // single-byte commands
  private static final byte SE = (byte) 0xF0;    // End of subcommmand
  private static final byte NOP = (byte) 0xF1;   // No Operation
  private static final byte IP = (byte) 0xF4;    // Interrupt process

  // double-byte commands
  private static final byte SB = (byte) 0xFA;    // Begin subcommand
  private static final byte WILL = (byte) 0xFB;
  private static final byte WONT = (byte) 0xFC;
  private static final byte DO = (byte) 0xFD;
  private static final byte DONT = (byte) 0xFE;

  // state variables
  private final byte[] data = new byte[16500];      // see also SessionReader
  private int dataPtr;
  private boolean pending;              // last byte was IAC, must check next byte
  private boolean weirdData;            // when stream starts with two IACs
  private byte command;                 // one of DO, DONT, WILL, WONT

  // command processor
  private final TelnetCommandProcessor commandProcessor;

  public TelnetProcessor(TelnetCommandProcessor commandProcessor) {
    this.commandProcessor = commandProcessor;
  }

  public void listen(byte... buffer) {
    for (byte thisByte : buffer) {
      data[dataPtr++] = thisByte;           // store every byte we receive

      if (thisByte == IAC) {
        // previous byte might have been an IAC
        if (pending) {
          pending = false;                  // treat it as a data 0xFF
          --dataPtr;                        // remove the second one
          // if there is just that data 0xFF in the
          if (dataPtr == 1) {
            weirdData = true;               // buffer, then flag it
          }
        } else {
          pending = true;                   // this byte might be an IAC
        }
        continue;
      }

      // previous byte really was an IAC
      if (pending) {
        pending = false;

        // first check for a valid 3270 data record
        if (thisByte == EOR) {
          commandProcessor.processRecord(data, dataPtr);
          reset();
          continue;
        }

        // next remove any non-telnet data
        // some non-telnet data is in the buffer
        if (data[0] != IAC || weirdData) {
          dataPtr -= 2;                     // hide IAC and this byte
          commandProcessor.processData(data, dataPtr);
          reset();

          data[dataPtr++] = IAC;            // drop through and process the new byte
          data[dataPtr++] = thisByte;
        }

        // leave IAC SB in buffer
        if (thisByte == SB) {
          continue;
        }

        if (thisByte == SE) {
          commandProcessor.processTelnetSubcommand(data, dataPtr);
          reset();
          continue;
        }

        // known three-byte commands
        if (thisByte == DO || thisByte == DONT || thisByte == WILL | thisByte == WONT) {
          command = thisByte;               // save it and wait for the third byte
          continue;
        }

        // known two-byte commands
        if (thisByte == NOP || thisByte == IP) {
          commandProcessor.processTelnetCommand(data, dataPtr);
          reset();
          continue;
        }

        System.err.printf("Unknown command: %02X%n", thisByte);   // handle error somehow
        // the third byte has arrived (in thisByte)
      } else if (command != 0) {
        commandProcessor.processTelnetCommand(data, dataPtr);
        reset();
      }
    }
  }

  private void reset() {
    dataPtr = 0;
    command = 0;
    weirdData = false;
  }

}
