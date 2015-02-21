package com.bytezone.dm3270.extended;

import com.bytezone.dm3270.application.Utility;

public class ResponseCommand extends AbstractExtendedCommand
{
  public ResponseCommand (CommandHeader commandHeader, byte[] buffer, int offset,
      int length)
  {
    super (commandHeader, buffer, offset, length);

    if (length != 1)
      System.out.println (Utility.toHex (buffer, offset, length));
  }

  @Override
  public String getName ()
  {
    return "Response";
  }

  @Override
  public String toString ()
  {
    return String.format ("Rsp: %02X", data[0]);
  }
}