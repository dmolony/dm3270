package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.utilities.Dm3270Utility;

public class DefaultStructuredField extends StructuredField
{
  public DefaultStructuredField (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);
    System.out.println ("Default Structured Field !!");
  }

  @Override
  public void process (Screen screen)
  {
    System.out.printf ("Processing a DefaultStructuredField: %02X%n", type);
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("Unknown SF   : %02X%n", data[0]));
    text.append (Dm3270Utility.toHex (data));
    return text.toString ();
  }
}