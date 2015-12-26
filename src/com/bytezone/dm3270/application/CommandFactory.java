package com.bytezone.dm3270.application;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.structuredfields.StructuredField;
import com.bytezone.dm3270.telnet.TelnetCommand;
import com.bytezone.dm3270.utilities.Dm3270Utility;

class CommandFactory
{
  boolean direct = true;

  protected byte[] createReadBufferCommand (byte command)
  {
    return direct ? createDirectReadBufferCommand (command)
        : createIndirectReadBufferCommand (command);
  }

  private byte[] createDirectReadBufferCommand (byte command)
  {
    int ptr = 0;
    byte[] buffer = new byte[3];

    buffer[ptr++] = command;
    ptr = terminateBuffer (buffer, ptr);

    return buffer;
  }

  private byte[] createIndirectReadBufferCommand (byte command)
  {
    int ptr = 0;
    byte[] buffer = new byte[8];

    buffer[ptr++] = Command.WRITE_STRUCTURED_FIELD_F3;
    ptr = Dm3270Utility.packUnsignedShort (buffer.length - 3, buffer, ptr);
    buffer[ptr++] = StructuredField.READ_PARTITION;         // 0x01
    buffer[ptr++] = 0x00;                                   // partition 0
    buffer[ptr++] = command;                                // RB-F2/RM-F6/RMA-6E
    ptr = terminateBuffer (buffer, ptr);

    return buffer;
  }

  protected byte[] createSetReplyModeCommand (byte mode)
  {
    int ptr = 0;
    byte[] buffer = new byte[mode == 2 ? 13 : 8];

    buffer[ptr++] = Command.WRITE_STRUCTURED_FIELD_F3;
    ptr = Dm3270Utility.packUnsignedShort (buffer.length - 3, buffer, ptr);
    buffer[ptr++] = StructuredField.SET_REPLY_MODE;
    buffer[ptr++] = 0x00;                                   // partition 0
    buffer[ptr++] = mode;                                   // reply mode

    if (mode == 2)
    {
      buffer[ptr++] = Attribute.XA_HIGHLIGHTING;
      buffer[ptr++] = Attribute.XA_FGCOLOR;
      buffer[ptr++] = Attribute.XA_CHARSET;
      buffer[ptr++] = Attribute.XA_BGCOLOR;
      buffer[ptr++] = Attribute.XA_TRANSPARENCY;
    }

    ptr = terminateBuffer (buffer, ptr);

    return buffer;
  }

  protected byte[] createProgramTabCommand1 ()
  {
    int ptr = 0;
    byte[] buffer = new byte[6];

    buffer[ptr++] = Command.WRITE_01;
    buffer[ptr++] = (byte) 0xC3;                          // wcc
    buffer[ptr++] = Order.PROGRAM_TAB;
    buffer[ptr++] = (byte) 0xC1;
    ptr = terminateBuffer (buffer, ptr);

    return buffer;
  }

  protected byte[] createProgramTabCommand2 ()
  {
    int ptr = 0;
    byte[] buffer = new byte[7];

    buffer[ptr++] = Command.WRITE_01;
    buffer[ptr++] = (byte) 0xC3;                          // wcc
    buffer[ptr++] = Order.PROGRAM_TAB;
    buffer[ptr++] = (byte) 0xC1;
    buffer[ptr++] = Order.PROGRAM_TAB;
    ptr = terminateBuffer (buffer, ptr);

    return buffer;
  }

  protected byte[] createProgramTabCommand3 ()
  {
    int ptr = 0;
    byte[] buffer = new byte[7];

    buffer[ptr++] = Command.WRITE_01;
    buffer[ptr++] = (byte) 0xC3;                          // wcc
    buffer[ptr++] = Order.PROGRAM_TAB;
    buffer[ptr++] = Order.PROGRAM_TAB;
    buffer[ptr++] = (byte) 0xC1;
    ptr = terminateBuffer (buffer, ptr);

    return buffer;
  }

  protected byte[] createEraseAllUnprotected ()
  {
    int ptr = 0;
    byte[] buffer = new byte[3];

    buffer[ptr++] = Command.ERASE_ALL_UNPROTECTED_0F;
    ptr = terminateBuffer (buffer, ptr);
    return buffer;
  }

  private int terminateBuffer (byte[] buffer, int ptr)
  {
    buffer[ptr++] = TelnetCommand.IAC;
    buffer[ptr++] = TelnetCommand.EOR;

    assert ptr == buffer.length;
    return ptr;
  }
}