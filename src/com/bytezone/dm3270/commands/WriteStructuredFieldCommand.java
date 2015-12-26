package com.bytezone.dm3270.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bytezone.dm3270.buffers.Buffer;
import com.bytezone.dm3270.buffers.MultiBuffer;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.filetransfer.FileTransferOutboundSF;
import com.bytezone.dm3270.structuredfields.DefaultStructuredField;
import com.bytezone.dm3270.structuredfields.EraseResetSF;
import com.bytezone.dm3270.structuredfields.Outbound3270DS;
import com.bytezone.dm3270.structuredfields.ReadPartitionSF;
import com.bytezone.dm3270.structuredfields.SetReplyModeSF;
import com.bytezone.dm3270.structuredfields.StructuredField;
import com.bytezone.dm3270.utilities.Dm3270Utility;

public class WriteStructuredFieldCommand extends Command
{
  private static final String line =
      "\n-------------------------------------------------------------------------";

  private final List<StructuredField> structuredFields =
      new ArrayList<StructuredField> ();
  private final List<Buffer> replies = new ArrayList<> ();

  public WriteStructuredFieldCommand (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);

    assert buffer[offset] == Command.WRITE_STRUCTURED_FIELD_11
        || buffer[offset] == Command.WRITE_STRUCTURED_FIELD_F3;

    int ptr = offset + 1;
    int max = offset + length;

    while (ptr < max)
    {
      int size = Dm3270Utility.unsignedShort (buffer, ptr) - 2;
      ptr += 2;

      switch (buffer[ptr])
      {
        // wrapper for original write commands - W. EW, EWA, EAU
        case StructuredField.OUTBOUND_3270DS:
          structuredFields.add (new Outbound3270DS (buffer, ptr, size));
          break;

        // wrapper for original read commands - RB, RM, RMA
        case StructuredField.READ_PARTITION:
          structuredFields.add (new ReadPartitionSF (buffer, ptr, size));
          break;

        case StructuredField.RESET_PARTITION:
          System.out.println ("SF_RESET_PARTITION (00) not written yet");
          structuredFields.add (new DefaultStructuredField (buffer, ptr, size));
          break;

        case StructuredField.SET_REPLY_MODE:
          structuredFields.add (new SetReplyModeSF (buffer, ptr, size));
          break;

        case StructuredField.ACTIVATE_PARTITION:
          System.out.println ("SF_ACTIVATE_PARTITION (0E) not written yet");
          structuredFields.add (new DefaultStructuredField (buffer, ptr, size));
          break;

        case StructuredField.ERASE_RESET:
          structuredFields.add (new EraseResetSF (buffer, ptr, size));
          break;

        case StructuredField.IND$FILE:
          structuredFields.add (new FileTransferOutboundSF (buffer, ptr, size));
          break;

        default:
          structuredFields.add (new DefaultStructuredField (buffer, ptr, size));
          break;
      }

      ptr += size;
    }
  }

  @Override
  public void process (Screen screen)
  {
    replies.clear ();

    for (StructuredField structuredField : structuredFields)
    {
      structuredField.process (screen);
      Optional<Buffer> reply = structuredField.getReply ();
      if (reply.isPresent ())
        replies.add (reply.get ());
    }
  }

  @Override
  public Optional<Buffer> getReply ()
  {
    if (replies.size () == 0)
      return Optional.empty ();

    if (replies.size () == 1)
      return Optional.of (replies.get (0));

    MultiBuffer multiBuffer = new MultiBuffer ();
    for (Buffer reply : replies)
      multiBuffer.addBuffer (reply);

    return Optional.of (multiBuffer);
  }

  @Override
  public String brief ()
  {
    StringBuilder text =
        new StringBuilder (String.format ("WSF (%d):", structuredFields.size ()));

    for (StructuredField sf : structuredFields)
      text.append ("\n       : " + sf.brief ());

    return text.toString ();
  }

  @Override
  public String getName ()
  {
    return "Write SF";
  }

  @Override
  public String toString ()
  {
    StringBuilder text =
        new StringBuilder (String.format ("WSF (%d):", structuredFields.size ()));

    for (StructuredField sf : structuredFields)
    {
      text.append (line);
      text.append ("\n");
      text.append (sf);
    }

    if (structuredFields.size () > 0)
      text.append (line);

    return text.toString ();
  }
}