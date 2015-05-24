package com.bytezone.dm3270.filetransfer;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.application.Utility;

public class Transfer
{
  String type;
  List<byte[]> messageBuffers = new ArrayList<> ();
  List<byte[]> dataBuffers = new ArrayList<> ();
  int messageLength;
  int dataLength;

  public void setCurrentTransfer (String type)
  {
    this.type = type;
    if (!isData () && !isMessage ())
    {
      this.type = null;
      throw new IllegalArgumentException ();
    }
  }

  public void add (byte[] buffer)
  {
    if (isData ())
    {
      dataBuffers.add (buffer);
      dataLength += buffer.length;

      System.out.println ("Received buffer:");
      System.out.println (Utility.toHex (buffer));
    }
    else
    {
      messageBuffers.add (buffer);
      messageLength += buffer.length;
    }
  }

  public int size ()
  {
    return isData () ? dataBuffers.size () : messageBuffers.size ();
  }

  public boolean isData ()
  {
    return "FT:DATA".equals (type);
  }

  public boolean isMessage ()
  {
    return "FT:MSG ".equals (type);
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Transfer ... : %s", type));

    int bufno = 0;
    if (isData ())
    {
      for (byte[] buffer : dataBuffers)
        text.append (String.format ("%n  Buffer %3d : %,d", bufno++, buffer.length));
      text.append (String.format ("%nTotal length : %,d", dataLength));
    }
    else
    {
      for (byte[] buffer : messageBuffers)
        text.append (String.format ("%n  Buffer %3d : %,d", bufno++, buffer.length));
      text.append (String.format ("%nTotal length : %,d", messageLength));
    }

    return text.toString ();
  }
}