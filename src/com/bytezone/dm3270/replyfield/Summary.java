package com.bytezone.dm3270.replyfield;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Summary extends ReplyField implements Iterable<ReplyField>
{
  List<ReplyField> replyList;

  public Summary (List<ReplyField> replies)
  {
    super (SUMMARY_QUERY_REPLY);

    replyList = new ArrayList<> (replies.size () + 1);
    replyList.add (this);
    replyList.addAll (replies);

    int ptr = createReply (replyList.size ());
    for (ReplyField replyField : replyList)
      reply[ptr++] = replyField.replyType.type;

    checkDataLength (ptr);
  }

  public Summary (byte[] buffer)
  {
    super (buffer);
    assert data[1] == SUMMARY_QUERY_REPLY;
    replyList = new ArrayList<> (buffer.length - 2);
  }

  public int size ()
  {
    if (replyList.size () > 0)
      return replyList.size ();
    return data.length - 2;
  }

  @Override
  public Iterator<ReplyField> iterator ()
  {
    return replyList.iterator ();
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (super.toString ());

    for (int i = 2; i < data.length; i++)
      text.append (String.format ("%n  %02X %s", data[i], getReplyType (data[i]).name));

    return text.toString ();
  }
}