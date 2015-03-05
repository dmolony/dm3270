package com.bytezone.dm3270.replyfield;

public class ReplyModes extends QueryReplyField
{
  private static String[] modeTypes = { "Field mode", "Extended field mode",
                                       "Character mode" };
  int[] modes;

  public ReplyModes ()
  {
    super (REPLY_MODES_REPLY);
    int ptr = createReply (3);

    reply[ptr++] = 0x00;
    reply[ptr++] = 0x01;
    reply[ptr++] = 0x02;

    checkDataLength (ptr);
  }

  public ReplyModes (byte[] buffer)
  {
    super (buffer);

    assert data[1] == REPLY_MODES_REPLY;

    modes = new int[data.length - 2];
    for (int i = 0; i < modes.length; i++)
      modes[i] = data[i + 2] & 0xFF;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (super.toString ());

    for (int mode : modes)
      text.append (String.format ("%n  mode       : %02X - %s", mode, modeTypes[mode]));
    return text.toString ();
  }
}