package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.buffers.AbstractTN3270Command;
import com.bytezone.dm3270.utilities.Dm3270Utility;

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
   * 
   *    process ()  : draws on the screen
   *    getReply () : null
   *    
   * 2) Erase All Unprotected
   * 
   *    process ()  : draws on the screen
   *    getReply () : null 
   * 
   * 3) Read Buffer, Read Modifed, Read Modified All
   *    Sent when the application wants to know about the screen fields
   *    
   *    process ()  : creates a Reply
   *    getReply () : AID command
   * 
   * 4) Write Structured Field
   *    Contains one or more structured fields.
   *    1) Outbound3270DS  - one of W/EW/EWA/EAU
   *    2) ReadPartitionSF - one of RB/RM/RMA, or a Query command
   *    3) SetReplyModeSF  - field/extended field/character mode
   *    4) EraseResetSF
   *    5) FileTransferOutbound - IND$FILE
   *    
   *    process ()  : calls process () on each command
   *    getReply () : calls getReply () on each command
   * 
   * Reply Types
   * 
   * 1) RB/RM/RMA returns an AID
   * 2) Query command returns an 88 (list of replies)
   *   
   */

  /*
   * RMA is the same as RM except that RMA causes modified fields to be read even 
   * when an RM would only perform a Short Read, such as when CLEAR or a PA key is 
   * used. A Short Read results in only the AID being sent inbound to the host.
   */

  // called by TelnetListener.processRecord()
  // called by Outbound3270DS constructor
  public static Command getCommand (byte[] buffer, int offset, int length)
  {
    switch (buffer[offset])
    {
      case Command.WRITE_F1:
      case Command.WRITE_01:
      case Command.ERASE_WRITE_F5:
      case Command.ERASE_WRITE_05:
      case Command.ERASE_WRITE_ALTERNATE_7E:
      case Command.ERASE_WRITE_ALTERNATE_0D:
        return new WriteCommand (buffer, offset, length);

      case Command.ERASE_ALL_UNPROTECTED_6F:
      case Command.ERASE_ALL_UNPROTECTED_0F:
        return new EraseAllUnprotectedCommand (buffer, offset, length);

      case Command.READ_BUFFER_F2:
      case Command.READ_BUFFER_02:
      case Command.READ_MODIFIED_F6:
      case Command.READ_MODIFIED_06:
      case Command.READ_MODIFIED_ALL_6E:
      case Command.READ_MODIFIED_ALL_0E:
        return new ReadCommand (buffer, offset, length);

      case Command.WRITE_STRUCTURED_FIELD_F3:
      case Command.WRITE_STRUCTURED_FIELD_11:
        return new WriteStructuredFieldCommand (buffer, offset, length);

      default:
        System.out
            .println ("Unknown 3270 Command: " + String.format ("%02X", buffer[offset]));
        System.out.println (Dm3270Utility.toHex (buffer, offset, length));
        Dm3270Utility.printStackTrace ();
        return null;
    }
  }

  public abstract String getName ();

  // called by TelnetListener.processRecord()
  public static Command getReply (byte[] buffer, int offset, int length)
  {
    switch (buffer[offset])
    {
      case AIDCommand.AID_STRUCTURED_FIELD:
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