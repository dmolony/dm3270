package com.bytezone.dm3270.replyfield;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.utilities.Dm3270Utility;

public class CharacterSets extends QueryReplyField
{
  byte flags1;
  byte flags2;
  int defaultSlotWidth;
  int defaultSlotHeight;
  int loadTypes;
  int descriptorLength;
  List<Descriptor> descriptors = new ArrayList<> ();

  public CharacterSets ()
  {
    super (CHARACTER_SETS_REPLY);

    int[] rest = { 0x82, 0x00, 0x07, 0x0C, 0x00, 0x00, 0x00, 0x00, //
                   0x07, 0x00, 0x00, 0x00, 0x02, 0xB9, 0x04, 0x17,  //
                   0x01, 0x00, 0xF1, 0x03, 0xC3, 0x01, 0x36 };
    int ptr = createReply (rest.length);
    for (int b : rest)
      reply[ptr++] = (byte) b;

    checkDataLength (ptr);
  }

  public CharacterSets (byte[] buffer)
  {
    super (buffer);

    assert data[1] == CHARACTER_SETS_REPLY;

    flags1 = data[2];
    flags2 = data[3];
    defaultSlotWidth = data[4] & 0xFF;
    defaultSlotHeight = data[5] & 0xFF;

    for (int i = 0; i < 4; i++)
      loadTypes = ((loadTypes << 8) | (data[i + 6] & 0xFF));

    descriptorLength = data[10] & 0xFF;

    for (int ptr = 11; ptr < data.length; ptr += descriptorLength)
      descriptors.add (new Descriptor (data, ptr, descriptorLength));
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (super.toString ());

    text.append (String.format ("%n  flags1     : %02X", flags1));
    text.append (String.format ("%n  flags2     : %02X", flags2));
    text.append (String.format ("%n  SDW        : %d", defaultSlotWidth));
    text.append (String.format ("%n  SDH        : %d", defaultSlotHeight));
    text.append (String.format ("%n  load types : %08X", loadTypes));
    text.append (String.format ("%n  desc len   : %d", descriptorLength));
    for (Descriptor descriptor : descriptors)
      text.append (String.format ("%n  Descriptor : %n%s", descriptor));

    return text.toString ();
  }

  class Descriptor
  {
    int set;
    byte flags;
    int localCharsetID;
    int slotWidth;
    int slotHeight;
    int startSubsection;
    int endSubsection;
    int cgcsID;

    public Descriptor (byte[] buffer, int offset, int length)
    {
      set = buffer[offset] & 0xFF;
      flags = buffer[offset + 1];
      localCharsetID = buffer[offset + 2] & 0xFF;
      slotWidth = buffer[offset + 3] & 0xFF;
      slotHeight = buffer[offset + 4] & 0xFF;
      startSubsection = buffer[offset + 5] & 0xFF;
      endSubsection = buffer[offset + 6] & 0xFF;
      if (length > 7)
        cgcsID = Dm3270Utility.unsignedLong (buffer, 7);
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("    Set : %d", set));
      text.append (String.format ("%n    flags    : %02X", flags));
      text.append (String.format ("%n    charset  : %02X", localCharsetID));
      text.append (String.format ("%n    slot w   : %02X", slotWidth));
      text.append (String.format ("%n    slot h   : %02X", slotHeight));
      text.append (String.format ("%n    start    : %02X", startSubsection));
      text.append (String.format ("%n    end      : %02X", endSubsection));
      text.append (String.format ("%n    graphics : %d", cgcsID));

      return text.toString ();
    }
  }
}