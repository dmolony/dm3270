package com.bytezone.dm3270.replyfield;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Summary extends QueryReplyField implements Iterable<QueryReplyField>
{
  List<QueryReplyField> replyList;        // replies we build

  public Summary (List<QueryReplyField> replies)
  {
    super (SUMMARY_QUERY_REPLY);

    replyList = new ArrayList<> (replies.size () + 1);
    replyList.add (this);
    replyList.addAll (replies);

    int ptr = createReply (replyList.size ());
    for (QueryReplyField replyField : replyList)
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

  protected boolean isListed (byte type)
  {
    for (int i = 2; i < data.length; i++)
      if (data[i] == type)
        return true;
    return false;
  }

  @Override
  public Iterator<QueryReplyField> iterator ()
  {
    return replyList.iterator ();
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (super.toString ());

    for (int i = 2; i < data.length; i++)
      text.append (String.format ("%n  %-30s  %s", getReplyType (data[i]),
                                  isProvided (data[i]) ? "" : "** missing **"));

    // check for QueryReplyFields sent but not listed in the summary
    List<QueryReplyField> missingFields = new ArrayList<> (4);
    for (QueryReplyField reply : replies)
      if (!isListed (reply.replyType.type))
        missingFields.add (reply);

    if (missingFields.size () > 0)
    {
      text.append ("\n\nNot listed in Summary:");
      for (QueryReplyField qrf : missingFields)
        text.append (String.format ("%n  %s", qrf.replyType));
    }

    return text.toString ();
  }
}