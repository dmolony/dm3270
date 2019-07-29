package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.Charset;
import com.bytezone.dm3270.buffers.AbstractTN3270Command;
import com.bytezone.dm3270.display.Screen;

public abstract class StructuredField extends AbstractTN3270Command {

  public static final byte RESET_PARTITION = 0x00;
  public static final byte READ_PARTITION = 0x01;
  public static final byte ERASE_RESET = 0x03;
  public static final byte SET_REPLY_MODE = 0x09;
  public static final byte ACTIVATE_PARTITION = 0x0E;
  public static final byte OUTBOUND_3270DS = 0x40;

  public static final byte QUERY_REPLY = (byte) 0x81;
  protected final Charset charset;

  protected byte type;

  public StructuredField(byte[] buffer, int offset, int length, Charset charset) {
    super(buffer, offset, length);
    this.charset = charset;
    type = buffer[offset];
  }

  @Override
  public void process(Screen screen) {
  }

  @Override
  public String toString() {
    return String.format("StrF: %s", charset.toHex(data).substring(8));
  }

}
