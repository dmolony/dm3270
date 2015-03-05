package com.bytezone.dm3270.replyfield;

public class Transparency extends QueryReplyField
{
  public Transparency (byte[] buffer)
  {
    super (buffer);

    assert data[1] == TRANSPARENCY_REPLY;
  }
}