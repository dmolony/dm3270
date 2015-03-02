package com.bytezone.dm3270.commands;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.replyfield.AlphanumericPartitions;
import com.bytezone.dm3270.replyfield.AuxilliaryDevices;
import com.bytezone.dm3270.replyfield.CharacterSets;
import com.bytezone.dm3270.replyfield.Color;
import com.bytezone.dm3270.replyfield.DistributedDataManagement;
import com.bytezone.dm3270.replyfield.Highlight;
import com.bytezone.dm3270.replyfield.ImplicitPartition;
import com.bytezone.dm3270.replyfield.OEMAuxilliaryDevice;
import com.bytezone.dm3270.replyfield.RPQNames;
import com.bytezone.dm3270.replyfield.ReplyField;
import com.bytezone.dm3270.replyfield.ReplyModes;
import com.bytezone.dm3270.replyfield.Summary;
import com.bytezone.dm3270.replyfield.UsableArea;
import com.bytezone.dm3270.structuredfields.DefaultStructuredField;
import com.bytezone.dm3270.structuredfields.Inbound3270DS;
import com.bytezone.dm3270.structuredfields.QueryReplySF;
import com.bytezone.dm3270.structuredfields.StructuredField;

public class ReadStructuredFieldCommand extends Command
{
  private final List<StructuredField> fields = new ArrayList<StructuredField> ();
  private static final String line = "\n----------------------------------------"
      + "---------------------------------";

  public ReadStructuredFieldCommand (ScreenHandler screenHandler, Screen screen)
  {
    this (buildReply (2), screenHandler, screen);
  }

  public ReadStructuredFieldCommand (byte[] buffer, ScreenHandler screenHandler,
      Screen screen)
  {
    this (buffer, 0, buffer.length, screenHandler, screen);
  }

  public ReadStructuredFieldCommand (byte[] buffer, int offset, int length,
      ScreenHandler screenHandler, Screen screen)
  {
    super (buffer, offset, length, screenHandler, screen);

    assert data[0] == (byte) 0x88;

    int ptr = 1;
    int max = data.length;

    while (ptr < max)
    {
      int size = Utility.unsignedShort (data, ptr) - 2;
      ptr += 2;

      switch (data[ptr])
      {
        case StructuredField.QUERY_REPLY:
          fields.add (new QueryReplySF (data, ptr, size, screenHandler, screen));
          break;

        case StructuredField.INBOUND_3270DS:
          System.out.println ("***************************** here RSF");
          fields.add (new Inbound3270DS (data, ptr, size, screenHandler, screen));
          break;

        default:
          System.out.printf ("Unknown Structured Field: %02X%n", data[ptr]);
          fields
              .add (new DefaultStructuredField (data, ptr, size, screenHandler, screen));
      }
      ptr += size;
    }
  }

  public List<StructuredField> getFieldList ()
  {
    return fields;
  }

  @Override
  public String getName ()
  {
    return "Read Structured Field";
  }

  private static byte[] buildReply (int version)
  {
    Highlight highlight = new Highlight ();
    Color color = new Color ();
    ImplicitPartition partition = new ImplicitPartition ();

    List<ReplyField> replyFields = new ArrayList<> ();

    // Freehost
    if (version == 1)
    {
      replyFields.add (color);
      replyFields.add (highlight);
      replyFields.add (partition);
    }
    // Vista
    else if (version == 2)
    {
      replyFields.add (new UsableArea ());
      replyFields.add (new CharacterSets ());
      replyFields.add (color);
      replyFields.add (highlight);
      replyFields.add (new ReplyModes ());
      replyFields.add (new AuxilliaryDevices ());
      replyFields.add (partition);
      replyFields.add (new OEMAuxilliaryDevice ());
    }
    // x3270
    else
    {
      replyFields.add (new UsableArea ());
      replyFields.add (new AlphanumericPartitions ());
      replyFields.add (new CharacterSets ());
      replyFields.add (color);
      replyFields.add (highlight);
      replyFields.add (new ReplyModes ());
      replyFields.add (partition);
      replyFields.add (new DistributedDataManagement ());
      replyFields.add (new RPQNames ());
    }

    Summary summary = new Summary (replyFields);      // adds itself to the list

    // calculate the size of the reply record
    int replyLength = 1;
    for (ReplyField reply : summary)
      replyLength += reply.replySize ();

    // create the reply record buffer
    byte[] buffer = new byte[replyLength];

    int ptr = 0;
    buffer[ptr++] = (byte) 0x88;      // AID

    // fill buffer with reply components
    for (ReplyField reply : summary)
      ptr = reply.packReply (buffer, ptr);

    assert ptr == replyLength;

    return buffer;
  }

  @Override
  public void process ()
  {
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (String.format ("RSF (%d):", fields.size ()));

    for (StructuredField sf : fields)
    {
      text.append (line);
      text.append ("\n");
      text.append (sf);
    }
    if (fields.size () > 0)
      text.append (line);

    return text.toString ();
  }
}