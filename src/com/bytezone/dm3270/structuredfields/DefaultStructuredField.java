package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.application.ScreenHandler;

public class DefaultStructuredField extends StructuredField
{
  public DefaultStructuredField (byte[] buffer, int offset, int length,
      ScreenHandler screenHandler)
  {
    super (buffer, offset, length, screenHandler);
    System.out.println ("Default Structured Field !!");
  }

  @Override
  public void process ()
  {
    System.out.printf ("Processing a DefaultStructuredField: %02X%n", type);
  }
}