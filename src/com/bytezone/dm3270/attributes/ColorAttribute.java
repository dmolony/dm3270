package com.bytezone.dm3270.attributes;

import javafx.scene.paint.Color;

public abstract class ColorAttribute extends Attribute
{
  public static final byte COLOR_NEUTRAL1 = 0x00;
  public static final byte COLOR_BLUE = (byte) 0xF1;
  public static final byte COLOR_RED = (byte) 0xF2;
  public static final byte COLOR_PINK = (byte) 0xF3;
  public static final byte COLOR_GREEN = (byte) 0xF4;
  public static final byte COLOR_TURQUOISE = (byte) 0xF5;
  public static final byte COLOR_YELLOW = (byte) 0xF6;
  public static final byte COLOR_NEUTRAL2 = (byte) 0xF7;
  public static final byte COLOR_BLACK = (byte) 0xF8;
  public static final byte COLOR_DEEP_BLUE = (byte) 0xF9;
  public static final byte COLOR_ORANGE = (byte) 0xFA;
  public static final byte COLOR_PURPLE = (byte) 0xFB;
  public static final byte COLOR_PALE_GREEN = (byte) 0xFC;
  public static final byte COLOR_PALE_TURQUOISE = (byte) 0xFD;
  public static final byte COLOR_GREY = (byte) 0xFE;
  public static final byte COLOR_WHITE = (byte) 0xFF;

  static String[] colorNames =
      { "Neutral1", "Blue", "Red", "Pink", "Green", "Turquoise", "Yellow", "Neutral2",
        "Black", "Deep blue", "Orange", "Purple", "Pale green", "Pale turquoise", "Grey",
        "White" };

  public static final Color[] colors = //
      { Color.WHITESMOKE,     //
        Color.DODGERBLUE,     // or DEEPSKYBLUE, SKYBLUE, LIGHTSKYBLUE
        Color.RED,            //
        Color.PINK,           //
        Color.LIME,           //
        Color.TURQUOISE,      //
        Color.YELLOW,         //
        Color.WHITESMOKE,     //
        Color.BLACK,          //
        Color.DARKBLUE,       //
        Color.ORANGE,         //
        Color.PURPLE,         //
        Color.PALEGREEN,      //
        Color.PALETURQUOISE,  //
        Color.GREY,           //
        Color.WHITESMOKE      //
  };

  protected final Color color;

  public static String getName (Color searchColor)
  {
    int count = 0;
    for (Color color : colors)
    {
      if (color == searchColor)
        return colorNames[count];
      ++count;
    }
    return searchColor.toString ();
  }

  public ColorAttribute (AttributeType type, byte byteType, byte value)
  {
    super (type, byteType, value);
    color = colors[value & 0x0F];
  }

  public Color getColor ()
  {
    return color;
  }

  public static String colorName (byte value)
  {
    return colorNames[value & 0x0F];
  }

  @Override
  public String toString ()
  {
    return String.format ("%-12s : %02X %-12s", name (), attributeValue, //
                          colorName (attributeValue));
  }
}