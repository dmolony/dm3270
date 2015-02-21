package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.application.Utility;

public class DefaultReply extends ReplyField
{
  public DefaultReply (byte[] buffer)
  {
    super (buffer);
    System.out.printf ("Unknown reply field: %02X%n", buffer[0]);
    System.out.println (Utility.toHex (buffer));
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (super.toString ());

    text.append (String.format ("%n%n%s", Utility.toHex (data)));

    return text.toString ();
  }
}