package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.buffers.AbstractTN3270Command;
import com.bytezone.dm3270.utilities.Utility;

public abstract class Command extends AbstractTN3270Command
{
  // EBCDIC codes (SNA) - Remote Attachment
  public final static byte WRITE_F1 = (byte) 0xF1;
  public final static byte ERASE_WRITE_F5 = (byte) 0xF5;
  public final static byte ERASE_WRITE_ALTERNATE_7E = 0x7E;
  public final static byte ERASE_ALL_UNPROTECTED_6F = 0x6F;
  public final static byte WRITE_STRUCTURED_FIELD_F3 = (byte) 0xF3;

  public final static byte READ_BUFFER_F2 = (byte) 0xF2;
  public final static byte READ_MODIFIED_F6 = (byte) 0xF6;
  public final static byte READ_MODIFIED_ALL_6E = 0x6E;

  // CCW opcodes (EXCP) - Local Attachment
  public final static byte WRITE_01 = 0x01;
  public final static byte ERASE_WRITE_05 = 0x05;
  public final static byte ERASE_WRITE_ALTERNATE_0D = 0x0D;
  public final static byte ERASE_ALL_UNPROTECTED_0F = 0x0F;
  public final static byte WRITE_STRUCTURED_FIELD_11 = 0x11;

  public final static byte READ_BUFFER_02 = 0x02;
  public final static byte READ_MODIFIED_06 = 0x06;
  public final static byte READ_MODIFIED_ALL_0E = 0x0E;

  // Reply code
  public final static byte READ_STRUCTURED_FIELD_88 = (byte) 0x88;

  /*
   * Command Description
   * 
   * Commands can be created from a byte array (either inbound, or outbound when
   * in Replay mode), or from scratch (outbound when building a Reply message, or
   * to send the user's data back to the MF).
   * 
   * Commands can be created from a byte stream (Inbound), or in response to a
   * request from the application.
   * Each command must be processed in order to create its reply, or to take some 
   * other action (like drawing to the screen).
   * 
   * Commands must not create replies in their constructor as this
   * could lead to a stack overflow.
   * 
   * Commands must be able to be processed more than once (due to the replay function).
   * 
   * Command Types
   * 
   * 1) Write, Erase Write, Erase Write Alternate
   *    process()   : draws on the screen
   *    getReply () : null
   * 
   * 2) Read Buffer, Read Modifed, Read Modified All
   *    Sent when the application wants to know about the screen fields.
   *    process()   : creates a Reply
   *    getReply () : AID command
   * 
   * 3) Write Structured Field
   *    Contains one or more commands, including W/EW/EWA and RB/RM/RMA embedded
   *    as an Outbound3270DS command. This command starts with 0x40 and the
   *    Partition ID, followed by the command in its usual format.
   *    process() : calls process () on each command
   *    
   *    Other commands are Read Partition (Query) which expects a Read Structured 
   *    Field (0x88) containing several Query Reply records as the response.
   * 
   * 4) AID (User command)    (AID ENTR/PFxx/PAx/CLR)
   * 
   * 5) Read Partition        (AID 0x61)
   * 
   * 6) Read Structured Field (AID 0x88)
   *    Used to send a reply consisting of several QueryReply records (in 
   *    response to QueryReply (List)). Used to tell the MF what features
   *    the terminal supports.
   *   
   */

  /*
   * RMA is the same as RM except that RMA causes modified fields to be read even 
   * when an RM would only perform a Short Read, such as when CLEAR or a PA key is 
   * used. A Short Read results in only the AID being sent inbound to the host.
   */

  public static Command getCommand (byte[] buffer, int offset, int length)
  {
    switch (buffer[offset])
    {
      case Command.WRITE_F1:
      case Command.WRITE_01:
        return new WriteCommand (buffer, offset, length, false);

      case Command.ERASE_WRITE_F5:
      case Command.ERASE_WRITE_05:
      case Command.ERASE_WRITE_ALTERNATE_7E:
      case Command.ERASE_WRITE_ALTERNATE_0D:
        return new WriteCommand (buffer, offset, length, true);

      case Command.WRITE_STRUCTURED_FIELD_F3:
      case Command.WRITE_STRUCTURED_FIELD_11:
        return new WriteStructuredFieldCommand (buffer, offset, length);

      case Command.ERASE_ALL_UNPROTECTED_6F:
      case Command.ERASE_ALL_UNPROTECTED_0F:
        return new EraseAllUnprotectedCommand (buffer, offset, length);

      case Command.READ_BUFFER_F2:
      case Command.READ_BUFFER_02:
      case Command.READ_MODIFIED_F6:
      case Command.READ_MODIFIED_06:
      case Command.READ_MODIFIED_ALL_6E:
      case Command.READ_MODIFIED_ALL_0E:
        // the ReadCommand creates a command with a reply of: AID, RM, RMA
        return new ReadCommand (buffer, offset, length);

      default:
        System.out
            .println ("Unknown 3270 Command: " + String.format ("%02X", buffer[offset]));
        System.out.println (Utility.toHex (buffer, offset, length));
        Utility.printStackTrace ();
        return null;
    }
  }

  public abstract String getName ();

  public static Command getReply (byte[] buffer, int offset, int length)
  {
    switch (buffer[offset])
    {
      case READ_STRUCTURED_FIELD_88:
        return new ReadStructuredFieldCommand (buffer, offset, length);

      default:
        return new AIDCommand (buffer, offset, length);
    }
  }

  public Command (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);
  }

  public Command ()
  {
    super ();
  }

  // reduce toString() to a single line
  public String brief ()
  {
    String longText = toString ();
    int ptr = longText.indexOf ('\n');
    return ptr > 0 ? longText.substring (0, ptr) : longText;
  }
}