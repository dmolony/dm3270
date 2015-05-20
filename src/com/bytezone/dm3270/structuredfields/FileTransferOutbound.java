package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.display.Screen;

public class FileTransferOutbound extends StructuredField
{
  private boolean ebcdic;
  private final byte type;
  private final byte subtype;
  private int max;
  private String message;
  private byte[] transfer;

  public FileTransferOutbound (byte[] buffer, int offset, int length, Screen screen)
  {
    super (buffer, offset, length, screen);

    type = data[1];
    subtype = data[2];

    if (type == 0x47 && subtype == 0x04)
      ebcdic = data[6] != 0;                // temp

    switch (type)
    {
      case 0x00:
        assert data.length == 0x21;
        message = new String (data, 0x1A, 7);
        System.out.println (message);
        break;

      case 0x41:
        break;

      case 0x47:
        if (subtype == 0x04)
        {
          int buflen = Utility.unsignedShort (data, 6) - 5;
          if (data[6] == 0)       // temp
            message = new String (data, 8, buflen).trim ();
          else
          {
            transfer = new byte[buflen];
            System.arraycopy (data, 8, transfer, 0, buflen);
          }
        }
        break;

      default:
        System.out.printf ("Unknown type: %02X%n", type);
    }

    System.out.println ();
    System.out.println (this);
    System.out.println ("-----------------------------------------"
        + "------------------------------");
  }

  @Override
  public void process ()
  {
    switch (type)
    {
      case 0x00:
        if (subtype == 0x12)
        {
          // RSF 0x0D 0x00 0x09
        }
        break;

      case 0x47:
        break;

      case 0x41:
        if (subtype == 0x12)
        {
          // RSF 0x0D 0x41 0x09
        }
        break;
    }
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ("Struct Field : D0 File Transfer Outbound\n");
    text.append (String.format ("   type      : %02X%n", type));
    text.append (String.format ("   subtype   : %02X%n", subtype));
    if (message != null)
      text.append (String.format ("   message   : %s", message));
    else if (transfer != null)
    {
      text.append ("\n");
      text.append (Utility.toHex (transfer, ebcdic));
    }
    else
    {
      text.append ("\n");
      text.append (Utility.toHex (data, 1, data.length - 1, ebcdic));
    }
    return text.toString ();
  }
}