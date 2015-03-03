package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.display.Screen;

public class DefaultStructuredField extends StructuredField
{
  public DefaultStructuredField (byte[] buffer, int offset, int length, Screen screen)
  {
    super (buffer, offset, length, screen);
    System.out.println ("Default Structured Field !!");
  }

  @Override
  public void process ()
  {
    System.out.printf ("Processing a DefaultStructuredField: %02X%n", type);
  }
}