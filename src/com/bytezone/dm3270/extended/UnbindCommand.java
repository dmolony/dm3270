package com.bytezone.dm3270.extended;

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

public class UnbindCommand extends AbstractExtendedCommand
{
  public UnbindCommand (CommandHeader commandHeader, byte[] buffer, int offset,
      int length)
  {
    super (commandHeader, buffer, offset, length);
  }

  @Override
  public String getName ()
  {
    return "Unbind";
  }

  @Override
  public String toString ()
  {
    return String.format ("UNB: %02X", data[0]);
  }
}