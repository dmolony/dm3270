package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.Charset;
import com.bytezone.dm3270.replyfield.QueryReplyField;

public class QueryReplySF extends StructuredField {

  private final QueryReplyField queryReplyField;

  public QueryReplySF(byte[] buffer, int offset, int length, Charset charset) {
    super(buffer, offset, length, charset);
    assert data[0] == StructuredField.QUERY_REPLY;
    queryReplyField = QueryReplyField.getReplyField(data, charset);
  }

  public QueryReplyField getQueryReplyField() {
    return queryReplyField;
  }

  @Override
  public String toString() {
    return String.format("Struct Field : %02X QueryReply%n", type)
        + queryReplyField;
  }

}
