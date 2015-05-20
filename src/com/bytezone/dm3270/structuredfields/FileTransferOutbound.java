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
  private byte[] extras;

  public FileTransferOutbound (byte[] buffer, int offset, int length, Screen screen)
  {
    super (buffer, offset, length, screen);
    assert data[0] == (byte) 0xD0;

    type = data[1];
    subtype = data[2];

    if (type == 0x47 && subtype == 0x04)
      ebcdic = data[6] != 0;                // temp

    switch (type)
    {
      case 0x00:
        //        assert data.length == 0x21;
        if (data.length == 33)
        {
          message = new String (data, 26, 7);
          extras = new byte[23];
          System.arraycopy (data, 3, extras, 0, extras.length);
        }
        else if (data.length == 39)
        {
          message = new String (data, 32, 7);
          extras = new byte[29];
          System.arraycopy (data, 3, extras, 0, extras.length);
        }
        else
        {
          System.out.printf ("Unrecognised data length: %d%n", data.length);
          System.out.println (Utility.toHex (data));
        }
        break;

      case 0x41:
        break;

      case 0x47:
        if (subtype == 0x04)        // message
        {
          int buflen = Utility.unsignedShort (data, 6) - 5;
          extras = new byte[3];
          System.arraycopy (data, 3, extras, 0, extras.length);
          if (data[6] == 0)       // temp
            message = new String (data, 8, buflen).trim ();
          else
          {
            transfer = new byte[buflen];
            System.arraycopy (data, 8, transfer, 0, buflen);
          }
        }
        else if (subtype == 0x11)   // transfer buffer
        {
          extras = new byte[5];
          System.arraycopy (data, 3, extras, 0, extras.length);
        }
        //        else if (subtype == 0x05)   // end of transfer buffers
        //        {
        //          extras = new byte[6];
        //          System.arraycopy (data, 3, extras, 0, extras.length);
        //        }
        else
          System.out.println ("Unknown subtype");
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
      case 0x00:                      // get ready for a message or data transfer
        if (subtype == 0x12)
        {
          // RSF 0x0D 0x00 0x09
        }
        break;

      case 0x47:                      // data transfer buffer
        if (subtype == 0x04)
        {
          // RSF  0x0D 0x47 0x05 0x63 0x06 0x00 0x00 0x00 0x01 
        }
        break;

      case 0x41:                      // finished data transfer
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
      text.append (Utility.toHex (data, 3, data.length - 3, ebcdic));
    }
    if (extras != null)
    {
      text.append ("\n   extras   " + " :\n");
      text.append (Utility.toHex (extras));
    }
    return text.toString ();
  }
}