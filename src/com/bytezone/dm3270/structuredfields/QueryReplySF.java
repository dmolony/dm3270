package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.replyfield.QueryReplyField;

public class QueryReplySF extends StructuredField
{
  private QueryReplyField queryReplyField;

  public QueryReplySF (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);
    assert data[0] == StructuredField.QUERY_REPLY;
    //    queryReplyField = QueryReplyField.getReplyField (data);
  }

  public QueryReplyField getQueryReplyField ()
  {
    if (queryReplyField == null)
      queryReplyField = QueryReplyField.getReplyField (data);
    return queryReplyField;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("Struct Field : %02X QueryReply%n", type));
    text.append (queryReplyField);
    return text.toString ();
  }
}