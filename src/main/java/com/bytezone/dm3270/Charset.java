package com.bytezone.dm3270;

import com.bytezone.dm3270.buffers.Buffer;
import java.nio.charset.UnsupportedCharsetException;

public enum Charset {
  CP1025,
  CP1026,
  CP1047,
  CP1140,
  CP1141,
  CP1142,
  CP1143,
  CP1144,
  CP1145,
  CP1146,
  CP1147,
  CP1148,
  CP1149,
  CP1153,
  CP1154,
  CP1166,
  CP1377,
  CP850,
  CP870,
  CP930,
  CP931,
  CP935,
  CP937,
  CP939;

  private char[] charsMapping;
  private java.nio.charset.Charset charset;

  public synchronized void load() throws UnsupportedCharsetException  {
    if (charset != null) {
      return;
    }
    charset = java.nio.charset.Charset.forName(name());
    byte[] baseBytes = new byte[256];
    for (int i = 0; i < 256; i++) {
      baseBytes[i] = (byte) i;
    }
    charsMapping = new String(baseBytes, charset).toCharArray();
  }

  public char getChar(byte value) {
    return charsMapping[value & 0xFF];
  }

  public String getString(byte[] buffer) {
    return new String(buffer, charset);
  }

  public String getString(byte[] buffer, int offset, int length) {
    return new String(buffer,
        offset + length > buffer.length ? buffer.length - offset - 1 : offset,
        length, charset);
  }

  public String toHex(byte[] b) {
    return toHex(b, 0, b.length);
  }

  public String toHex(byte[] b, int offset, int length) {
    StringBuilder text = new StringBuilder();
    for (int ptr = offset, max = offset + length; ptr < max; ptr += Buffer.HEX_LINE_SIZE) {
      StringBuilder hexLine = new StringBuilder();
      StringBuilder textLine = new StringBuilder();
      for (int linePtr = 0; linePtr < Buffer.HEX_LINE_SIZE && ptr + linePtr < max; linePtr++) {
        int val = b[ptr + linePtr] & 0xFF;
        hexLine.append(String.format("%02X ", val));
        if (val < 0x40 || val == 0xFF) {
          textLine.append('.');
        } else {
          textLine.append(new String(b, ptr + linePtr, 1, charset));
        }
      }
      text.append(String.format("%04X  %-48s %s%n", ptr, hexLine.toString(), textLine.toString()));
    }
    return text.length() > 0 ? text.substring(0, text.length() - 1) : text.toString();
  }

}
