package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.utilities.Utility;

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

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("Unknown SF   : %02X%n", data[0]));
    text.append (Utility.toHex (data));
    return text.toString ();
  }
}