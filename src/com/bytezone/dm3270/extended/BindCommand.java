package com.bytezone.dm3270.extended;

import com.bytezone.dm3270.application.Utility;

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

public class BindCommand extends AbstractExtendedCommand
{
  private final int profile;
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
    assert buffer[offset] == 0x31;                 // bind command

    profile = buffer[offset + 14] & 0xFF;

    if (profile == 2)
    {
      flags = buffer[offset + 15];                    // bit 0 = 1 means Query supported
      // 16-19 are reserved
      primaryRows = buffer[offset + 20] & 0xFF;
      primaryColumns = buffer[offset + 21] & 0xFF;
      alternateRows = buffer[offset + 22] & 0xFF;
      alternateColumns = buffer[offset + 23] & 0xFF;
      presentationSpace = buffer[offset + 24] & 0xFF; // 0x7E means 'use default fields'
      // 25 is compression

      cryptographicControl = buffer[offset + 26];
      pluNameLength = buffer[offset + 27] & 0xFF;
      pluName = Utility.getString (buffer, offset + 28, pluNameLength);
      userDataLength = buffer[offset + 28 + pluNameLength] & 0xFF;
    }
  }

  @Override
  public String getName ()
  {
    return "Bind";
  }

  @Override
  public String toString ()
  {
    if (alternateRows > 0 || alternateColumns > 0)
      System.out.printf ("Alternate rows: %d, cols: %d%n", alternateRows,
                         alternateColumns);
    return String.format ("BND: Profile:%d, Rows:%d, Columns:%d, Name:%s", profile,
                          primaryRows, primaryColumns, pluName);
  }
}