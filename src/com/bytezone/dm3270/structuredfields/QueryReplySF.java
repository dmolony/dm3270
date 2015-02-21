package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.replyfield.ReplyField;

public class QueryReplySF extends StructuredField
{
  private final ReplyField replyField;

  public QueryReplySF (byte[] buffer, int offset, int length, ScreenHandler screenHandler)
  {
    super (buffer, offset, length, screenHandler);
    assert data[0] == StructuredField.QUERY_REPLY;
    replyField = ReplyField.getReplyField (data);     // not a reply in the usual sense
  }

  public ReplyField getReplyField ()
  {
    return replyField;
  }

  @Override
  public void process ()
  {
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("Struct Field : %02X QueryReply\n", type));
    text.append (replyField);
    return text.toString ();
  }
}