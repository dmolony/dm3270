package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.buffers.AbstractTN3270Command;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.utilities.Dm3270Utility;

public abstract class StructuredField extends AbstractTN3270Command
{
  public final static byte RESET_PARTITION = 0x00;
  public final static byte READ_PARTITION = 0x01;
  public final static byte ERASE_RESET = 0x03;
  public final static byte LOAD_PROGRAMMED_SYMBOLS = 0x06;
  public final static byte SET_REPLY_MODE = 0x09;
  public final static byte SET_WINDOW_ORIGIN = 0x0B;
  public final static byte CREATE_PARTITION = 0x0C;
  public final static byte DESTROY_PARTITION = 0x0D;
  public final static byte ACTIVATE_PARTITION = 0x0E;
  public final static byte OUTBOUND_3270DS = 0x40;
  public final static byte SCS_DATA = 0x41;
  public final static byte SELECT_FORMAT_GROUP = 0x4A;
  public final static byte PRESENT_ABSOLUTE_FORMAT = 0x4B;
  public final static byte PRESENT_RELATIVE_FORMAT = 0x4C;

  public final static byte INBOUND_3270DS = (byte) 0x80;
  public final static byte QUERY_REPLY = (byte) 0x81;

  public final static byte IND$FILE = (byte) 0xD0;

  protected byte type;

  public StructuredField (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);
    type = buffer[offset];
  }

  @Override
  public void process (Screen screen)
  {
    // do nothing 
  }

  public String brief ()
  {
    return toString ();
  }

  @Override
  public String toString ()
  {
    return String.format ("StrF: %s", Dm3270Utility.toHex (data).substring (8));
  }
}