package com.bytezone.dm3270.extended;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.utilities.Dm3270Utility;

// See SNA Programming pp 807

/*
 * #define TN3270E_UNBIND_NORMAL 0x01
 * #define TN3270E_UNBIND_BIND_FORTHCOMING 0x02
 * #define TN3270E_UNBIND_VR_INOPERATIVE 0x07
 * #define TN3270E_UNBIND_RX_INOPERATIVE 0x08
 * #define TN3270E_UNBIND_HRESET 0x09
 * #define TN3270E_UNBIND_SSCP_GONE 0x0a
 * #define TN3270E_UNBIND_VR_DEACTIVATED 0x0b
 * #define TN3270E_UNBIND_LU_FAILURE_PERM 0x0c
 * #define TN3270E_UNBIND_LU_FAILURE_TEMP 0x0e
 * #define TN3270E_UNBIND_CLEANUP 0x0f
 * #define TN3270E_UNBIND_BAD_SENSE 0xfe
 */

// http://publibfp.dhe.ibm.com/cgi-bin/bookmgr/BOOKS/d50a5007/6.3.20

public class BindCommand extends AbstractExtendedCommand
{
  private final int format;
  private final int type;

  private final int psProfile;
  private final int fmProfile;
  private final int tsProfile;

  private final LogicalUnit primaryLU;
  private final LogicalUnit secondaryLU;

  private final int wholeBIUs;
  private final int fmHeaderUsage;
  private final int bracketsUsage;
  private final int bracketsTermination;
  private final int altCodeSet;
  private final int seqAvailability;
  private final int bisSent;
  private final int bindQueuingIndicator;

  private final int privateOptions;
  private final int sessionOptions;
  private final int sessionOptionsLength;

  private final int nsOffset;
  private final int userDataOffset;

  private byte flags;
  private int primaryRows;
  private int primaryColumns;
  private int alternateRows;
  private int alternateColumns;
  private int presentationSpace;
  private byte cryptographicControl;
  private int primaryLuNameLength;
  private String primaryLuName;
  private int userDataLength;

  private ScreenDimensions primaryScreenDimensions;
  private ScreenDimensions alternateScreenDimensions;

  public BindCommand (CommandHeader commandHeader, byte[] buffer, int offset, int length)
  {
    super (commandHeader, buffer, offset, length);
    assert data[0] == 0x31;                    // bind command

    Dm3270Utility.hexDump (data);

    format = (data[1] & 0xF0) >> 4;
    type = data[1] & 0x0F;

    fmProfile = data[2] & 0xFF;         // should be 03
    tsProfile = data[3] & 0xFF;         // should be 03
    psProfile = data[14] & 0xFF;        // should be 02
    if (fmProfile != 3 || tsProfile != 3 || psProfile != 2)
      System.out.printf ("FM:02X, TS:%02X, PS:%02X%n", fmProfile, tsProfile, psProfile);

    primaryLU = new LogicalUnit (data[4]);
    secondaryLU = new LogicalUnit (data[5]);

    wholeBIUs = (data[6] & 0x80) >> 7;
    fmHeaderUsage = (data[6] & 0x40) >> 6;
    bracketsUsage = (data[6] & 0x20) >> 5;
    bracketsTermination = (data[6] & 0x10) >> 4;
    altCodeSet = (data[6] & 0x08) >> 3;
    seqAvailability = (data[6] & 0x04) >> 2;
    bisSent = (data[6] & 0x02) >> 1;
    bindQueuingIndicator = data[6] & 0x01;

    if (psProfile == 2)
    {
      flags = data[15];                               // bit 0 = 1 means Query supported

      // 16-19 are reserved

      primaryRows = data[20] & 0xFF;
      primaryColumns = data[21] & 0xFF;
      alternateRows = data[22] & 0xFF;
      alternateColumns = data[23] & 0xFF;
      presentationSpace = data[24] & 0xFF;            // 0x7E means 'use default fields'
      primaryScreenDimensions = new ScreenDimensions (primaryRows, primaryColumns);
      alternateScreenDimensions = new ScreenDimensions (alternateRows, alternateColumns);

      // 25 is compression

      cryptographicControl = data[26];
      privateOptions = (data[26] & 0xC0) >> 6;
      sessionOptions = (data[26] & 0x30) >> 4;
      sessionOptionsLength = data[26] & 0x0F;

      if (cryptographicControl == 0)
        nsOffset = 0;
      else
        nsOffset = 8;

      primaryLuNameLength = data[27 + nsOffset] & 0xFF;
      primaryLuName = Dm3270Utility.getString (data, 28 + nsOffset, primaryLuNameLength);

      userDataOffset = 28 + nsOffset + primaryLuNameLength;
      userDataLength = data[userDataOffset] & 0xFF;

      System.out.printf ("Data:%d, ns:%d, pLU:%d, user:%d%n", data.length, nsOffset,
                         primaryLuNameLength, userDataLength);

      int ptr = userDataOffset + userDataLength + 1;
      while (ptr < data.length)
      {
        int len = data[ptr] & 0xFF;
        System.out.printf ("ptr:%d, len:%d%n", ptr, len);
        ptr += len + 1;
      }
    }
    else
    {
      System.out.println ("profile: " + psProfile);
      privateOptions = 0;
      sessionOptions = 0;
      sessionOptionsLength = 0;
      nsOffset = 0;
      userDataOffset = 0;
    }
  }

  @Override
  public void process (Screen screen)
  {
    screen.getTelnetState ().setScreenDimensions (primaryScreenDimensions,
                                                  alternateScreenDimensions);
  }

  @Override
  public String getName ()
  {
    return "Bind";
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ("BND:\n");

    text.append (String.format ("Format ............... %02X%n", format));
    text.append (String.format ("Type ................. %02X%n", type));
    text.append ("\n");
    text.append (String.format ("FM profile ........... %02X%n", fmProfile));
    text.append (String.format ("TS profile ........... %02X%n", tsProfile));
    text.append (String.format ("PS profile ........... %02X%n", psProfile));
    text.append ("\n---- Primary LU ---------\n");
    text.append (primaryLU);
    text.append ("\n---- Secondary LU -------\n");
    text.append (secondaryLU);
    text.append ("\n---- Common LU ----------\n");
    text.append (String.format ("whole BIUs ........... %02X%n", wholeBIUs));
    text.append (String.format ("FM header usage ...... %02X%n", fmHeaderUsage));
    text.append (String.format ("brackets usage ....... %02X%n", bracketsUsage));
    text.append (String.format ("brackets termination . %02X%n", bracketsTermination));
    text.append (String.format ("alt code set ......... %02X%n", altCodeSet));
    text.append (String.format ("set availability ..... %02X%n", seqAvailability));
    text.append (String.format ("BIS sent ............. %02X%n", bisSent));
    text.append (String.format ("bind queuing ind ..... %02X%n", bindQueuingIndicator));
    text.append ("\n---- Cryptography -------\n");
    text.append (String.format ("private options ...... %02X%n", privateOptions));
    text.append (String.format ("session options ...... %02X%n", sessionOptions));
    text.append (String.format ("session options len .. %02X%n", sessionOptionsLength));
    text.append (String.format ("ns offset ............ %02X%n", nsOffset));
    text.append ("\n---- Screen sizes ------\n");
    text.append (String.format ("Rows ................. %02X  %3d%n", primaryRows,
                                primaryRows));
    text.append (String.format ("Columns .............. %02X  %3d%n", primaryColumns,
                                primaryColumns));
    text.append (String.format ("Alt Rows ............. %02X  %3d%n", alternateRows,
                                alternateRows));
    text.append (String.format ("Alt Columns .......... %02X  %3d%n", alternateColumns,
                                alternateColumns));
    text.append ("\n");
    text.append (String.format ("primary LU name len .. %02X%n", primaryLuNameLength));
    text.append (String.format ("primary LU name ...... %s%n", primaryLuName));

    text.append (String.format ("user data offset ..... %02X%n", userDataOffset));
    text.append (String.format ("user data length ..... %02X%n", userDataLength));

    return text.toString ();
  }
}