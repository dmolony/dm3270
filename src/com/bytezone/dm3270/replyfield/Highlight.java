package com.bytezone.dm3270.replyfield;

public class Highlight extends QueryReplyField
{
  private static final String[] values = { "Normal", "Blink", "Reverse video", "",
                                          "Underscore", "", "", "", "Intensity" };

  final static byte HIGHLIGHT_DEFAULT = 0x00;
  final static byte HIGHLIGHT_NORMAL = (byte) 0xF0;
  final static byte HIGHLIGHT_BLINK = (byte) 0xF1;
  final static byte HIGHLIGHT_REVERSE = (byte) 0xF2;
  final static byte HIGHLIGHT_UNDERSCORE = (byte) 0xF4;
  final static byte HIGHLIGHT_INTENSIFY = (byte) 0xF8;

  int pairs;
  byte[] attributeValue;
  byte[] action;

  public Highlight ()
  {
    super (HIGHLIGHT_QUERY_REPLY);

    int ptr = createReply (11);       // 5 pairs x 2 plus 1

    reply[ptr++] = 0x05;      //Number of attribute-value/action pairs

    // Byte 1: Data stream attribute
    // Byte 2: Data stream action

    reply[ptr++] = HIGHLIGHT_DEFAULT;
    reply[ptr++] = HIGHLIGHT_NORMAL;

    reply[ptr++] = HIGHLIGHT_BLINK;
    reply[ptr++] = HIGHLIGHT_BLINK;

    reply[ptr++] = HIGHLIGHT_REVERSE;
    reply[ptr++] = HIGHLIGHT_REVERSE;

    reply[ptr++] = HIGHLIGHT_UNDERSCORE;
    reply[ptr++] = HIGHLIGHT_UNDERSCORE;

    reply[ptr++] = HIGHLIGHT_INTENSIFY;
    reply[ptr++] = HIGHLIGHT_INTENSIFY;

    checkDataLength (ptr);
  }

  public Highlight (byte[] buffer)
  {
    super (buffer);

    assert data[1] == HIGHLIGHT_QUERY_REPLY;

    pairs = data[2] & 0xFF;
    attributeValue = new byte[pairs];
    action = new byte[pairs];

    for (int i = 0; i < pairs; i++)
    {
      attributeValue[i] = data[i * 2 + 3];
      action[i] = data[i * 2 + 4];
    }
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (super.toString ());

    text.append (String.format ("%n  pairs      : %d", pairs));
    for (int i = 0; i < pairs; i++)
    {
      int av = attributeValue[i];
      String attrText = av == 0 ? "Default" : values[av & 0x0F];
      String actionText = values[action[i] & 0x0F];
      String out =
          attrText.equals (actionText) ? attrText : attrText + " -> " + actionText;
      text.append (String.format ("%n  val/actn   : %02X/%02X - %s", attributeValue[i],
                                  action[i], out));
    }

    return text.toString ();
  }
}