package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.display.Screen;

public class FileTransferSF extends StructuredField
{

  public FileTransferSF (byte[] buffer, int offset, int length, Screen screen)
  {
    super (buffer, offset, length, screen);
  }

  @Override
  public void process ()
  {
    // what to do?
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ("Struct Field : D0 File Transfer\n");
    text.append (Utility.toHex (data));
    return text.toString ();
  }
}
