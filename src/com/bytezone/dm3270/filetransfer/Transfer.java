package com.bytezone.dm3270.filetransfer;

import java.util.ArrayList;
import java.util.List;

public class Transfer
{
  String type;
  List<byte[]> buffers = new ArrayList<> ();
  int length;

  public Transfer (String type)
  {
    this.type = type;
  }

  public void add (byte[] buffer)
  {
    buffers.add (buffer);
    length += buffer.length;
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
    for (byte[] buffer : buffers)
      text.append (String.format ("%n  Buffer %3d : %,d", bufno++, buffer.length));
    text.append (String.format ("%nTotal length : %,d", length));

    return text.toString ();
  }
}