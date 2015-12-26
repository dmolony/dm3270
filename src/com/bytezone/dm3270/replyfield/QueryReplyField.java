package com.bytezone.dm3270.replyfield;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.bytezone.dm3270.structuredfields.StructuredField;
import com.bytezone.dm3270.utilities.Dm3270Utility;

public abstract class QueryReplyField
{
  public final static byte SUMMARY_QUERY_REPLY = (byte) 0x80;
  public final static byte USABLE_AREA_REPLY = (byte) 0x81;
  public final static byte ALPHANUMERIC_PARTITIONS_REPLY = (byte) 0x84;
  public final static byte CHARACTER_SETS_REPLY = (byte) 0x85;
  public final static byte COLOR_QUERY_REPLY = (byte) 0x86;
  public final static byte HIGHLIGHT_QUERY_REPLY = (byte) 0x87;
  public final static byte REPLY_MODES_REPLY = (byte) 0x88;
  public final static byte OEM_AUXILLIARY_DEVICE_REPLY = (byte) 0x8F;
  public final static byte DISTRIBUTED_DATA_MANAGEMENT_REPLY = (byte) 0x95;
  public final static byte STORAGE_POOLS_REPLY = (byte) 0x96;
  public final static byte AUXILLIARY_DEVICE_REPLY = (byte) 0x99;
  public final static byte RPQ_NAMES_REPLY = (byte) 0xA1;
  public final static byte IMP_PART_QUERY_REPLY = (byte) 0xA6;
  public final static byte TRANSPARENCY_REPLY = (byte) 0xA8;
  public final static byte SEGMENT_REPLY = (byte) 0xB0;
  public final static byte PROCEDURE_REPLY = (byte) 0xB1;
  public final static byte LINE_TYPE_REPLY = (byte) 0xB2;
  public final static byte PORT_REPLY = (byte) 0xB3;
  public final static byte GRAPHIC_COLOR_REPLY = (byte) 0xB4;
  public final static byte GRAPHIC_SYMBOL_SETS_REPLY = (byte) 0xB6;

  protected byte[] data;                            // data read from a saved session
  protected final ReplyType replyType;
  protected byte[] reply;                           // data created by us
  protected List<QueryReplyField> replies;          // actual replies from REPLAY

  // change this to a treemap or an enum
  public static final List<ReplyType> replyTypes =
      Arrays.asList (new ReplyType (SUMMARY_QUERY_REPLY, "Summary"),
                     new ReplyType (USABLE_AREA_REPLY, "Usable Area"),
                     new ReplyType (ALPHANUMERIC_PARTITIONS_REPLY,
                         "Alphanumeric Partitions"),
                     new ReplyType (CHARACTER_SETS_REPLY, "Character Sets"),
                     new ReplyType (COLOR_QUERY_REPLY, "Color"),
                     new ReplyType (HIGHLIGHT_QUERY_REPLY, "Highlight"),
                     new ReplyType (REPLY_MODES_REPLY, "Reply Modes"),
                     new ReplyType (OEM_AUXILLIARY_DEVICE_REPLY, "OEM Aux Devices"),
                     new ReplyType (DISTRIBUTED_DATA_MANAGEMENT_REPLY,
                         "Distributed Data Management"),
                     new ReplyType (STORAGE_POOLS_REPLY, "Storage Pools"),
                     new ReplyType (AUXILLIARY_DEVICE_REPLY, "Auxilliary Devices"),
                     new ReplyType (RPQ_NAMES_REPLY, "RPQ Names"),
                     new ReplyType (IMP_PART_QUERY_REPLY, "Implicit Partition"),
                     new ReplyType (TRANSPARENCY_REPLY, "Transparency"),
                     new ReplyType (SEGMENT_REPLY, "Segment"),
                     new ReplyType (PROCEDURE_REPLY, "Procedure"),
                     new ReplyType (LINE_TYPE_REPLY, "Line Type"),
                     new ReplyType (PORT_REPLY, "Port"),
                     new ReplyType (GRAPHIC_COLOR_REPLY, "Graphic Color"),
                     new ReplyType (GRAPHIC_SYMBOL_SETS_REPLY, "Graphic Symbol Sets"));

  public static QueryReplyField getReplyField (byte[] buffer)
  {
    assert buffer[0] == StructuredField.QUERY_REPLY;

    switch (buffer[1])
    {
      case SUMMARY_QUERY_REPLY:
        return new Summary (buffer);

      case USABLE_AREA_REPLY:
        return new UsableArea (buffer);

      case ALPHANUMERIC_PARTITIONS_REPLY:
        return new AlphanumericPartitions (buffer);

      case CHARACTER_SETS_REPLY:
        return new CharacterSets (buffer);

      case COLOR_QUERY_REPLY:
        return new Color (buffer);

      case HIGHLIGHT_QUERY_REPLY:
        return new Highlight (buffer);

      case REPLY_MODES_REPLY:
        return new ReplyModes (buffer);

      case OEM_AUXILLIARY_DEVICE_REPLY:
        return new OEMAuxilliaryDevice (buffer);

      case DISTRIBUTED_DATA_MANAGEMENT_REPLY:
        return new DistributedDataManagement (buffer);

      case AUXILLIARY_DEVICE_REPLY:
        return new AuxilliaryDevices (buffer);

      case RPQ_NAMES_REPLY:
        return new RPQNames (buffer);

      case IMP_PART_QUERY_REPLY:
        return new ImplicitPartition (buffer);

      case TRANSPARENCY_REPLY:
        return new Transparency (buffer);

      case SEGMENT_REPLY:
        return new Segment (buffer);

      default:
        return new DefaultReply (buffer);
    }
  }

  public QueryReplyField (byte replyType)
  {
    this.replyType = getReplyType (replyType);
  }

  public QueryReplyField (byte[] buffer)
  {
    assert buffer[0] == (byte) 0x81;
    data = buffer;
    replyType = getReplyType (buffer[1]);
  }

  // Called from ReadStructuredFieldCommand when in REPLAY mode
  public void addReplyFields (List<QueryReplyField> replies)
  {
    this.replies = replies;
  }

  protected boolean isProvided (byte type)
  {
    for (QueryReplyField reply : replies)
      if (reply.replyType.type == type)
        return true;
    return false;
  }

  protected Optional<Summary> getSummary ()
  {
    for (QueryReplyField reply : replies)
      if (reply.replyType.type == SUMMARY_QUERY_REPLY)
        return Optional.of ((Summary) reply);
    return Optional.empty ();
  }

  protected int createReply (int size)
  {
    size += 4;                              // we add 4 bytes at the beginning
    reply = new byte[size];
    int ptr = Dm3270Utility.packUnsignedShort (size, reply, 0);
    reply[ptr++] = (byte) 0x81;
    reply[ptr++] = replyType.type;
    return ptr;                             // next location to fill
  }

  public ReplyType getReplyType ()
  {
    return replyType;
  }

  public ReplyType getReplyType (byte type)
  {
    for (ReplyType rt : replyTypes)
      if (type == rt.type)
        return rt;
    return new ReplyType (type, "Unknown Reply Type");
  }

  public int packReply (byte[] buffer, int ptr)
  {
    System.arraycopy (reply, 0, buffer, ptr, reply.length);
    ptr += reply.length;
    return ptr;
  }

  protected boolean checkDataLength (int ptr)
  {
    assert ptr == reply.length : String.format ("Length:%d, Ptr:%d", reply.length, ptr);
    return ptr == reply.length;
  }

  public int replySize ()
  {
    return reply.length;
  }

  public String brief ()
  {
    return String.format ("Type  : %02X %s", data[1], replyType.name);
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    Optional<Summary> summary = getSummary ();
    String message = summary.isPresent ()
        ? summary.get ().isListed (replyType.type) ? "" : "** missing **" : "no summary";
    text.append (String.format ("  Type       : %-30s %s%n", replyType, message));
    return text.toString ();
  }

  public static class ReplyType
  {
    public final byte type;
    public final String name;

    public ReplyType (byte type, String name)
    {
      this.type = type;
      this.name = name;
    }

    @Override
    public String toString ()
    {
      return String.format ("%02X %s", type, name);
    }
  }
}