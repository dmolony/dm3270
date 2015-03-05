package com.bytezone.dm3270.replyfield;

public class AuxilliaryDevices extends QueryReplyField
{
  byte flags1;
  byte flags2;

  public AuxilliaryDevices ()
  {
    super (AUXILLIARY_DEVICE_REPLY);

    int ptr = createReply (2);
    reply[ptr++] = 0;
    reply[ptr++] = 0;

    checkDataLength (ptr);
  }

  public AuxilliaryDevices (byte[] buffer)
  {
    super (buffer);

    assert data[1] == AUXILLIARY_DEVICE_REPLY;

    flags1 = data[2];
    flags2 = data[3];
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (super.toString ());

    text.append (String.format ("%n  flags1     : %02X", flags1));
    text.append (String.format ("%n  flags2     : %02X", flags2));
    return text.toString ();
  }
}