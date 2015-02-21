package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.application.Utility;

/*
 * The Reply Mode Query Reply (QCODE=x'88') indicates which Reply Modes are
 * supported by the terminal. Field Mode (Reply Mode 0) is the default.
 * Extended Field Mode (Reply Mode 1) adds SFE to the possible inbound orders.
 * Character Mode (Reply Mode 2) adds SA to the possible inbound orders.
 */

public class SetReplyMode extends StructuredField
{
  public static final byte RM_FIELD = 0x00;
  public static final byte RM_EXTENDED_FIELD = 0x01;
  public static final byte RM_CHARACTER = 0x02;

  private static final String[] modes = { "Field", "Extended field", "Character" };
  private final byte partition;
  private final byte replyMode;
  private final byte[] types;

  public SetReplyMode (byte[] buffer, int offset, int length, ScreenHandler screenHandler)
  {
    super (buffer, offset, length, screenHandler);

    assert data[0] == StructuredField.SET_REPLY_MODE;

    int ptr = offset + 1;
    partition = buffer[ptr++];
    replyMode = buffer[ptr++];

    int totalTypes = length - 3;
    types = new byte[totalTypes];
    System.arraycopy (buffer, ptr, types, 0, types.length);
  }

  @Override
  public void process ()
  {
    screenHandler.setReplyMode (replyMode, types);
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ("Struct Field : 09 Set Reply Mode\n");
    text.append (String.format ("   partition : %02X%n", partition));
    text.append (String.format ("   mode      : %02X %s mode", replyMode,
                                modes[replyMode]));
    if (types.length > 0)
      text.append (String.format ("%n   types     : %s", Utility.toHexString (types)));
    return text.toString ();
  }
}