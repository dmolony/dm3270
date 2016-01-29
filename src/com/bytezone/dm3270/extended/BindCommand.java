package com.bytezone.dm3270.extended;

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
  private final int profile;
  private final int fmProfile;
  private final int tsProfile;

  private final int[] chainingUse = new int[2];
  private final int[] modeSelection = new int[2];
  private final int[] responseProtocol = new int[2];
  private final int[] commit = new int[2];
  private final int[] scbCompression = new int[2];
  private final int[] sendEndBracket = new int[2];

  private final int wholeBIUs;
  private final int fmHeaderUsage;
  private final int bracketsUsage;
  private final int bracketsTermination;
  private final int altCodeSet;
  private final int seqAvailability;
  private final int bisSent;
  private final int bindQueuingIndicator;

  private byte flags;
  private int primaryRows;
  private int primaryColumns;
  private int alternateRows;
  private int alternateColumns;
  private int presentationSpace;
  private byte cryptographicControl;
  private int pluNameLength;
  private String pluName;
  private int userDataLength;

  public BindCommand (CommandHeader commandHeader, byte[] buffer, int offset, int length)
  {
    super (commandHeader, buffer, offset, length);
    assert data[0] == 0x31;                    // bind command

    Dm3270Utility.hexDump (data);

    format = (data[1] & 0xF0) >> 4;
    type = data[1] & 0x0F;

    fmProfile = data[2] & 0xFF;
    tsProfile = data[3] & 0xFF;

    chainingUse[0] = (data[4] & 0x80) >> 7;
    modeSelection[0] = (data[4] & 0x40) >> 6;
    responseProtocol[0] = (data[4] & 0x30) >> 4;
    commit[0] = (data[4] & 0x08) >> 3;
    scbCompression[0] = (data[4] & 0x02) >> 1;
    sendEndBracket[0] = (data[4] & 0x01);

    chainingUse[1] = (data[5] & 0x80) >> 7;
    modeSelection[1] = (data[5] & 0x40) >> 6;
    responseProtocol[1] = (data[5] & 0x30) >> 4;
    commit[1] = (data[5] & 0x08) >> 3;
    scbCompression[1] = (data[5] & 0x02) >> 1;
    sendEndBracket[1] = (data[5] & 0x01);

    wholeBIUs = (data[6] & 0x80) >> 7;
    fmHeaderUsage = (data[6] & 0x40) >> 6;
    bracketsUsage = (data[6] & 0x20) >> 5;
    bracketsTermination = (data[6] & 0x10) >> 4;
    altCodeSet = (data[6] & 0x08) >> 3;
    seqAvailability = (data[6] & 0x04) >> 2;
    bisSent = (data[6] & 0x02) >> 1;
    bindQueuingIndicator = data[6] & 0x01;

    profile = data[14] & 0xFF;

    if (profile == 2)
    {
      flags = data[15];                               // bit 0 = 1 means Query supported

      // 16-19 are reserved

      primaryRows = data[20] & 0xFF;
      primaryColumns = data[21] & 0xFF;
      alternateRows = data[22] & 0xFF;
      alternateColumns = data[23] & 0xFF;
      presentationSpace = data[24] & 0xFF;            // 0x7E means 'use default fields'

      // 25 is compression

      cryptographicControl = data[26];
      pluNameLength = data[27] & 0xFF;
      pluName = Dm3270Utility.getString (data, 28, pluNameLength);
      userDataLength = data[28 + pluNameLength] & 0xFF;
    }
    else
      System.out.println ("profile: " + profile);
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
    text.append (String.format ("FM profile ........... %02X%n", fmProfile));
    text.append (String.format ("TS profile ........... %02X%n", tsProfile));
    text.append ("\n----- Primary LU -----\n");
    text.append (String.format ("Chaining use ......... %02X%n", chainingUse[0]));
    text.append (String.format ("Mode selection ....... %02X%n", modeSelection[0]));
    text.append (String.format ("Resonse protocol ..... %02X%n", responseProtocol[0]));
    text.append (String.format ("Commit ............... %02X%n", commit[0]));
    text.append (String.format ("SCB compression ...... %02X%n", scbCompression[0]));
    text.append (String.format ("send End Bracket ..... %02X%n", sendEndBracket[0]));
    text.append ("\n---- Secondary LU ----\n");
    text.append (String.format ("Chaining use ......... %02X%n", chainingUse[1]));
    text.append (String.format ("Mode selection ....... %02X%n", modeSelection[1]));
    text.append (String.format ("Resonse protocol ..... %02X%n", responseProtocol[1]));
    text.append (String.format ("Commit ............... %02X%n", commit[1]));
    text.append (String.format ("SCB compression ...... %02X%n", scbCompression[1]));
    text.append (String.format ("send End Bracket ..... %02X%n", sendEndBracket[1]));
    text.append ("\n---- Common LU ----\n");
    text.append (String.format ("whole BIUs ........... %02X%n", wholeBIUs));
    text.append (String.format ("FM header usage ...... %02X%n", fmHeaderUsage));
    text.append (String.format ("brackets usage ....... %02X%n", bracketsUsage));
    text.append (String.format ("brackets termination . %02X%n", bracketsTermination));
    text.append (String.format ("alt code set ......... %02X%n", altCodeSet));
    text.append (String.format ("set availability ..... %02X%n", seqAvailability));
    text.append (String.format ("BIS sent ............. %02X%n", bisSent));
    text.append (String.format ("bind queuing ind ..... %02X%n", bindQueuingIndicator));
    text.append ("\n");
    text.append (String.format ("Rows ................. %02X%n", primaryRows));
    text.append (String.format ("Columns .............. %02X%n", primaryColumns));
    text.append (String.format ("Alt Rows ............. %02X%n", alternateRows));
    text.append (String.format ("Alt Columns .......... %02X%n", alternateColumns));
    text.append (String.format ("PLU name ............. %s%n", pluName));

    return text.toString ();
  }
}