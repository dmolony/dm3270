package com.bytezone.dm3270.commands;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.filetransfer.FileTransferInboundSF;
import com.bytezone.dm3270.replyfield.AlphanumericPartitions;
import com.bytezone.dm3270.replyfield.AuxilliaryDevices;
import com.bytezone.dm3270.replyfield.CharacterSets;
import com.bytezone.dm3270.replyfield.Color;
import com.bytezone.dm3270.replyfield.DistributedDataManagement;
import com.bytezone.dm3270.replyfield.Highlight;
import com.bytezone.dm3270.replyfield.ImplicitPartition;
import com.bytezone.dm3270.replyfield.OEMAuxilliaryDevice;
import com.bytezone.dm3270.replyfield.QueryReplyField;
import com.bytezone.dm3270.replyfield.RPQNames;
import com.bytezone.dm3270.replyfield.ReplyModes;
import com.bytezone.dm3270.replyfield.Summary;
import com.bytezone.dm3270.replyfield.UsableArea;
import com.bytezone.dm3270.structuredfields.DefaultStructuredField;
import com.bytezone.dm3270.structuredfields.QueryReplySF;
import com.bytezone.dm3270.structuredfields.StructuredField;
import com.bytezone.dm3270.utilities.Utility;

public class ReadStructuredFieldCommand extends Command
{
  private static Map<String, String> clientNames = new HashMap<> ();

  private final List<StructuredField> fields = new ArrayList<StructuredField> ();
  private static final String line =
      "\n-------------------------------------------------------------------------";

  private String clientName = "";
  private String signature;
  private final List<QueryReplyField> replies = new ArrayList<> ();

  static
  {
    clientNames.put ("53952DB14CBB53CD7C1E5AB1FDFDA193", "tn3270X");
    clientNames.put ("12F0F4557FB72796E8A4398AA694255C", "Vista");
    clientNames.put ("19D8CA4B4B59357FBF37FB9B7F38EC21", "x3270");
    clientNames.put ("F960E103861F3920FC3B8AF00D8B8601", "FreeHost");
    clientNames.put ("C1F30DBA8306E1887C7EE2D976C6B24A", "dm3270 (old1)");
    clientNames.put ("0BA60960D0116F016EBA4D14E610AA39", "Vista2");
    clientNames.put ("08997C53F68A969853867072174CD882", "dm3270 (old2)");
    clientNames.put ("BD47AE1B606E2DF29C7D24DD128648A8", "dm3270");
  }

  public ReadStructuredFieldCommand ()
  {
    this (buildReply (2));
  }

  public ReadStructuredFieldCommand (byte[] buffer)
  {
    this (buffer, 0, buffer.length);
  }

  public ReadStructuredFieldCommand (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);

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
          QueryReplySF queryReply = new QueryReplySF (data, ptr, size);
          fields.add (queryReply);
          replies.add (queryReply.getQueryReplyField ());
          break;

        //        case StructuredField.INBOUND_3270DS:
        //          System.out.println ("***************************** here RSF");
        //          fields.add (new Inbound3270DS (data, ptr, size));
        //          break;

        case StructuredField.IND$FILE:
          fields.add (new FileTransferInboundSF (data, ptr, size));
          break;

        default:
          System.out.printf ("Unknown Structured Field: %02X%n", data[ptr]);
          fields.add (new DefaultStructuredField (data, ptr, size));
      }
      ptr += size;
    }

    if (replies.size () > 0)
    {
      clientName = getClientName (data);
      for (QueryReplyField reply : replies)
        reply.addReplyFields (replies);         // allow each QRF to see all the others
    }
  }

  private String getClientName (byte[] buffer)
  {
    try
    {
      byte[] digest = MessageDigest.getInstance ("MD5").digest (buffer);
      signature = DatatypeConverter.printHexBinary (digest);
      String clientName = clientNames.get (signature);
      return clientName == null ? "Unknown" : clientName;
    }
    catch (NoSuchAlgorithmException e)
    {
      e.printStackTrace ();
    }
    return "Unknown";
  }

  public String getClientName ()
  {
    return clientName;
  }

  public List<StructuredField> getFieldList ()
  {
    return fields;
  }

  private static byte[] buildReply (int version)
  {
    Highlight highlight = new Highlight ();
    Color color = new Color ();
    ImplicitPartition partition = new ImplicitPartition ();

    List<QueryReplyField> replyFields = new ArrayList<> ();

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
      replyFields.add (new DistributedDataManagement ());
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
    for (QueryReplyField reply : summary)
      replyLength += reply.replySize ();

    // create the reply record buffer
    byte[] buffer = new byte[replyLength];

    int ptr = 0;
    buffer[ptr++] = (byte) 0x88;      // AID

    // fill buffer with reply components
    for (QueryReplyField reply : summary)
      ptr = reply.packReply (buffer, ptr);

    assert ptr == replyLength;

    return buffer;
  }

  @Override
  public void process (Screen screen)
  {
  }

  @Override
  public String getName ()
  {
    return "Read SF";
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (String.format ("RSF (%d):", fields.size ()));

    if (replies.size () > 0)
    {
      text.append (String.format ("%nChecksum     : %s", signature));
      text.append (String.format ("%nClient name  : %s", clientName));
    }

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