package com.bytezone.dm3270.buffers;

import java.util.Optional;

public interface ReplyBuffer extends Buffer {

  Optional<Buffer> getReply();

  void setReply(Buffer reply);

}
