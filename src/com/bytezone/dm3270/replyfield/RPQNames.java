package com.bytezone.dm3270.replyfield;

import java.io.UnsupportedEncodingException;

import com.bytezone.dm3270.utilities.Dm3270Utility;

public class RPQNames extends QueryReplyField
{
  String deviceType;
  long model;
  String rpqName;

  public RPQNames ()
  {
    super (RPQ_NAMES_REPLY);

    try
    {
      String rpqName = "dm3270";
      int ptr = createReply (9 + rpqName.length ());

      ptr = Dm3270Utility.packUnsignedLong (0, reply, ptr);     // deviceType
      ptr = Dm3270Utility.packUnsignedLong (0, reply, ptr);     // model

      reply[ptr++] = (byte) (rpqName.length () + 1);      // name length + 1

      for (byte b : rpqName.getBytes ("CP1047"))
        reply[ptr++] = b;

      checkDataLength (ptr);
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }
  }

  public RPQNames (byte[] buffer)
  {
    super (buffer);
    assert data[1] == RPQ_NAMES_REPLY;

    try
    {
      deviceType = new String (data, 2, 4, "CP1047");
      model = Dm3270Utility.unsignedLong (data, 6);
      int len = (data[10] & 0xFF) - 1;
      if (len > 0)
        rpqName = new String (data, 11, len, "CP1047");
      else
        rpqName = "";
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }
  }

  public String getRPQName ()
  {
    return rpqName;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (super.toString ());

    text.append (String.format ("%n  device     : %s", deviceType));
    text.append (String.format ("%n  model      : %d", model));
    text.append (String.format ("%n  RPQ name   : %s", rpqName));

    return text.toString ();
  }
}