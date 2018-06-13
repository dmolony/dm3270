package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.replyfield.CharacterSets;
import com.bytezone.dm3270.replyfield.Color;
import com.bytezone.dm3270.replyfield.Highlight;
import com.bytezone.dm3270.replyfield.ImplicitPartition;
import com.bytezone.dm3270.replyfield.QueryReplyField;
import com.bytezone.dm3270.replyfield.ReplyModes;
import com.bytezone.dm3270.replyfield.UsableArea;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.structuredfields.DefaultStructuredField;
import com.bytezone.dm3270.structuredfields.QueryReplySF;
import com.bytezone.dm3270.structuredfields.StructuredField;
import com.bytezone.dm3270.utilities.Dm3270Utility;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadStructuredFieldCommand extends Command {

  private static final Logger LOG = LoggerFactory.getLogger(ReadStructuredFieldCommand.class);

  private static final String SEPARATOR =
      "\n-------------------------------------------------------------------------";

  private final List<StructuredField> structuredFields = new ArrayList<>();

  private ScreenDimensions screenDimensions;

  public ReadStructuredFieldCommand(TelnetState telnetState) {
    this(buildReply(telnetState));
  }

  private ReadStructuredFieldCommand(byte[] buffer) {
    this(buffer, 0, buffer.length);
  }

  public ReadStructuredFieldCommand(byte[] buffer, int offset, int length) {
    super(buffer, offset, length);

    assert data[0] == AIDCommand.AID_STRUCTURED_FIELD;

    int ptr = 1;
    int max = data.length;

    List<QueryReplyField> replies = new ArrayList<>();
    while (ptr < max) {
      int size = Dm3270Utility.unsignedShort(data, ptr) - 2;
      ptr += 2;

      switch (data[ptr]) {
        case StructuredField.QUERY_REPLY:
          QueryReplySF queryReply = new QueryReplySF(data, ptr, size);
          structuredFields.add(queryReply);
          replies.add(queryReply.getQueryReplyField());
          break;

        default:
          LOG.warn("Unknown Structured Field: {}", Dm3270Utility.toHex(data, ptr, 1, false));
          structuredFields.add(new DefaultStructuredField(data, ptr, size));
      }
      ptr += size;
    }

    if (replies.size() > 0) {
      for (QueryReplyField reply : replies) {
        reply.addReplyFields(replies);         // allow each QRF to see all the others
        if (screenDimensions == null && reply instanceof UsableArea) {
          screenDimensions = ((UsableArea) reply).getScreenDimensions();
        }
      }
    }
  }

  private static byte[] buildReply(TelnetState telnetState) {
    Highlight highlight = new Highlight();
    Color color = new Color();

    ScreenDimensions screenDimensions = telnetState.getSecondary();
    ImplicitPartition partition =
        new ImplicitPartition(screenDimensions.rows, screenDimensions.columns);

    List<QueryReplyField> replyFields = new ArrayList<>();

    replyFields.add(color);
    replyFields.add(highlight);
    replyFields.add(new UsableArea(screenDimensions.rows, screenDimensions.columns));
    replyFields.add(partition);
    replyFields.add(new ReplyModes());
    replyFields.add(new CharacterSets());

    // calculate the size of the reply record
    int replyLength = 1;
    for (QueryReplyField reply : replyFields) {
      replyLength += reply.replySize();
    }

    // create the reply record buffer
    byte[] buffer = new byte[replyLength];

    int ptr = 0;
    buffer[ptr++] = AIDCommand.AID_STRUCTURED_FIELD;

    // fill buffer with reply components
    for (QueryReplyField reply : replyFields) {
      ptr = reply.packReply(buffer, ptr);
    }

    assert ptr == replyLength;

    return buffer;
  }

  @Override
  public void process(Screen screen) {
  }

  @Override
  public String getName() {
    return "Read SF";
  }

  @Override
  public String toString() {
    StringBuilder text =
        new StringBuilder(String.format("RSF (%d):", structuredFields.size()));

    for (StructuredField sf : structuredFields) {
      text.append(SEPARATOR);
      text.append("\n");
      text.append(sf);
    }

    if (structuredFields.size() > 0) {
      text.append(SEPARATOR);
    }

    return text.toString();
  }

}
