package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.display.Screen;

public class FileTransferInboundSF extends StructuredField
{

  public FileTransferInboundSF (byte[] buffer, int offset, int length, Screen screen)
  {
    super (buffer, offset, length, screen);

    System.out.println ();
    System.out.println (this);
    System.out.println ("-----------------------------------------"
        + "------------------------------");
  }

  @Override
  public void process ()
  {
    // what to do?
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ("Struct Field : D0 File Transfer Inbound\n");
    text.append (Utility.toHex (data));
    return text.toString ();
  }
}