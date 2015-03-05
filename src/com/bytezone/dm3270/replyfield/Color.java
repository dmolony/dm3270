package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.attributes.ColorAttribute;

public class Color extends QueryReplyField
{
  private byte flags;
  private int pairs;
  private byte[] attributeValue;
  private byte[] action;

  public Color ()
  {
    super (COLOR_QUERY_REPLY);

    int ptr = createReply (34);     // 16 pairs x 2 plus count field

    // count field
    reply[ptr++] = 0x00;
    reply[ptr++] = 0x10;

    reply[ptr++] = ColorAttribute.COLOR_NEUTRAL1;
    reply[ptr++] = ColorAttribute.COLOR_GREEN;

    reply[ptr++] = ColorAttribute.COLOR_BLUE;
    reply[ptr++] = ColorAttribute.COLOR_BLUE;

    reply[ptr++] = ColorAttribute.COLOR_RED;
    reply[ptr++] = ColorAttribute.COLOR_RED;

    reply[ptr++] = ColorAttribute.COLOR_PINK;
    reply[ptr++] = ColorAttribute.COLOR_PINK;

    reply[ptr++] = ColorAttribute.COLOR_GREEN;
    reply[ptr++] = ColorAttribute.COLOR_GREEN;

    reply[ptr++] = ColorAttribute.COLOR_TURQUOISE;
    reply[ptr++] = ColorAttribute.COLOR_TURQUOISE;

    reply[ptr++] = ColorAttribute.COLOR_YELLOW;
    reply[ptr++] = ColorAttribute.COLOR_YELLOW;

    reply[ptr++] = ColorAttribute.COLOR_NEUTRAL2;
    reply[ptr++] = ColorAttribute.COLOR_NEUTRAL2;

    reply[ptr++] = ColorAttribute.COLOR_BLACK;
    reply[ptr++] = ColorAttribute.COLOR_BLACK;

    reply[ptr++] = ColorAttribute.COLOR_DEEP_BLUE;
    reply[ptr++] = ColorAttribute.COLOR_DEEP_BLUE;

    reply[ptr++] = ColorAttribute.COLOR_ORANGE;
    reply[ptr++] = ColorAttribute.COLOR_ORANGE;

    reply[ptr++] = ColorAttribute.COLOR_PURPLE;
    reply[ptr++] = ColorAttribute.COLOR_PURPLE;

    reply[ptr++] = ColorAttribute.COLOR_PALE_GREEN;
    reply[ptr++] = ColorAttribute.COLOR_PALE_GREEN;

    reply[ptr++] = ColorAttribute.COLOR_PALE_TURQUOISE;
    reply[ptr++] = ColorAttribute.COLOR_PALE_TURQUOISE;

    reply[ptr++] = ColorAttribute.COLOR_GREY;
    reply[ptr++] = ColorAttribute.COLOR_GREY;

    reply[ptr++] = ColorAttribute.COLOR_WHITE;  // make sure these 0xFF bytes are doubled
    reply[ptr++] = ColorAttribute.COLOR_WHITE;  // before sending to a socket

    checkDataLength (ptr);
  }

  public Color (byte[] buffer)
  {
    super (buffer);

    assert data[1] == COLOR_QUERY_REPLY;

    flags = data[2];
    pairs = data[3] & 0xFF;
    attributeValue = new byte[pairs];
    action = new byte[pairs];

    for (int i = 0; i < pairs; i++)
    {
      attributeValue[i] = data[i * 2 + 4];
      action[i] = data[i * 2 + 5];
    }
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (super.toString ());

    text.append (String.format ("%n  flags      : %02X", flags));
    text.append (String.format ("%n  pairs      : %d", pairs));
    for (int i = 0; i < pairs; i++)
    {
      text.append (String.format ("%n  val/actn   : %02X/%02X - %s", attributeValue[i],
                                  action[i], ColorAttribute.colorName (attributeValue[i])));
      if (attributeValue[i] != action[i])
        text.append ("/" + ColorAttribute.colorName (action[i]));
    }

    return text.toString ();
  }
}