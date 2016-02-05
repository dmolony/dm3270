package com.bytezone.dm3270.extended;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.utilities.Dm3270Utility;

// http://publibfp.dhe.ibm.com/cgi-bin/bookmgr/BOOKS/d50a5007/6.3.20
// z/OS V2R1.0 Communications Server: SNA Programming

public class BindCommand extends AbstractExtendedCommand
{
  private static final String[] presentationText =
      { "Undefined", "12 x 40", "24 x 80", "default 24 x 80, alternate in Query Reply",
        "fixed size as defined by default values",
        "both default and alternate as specifed" };
  private static final String[] compressionTypes =
      { "No compression", "Compression bid", "Reserved", "Compression required" };

  private final int format;
  private final int type;

  private final int psProfile;
  private final int fmProfile;
  private final int tsProfile;

  private final LogicalUnit primaryLuProtocols;
  private final LogicalUnit secondaryLuProtocols;

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
  private boolean querySupported;
  private int primaryRows;
  private int primaryColumns;
  private int alternateRows;
  private int alternateColumns;
  private int presentationSpace;

  private int compression;
  private String compressionText;

  private byte cryptographicControl;
  private int primaryLuNameLength;
  private String primaryLuName;
  private int userDataLength;
  private int extraBytes;

  public BindCommand (CommandHeader commandHeader, byte[] buffer, int offset, int length)
  {
    super (commandHeader, buffer, offset, length);
    assert data[0] == 0x31;                    // bind command

    Dm3270Utility.hexDump (data);

    format = (data[1] & 0xF0) >> 4;
    type = data[1] & 0x0F;

    fmProfile = data[2] & 0xFF;         // Function Management Profile   - should be 03
    tsProfile = data[3] & 0xFF;         // Transmission Services Profile - should be 03
    psProfile = data[14] & 0xFF;        // Presentation Space Profile    - should be 02

    if (fmProfile != 3 || tsProfile != 3 || psProfile != 2)
      System.out.printf ("FM:02X, TS:%02X, PS:%02X%n", fmProfile, tsProfile, psProfile);

    primaryLuProtocols = new LogicalUnit (data[4]);
    secondaryLuProtocols = new LogicalUnit (data[5]);

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
      querySupported = (flags & 0x80) == 0x80;

      // 16-19 are reserved

      primaryRows = data[20] & 0xFF;
      primaryColumns = data[21] & 0xFF;
      alternateRows = data[22] & 0xFF;
      alternateColumns = data[23] & 0xFF;
      presentationSpace = data[24] & 0xFF;

      compression = data[25] & 0xFF;
      compressionText = compressionTypes[data[25] & 0x03];

      cryptographicControl = data[26];
      privateOptions = (data[26] & 0xC0) >> 6;
      sessionOptions = (data[26] & 0x30) >> 4;
      sessionOptionsLength = data[26] & 0x0F;

      nsOffset = cryptographicControl == 0 ? 0 : 8;

      primaryLuNameLength = data[27 + nsOffset] & 0xFF;
      primaryLuName = Dm3270Utility.getString (data, 28 + nsOffset, primaryLuNameLength);

      userDataOffset = 28 + nsOffset + primaryLuNameLength;
      userDataLength = data[userDataOffset] & 0xFF;
      extraBytes = data.length - userDataOffset - userDataLength - 1;

      System.out.printf ("Data:%d, ns:%d, pLU:%d, user:%d, extra:%d%n", data.length,
                         nsOffset, primaryLuNameLength, userDataLength, extraBytes);

      if (extraBytes > 0)
      {
        System.out.println ();
        int ptr = userDataOffset + userDataLength + 1;
        while (ptr < data.length)
        {
          int len = data[ptr] & 0xFF;
          String userData = Dm3270Utility.getSanitisedString (data, ptr + 1, len);
          System.out.printf ("ptr:%d, len:%d, [%s]%n", ptr, len, userData);
          ptr += len + 1;
        }
      }
      System.out.println ();
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
    //    System.out.println (this);
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

    int offset = presentationSpace <= 3 ? 0 : 0x7A;
    String presText = presentationText[presentationSpace - offset];

    text.append (String.format ("Format ............... %02X  %s%n", format,
                                "must be zero"));
    text.append (String.format ("Type ................. %02X  %s%n", type,
                                (type == 1 ? "non-" : "") + "negotiable"));
    text.append ("\n");
    text.append (String.format ("FM profile ........... %02X%n", fmProfile));
    text.append (String.format ("TS profile ........... %02X%n", tsProfile));
    text.append (String.format ("PS profile ........... %02X%n", psProfile));

    String[] plu = primaryLuProtocols.toString ().split ("\n");
    String[] slu = secondaryLuProtocols.toString ().split ("\n");
    text.append ("\n---- Primary LU ---------           ---- Secondary LU -------\n");
    for (int i = 0; i < plu.length; i++)
      text.append (String.format ("%-35s %-35s%n", plu[i], slu[i]));

    text.append ("\n---- Common LU ----------\n");
    text.append (String.format ("Whole BIUs ........... %02X%n", wholeBIUs));
    text.append (String.format ("FM header usage ...... %02X%n", fmHeaderUsage));
    text.append (String.format ("Brackets usage ....... %02X%n", bracketsUsage));
    text.append (String.format ("Brackets termination . %02X%n", bracketsTermination));
    text.append (String.format ("Alt code set ......... %02X%n", altCodeSet));
    text.append (String.format ("Set availability ..... %02X%n", seqAvailability));
    text.append (String.format ("BIS sent ............. %02X%n", bisSent));
    text.append (String.format ("Bind queuing ind ..... %02X%n", bindQueuingIndicator));

    text.append ("\n---- Cryptography -------\n");
    text.append (String.format ("Private options ...... %02X%n", privateOptions));
    text.append (String.format ("Session options ...... %02X%n", sessionOptions));
    text.append (String.format ("Session options len .. %02X%n", sessionOptionsLength));
    text.append (String.format ("NS offset ............ %02X%n", nsOffset));

    text.append ("\n--- Presentation Space --\n");
    text.append (String.format ("Query supported ...... %02X  %s%n", flags,
                                querySupported));
    text.append (String.format ("Presentation space ... %02X  %s%n", presentationSpace,
                                presText));
    text.append (String.format ("Default rows ......... %02X  %3d%n", primaryRows,
                                primaryRows));
    text.append (String.format ("Default columns ...... %02X  %3d%n", primaryColumns,
                                primaryColumns));
    text.append (String.format ("Alternate Rows ....... %02X  %3d%n", alternateRows,
                                alternateRows));
    text.append (String.format ("Alternate Columns .... %02X  %3d%n", alternateColumns,
                                alternateColumns));
    text.append ("\n");
    text.append (String.format ("Compression .......... %02X  %s%n", compression,
                                compressionText));
    text.append ("\n");
    text.append (String.format ("Primary LU name len .. %02X%n", primaryLuNameLength));
    text.append (String.format ("Primary LU name ...... %s%n", primaryLuName));

    text.append (String.format ("User data offset ..... %02X%n", userDataOffset));
    text.append (String.format ("User data length ..... %02X%n", userDataLength));
    text.append ("\n");
    text.append (String.format ("Extra bytes .......... %02X%n", extraBytes));

    if (extraBytes > 0)
      text.append (Dm3270Utility.toHex (data, userDataOffset + userDataLength + 1,
                                        extraBytes));

    return text.toString ();
  }
}