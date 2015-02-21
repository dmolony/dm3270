package com.bytezone.dm3270.attributes;

import com.bytezone.dm3270.application.ScreenPosition;

public class ExtendedHighlight extends Attribute
{
  private static String[] highlights = { "xx", "Blink", "Reverse video", "bb",
                                        "Underscore" };

  public ExtendedHighlight (byte value)
  {
    super (AttributeType.HIGHLIGHT, Attribute.XA_HIGHLIGHTING, value);
  }

  @Override
  public void process (ScreenPosition screenPosition)
  {
    switch (value)
    {
      case 0:
        System.out.println ("0 not written - default");
        System.out.println ("**************** Should be resetting here");
        break;
      case (byte) 0xF0:
        System.out.println ("F0 not written - normal");
        break;
      case (byte) 0xF1:
        System.out.println ("F1 not written - blink");
        break;
      case (byte) 0xF2:
        screenPosition.setReverseVideo (true);
        break;
      case (byte) 0xF4:
        screenPosition.setUnderscore (true);
        break;

      default:
    }
  }

  @Override
  public String toString ()
  {
    String valueText = value == 0 ? "Reset" : highlights[value & 0x0F];
    return String.format ("%-12s : %02X %s", name (), value, valueText);
  }
}