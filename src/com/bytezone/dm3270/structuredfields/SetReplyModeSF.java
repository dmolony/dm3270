package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.display.Screen;

public class SetReplyModeSF extends StructuredField
{
  public static final byte RM_FIELD = 0x00;
  public static final byte RM_EXTENDED_FIELD = 0x01;
  public static final byte RM_CHARACTER = 0x02;

  private static final String[] modes = { "Field", "Extended field", "Character" };
  private final byte partition;
  private final byte replyMode;
  private final byte[] types;

  public SetReplyModeSF (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);

    assert data[0] == StructuredField.SET_REPLY_MODE;

    int ptr = offset + 1;
    partition = buffer[ptr++];
    replyMode = buffer[ptr++];

    int totalTypes = length - 3;
    types = new byte[totalTypes];
    System.arraycopy (buffer, ptr, types, 0, types.length);
  }

  @Override
  public void process (Screen screen)
  {
    screen.setReplyMode (replyMode, types);
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ("Struct Field : 09 Set Reply Mode\n");
    text.append (String.format ("   partition : %02X%n", partition));
    text.append (String.format ("   mode      : %02X %s mode", replyMode,
                                modes[replyMode]));
    for (byte type : types)
    {
      String typeName = Attribute.getTypeName (type);
      text.append (String.format ("%n   type      : %02X %s", type, typeName));
    }

    return text.toString ();
  }
}