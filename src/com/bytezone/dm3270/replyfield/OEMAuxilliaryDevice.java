package com.bytezone.dm3270.replyfield;

import java.io.UnsupportedEncodingException;

import com.bytezone.dm3270.structuredfields.StructuredField;

public class OEMAuxilliaryDevice extends QueryReplyField
{
  byte flags;
  byte refID;
  String deviceType;
  String userName;

  public OEMAuxilliaryDevice ()
  {
    super (QueryReplyField.OEM_AUXILLIARY_DEVICE_REPLY);

    byte[] rest = { 0x00, 0x00, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, //
                   0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40,  //
                   0x40, 0x40, 0x04, 0x01, 0x00, 0x00, 0x25, (byte) 0xFF,  //
                   0x02, 0x06, 0x00, 0x00, (byte) 0xC0, (byte) 0xD5, (byte) 0x9D, 0x50, //
                   0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x28, 0x4E,  //
                   0x6F, 0x74, 0x20, 0x79, 0x65, 0x74, 0x20, 0x6C,  //
                   0x6F, 0x67, 0x67, 0x65, 0x64, 0x20, 0x69, 0x6E,  //
                   0x21, 0x29, 0x00 };
    try
    {
      System.arraycopy ("TCP3270 ".getBytes ("CP1047"), 0, rest, 2, 8);
      System.arraycopy ("dm3270  ".getBytes ("CP1047"), 0, rest, 10, 8);
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }
    int ptr = createReply (rest.length);
    for (byte b : rest)
      reply[ptr++] = b;

    checkDataLength (ptr);
  }

  public OEMAuxilliaryDevice (byte[] buffer)
  {
    super (buffer);

    assert data[0] == StructuredField.QUERY_REPLY;
    assert data[1] == QueryReplyField.OEM_AUXILLIARY_DEVICE_REPLY;

    try
    {
      flags = data[2];
      refID = data[3];
      deviceType = new String (data, 4, 8, "CP1047").trim ();
      userName = new String (data, 12, 8, "CP1047").trim ();
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }
  }

  public String getUserName ()
  {
    return userName;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (super.toString ());

    text.append (String.format ("%n  flags1     : %02X", flags));
    text.append (String.format ("%n  ref ID     : %02X", refID));
    text.append (String.format ("%n  type       : %s", deviceType));
    text.append (String.format ("%n  name       : %s", userName));

    return text.toString ();
  }
}