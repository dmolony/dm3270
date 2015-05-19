package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.display.Screen;

public class FileTransferOutbound extends StructuredField
{

  public FileTransferOutbound (byte[] buffer, int offset, int length, Screen screen)
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
    StringBuilder text = new StringBuilder ("Struct Field : D0 File Transfer Outbound\n");
    text.append (Utility.toHex (data, false));
    return text.toString ();
  }
}