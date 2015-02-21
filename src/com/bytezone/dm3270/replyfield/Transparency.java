package com.bytezone.dm3270.replyfield;

public class Transparency extends ReplyField
{
  public Transparency (byte[] buffer)
  {
    super (buffer);

    assert data[1] == TRANSPARENCY_REPLY;
  }
}