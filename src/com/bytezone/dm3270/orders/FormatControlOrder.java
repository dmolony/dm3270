package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.display.DisplayScreen;
import com.bytezone.dm3270.display.Pen;

public class FormatControlOrder extends Order
{
  private static byte[] orderValues =
      { FCO_NULL, FCO_SUBSTITUTE, FCO_DUPLICATE, FCO_FIELD_MARK, FCO_FORM_FEED,
        FCO_CARRIAGE_RETURN, FCO_NEWLINE, FCO_END_OF_MEDIUM, FCO_EIGHT_ONES };
  private static String[] orderNames =
      { "Null", "Substitute", "Duplicate", "Field Mark", "Form Feed", "Return", "Newline",
        "EOM", "8 ones" };

  public FormatControlOrder (byte[] buffer, int offset)
  {
    this.buffer = new byte[1];
    this.buffer[0] = buffer[offset];
  }

  @Override
  public void process (DisplayScreen screen)
  {
    Pen pen = screen.getPen ();
    pen.write ((byte) 0x40);
  }

  @Override
  public String toString ()
  {
    byte value = buffer[0];
    String text = "";
    for (int i = 0; i < orderValues.length; i++)
      if (value == orderValues[i])
      {
        text = orderNames[i];
        break;
      }
    return String.format ("FCO : %-12s : %02X", text, buffer[0]);
  }
}