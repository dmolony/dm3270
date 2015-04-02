package com.bytezone.dm3270.replyfield;

public class Segment extends QueryReplyField
{
  public Segment (byte[] buffer)
  {
    super (buffer);

    assert data[1] == SEGMENT_REPLY;
  }
}