package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.display.Screen;

public class EraseResetSF extends StructuredField
{
  byte flags;

  public EraseResetSF (byte[] buffer, int offset, int length, Screen screen)
  {
    super (buffer, offset, length, screen);

    assert data[0] == StructuredField.ERASE_RESET;
    flags = data[1];
  }

  @Override
  public void process ()
  {
    // not doing anything, assume that the size will be the expected 24x80
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ("Struct Field : 03 Erase/Reset\n");
    text.append (String.format ("   flags     : %02X%n", flags));
    return text.toString ();
  }
}