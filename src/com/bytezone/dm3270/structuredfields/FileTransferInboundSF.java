package com.bytezone.dm3270.structuredfields;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.display.Screen;

public class FileTransferInboundSF extends StructuredField
{
  private final byte rectype;
  private final byte subtype;

  private byte[] transfer;
  private boolean ebcdic;
  private final List<String> extraBytes = new ArrayList<> ();

  public FileTransferInboundSF (byte[] buffer, int offset, int length, Screen screen)
  {
    super (buffer, offset, length, screen);

    assert data[0] == (byte) 0xD0;

    rectype = data[1];
    subtype = data[2];

    switch (rectype)
    {
      case 0x00:
        extraBytes.add (Utility.toHexString (data, 3, data.length - 3));
        break;

      case 0x41:
        extraBytes.add (Utility.toHexString (data, 3, data.length - 3));
        break;

      case 0x46:
        if (subtype == 0x05)        // transfer buffer
        {
          int buflen = Utility.unsignedShort (data, 12) - 5;
          extraBytes.add (Utility.toHexString (data, 3, 6));
          extraBytes.add (Utility.toHexString (data, 9, 5));    // 5 extra

          ebcdic = true;
          transfer = new byte[buflen];
          System.arraycopy (data, 14, transfer, 0, buflen);
        }
        else
          extraBytes.add (Utility.toHexString (data, 3, data.length - 3));
        break;

      case 0x47:
        extraBytes.add (Utility.toHexString (data, 3, data.length - 3));
        break;

      default:
        extraBytes.add (Utility.toHexString (data, 3, data.length - 3));
        System.out.printf ("Unknown type: %02X%n", rectype);
    }

    if (true)
    {
      System.out.println (this);
      System.out.println ("-----------------------------------------"
          + "------------------------------");
    }
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ("Struct Field : D0 File Transfer Inbound\n");
    text.append (String.format ("   type      : %02X%n", rectype));
    text.append (String.format ("   subtype   : %02X", subtype));

    for (String extra : extraBytes)
      if (!extra.isEmpty ())
        text.append ("\n   extras    : " + extra);

    if (transfer != null)
    {
      text.append ("\n");
      text.append (Utility.toHex (transfer, ebcdic));
    }

    return text.toString ();
  }
}