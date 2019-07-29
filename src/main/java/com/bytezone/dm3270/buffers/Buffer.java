package com.bytezone.dm3270.buffers;

import com.bytezone.dm3270.display.Screen;

public interface Buffer {

  int HEX_LINE_SIZE = 16;

  static int unsignedShort(byte[] buffer, int offset) {
    return (buffer[offset] & 0xFF) * 0x100 + (buffer[offset + 1] & 0xFF);
  }

  static int packUnsignedShort(int value, byte[] buffer, int offset) {
    buffer[offset++] = (byte) ((value >> 8) & 0xFF);
    buffer[offset++] = (byte) (value & 0xFF);
    return offset;
  }

  static int unsignedLong(byte[] buffer, int offset) {
    return (buffer[offset] & 0xFF) * 0x1000000 + (buffer[offset + 1] & 0xFF) * 0x10000
        + (buffer[offset + 2] & 0xFF) * 0x100 + (buffer[offset + 3] & 0xFF);
  }

  static String toHex(byte[] b, int offset, int length) {
    StringBuilder text = new StringBuilder();
    for (int ptr = offset, max = offset + length; ptr < max; ptr += HEX_LINE_SIZE) {
      StringBuilder hexLine = new StringBuilder();
      StringBuilder textLine = new StringBuilder();
      for (int linePtr = 0; linePtr < HEX_LINE_SIZE && ptr + linePtr < max; linePtr++) {
        int val = b[ptr + linePtr] & 0xFF;
        hexLine.append(String.format("%02X ", val));
        if (val < 0x20 || val >= 0xF0) {
          textLine.append('.');
        } else {
          textLine.append(new String(b, ptr + linePtr, 1));
        }
      }
      text.append(String.format("%04X  %-48s %s%n", ptr, hexLine.toString(), textLine.toString()));
    }
    return text.length() > 0 ? text.substring(0, text.length() - 1) : text.toString();
  }

  byte[] getData();

  byte[] getTelnetData();

  int size();

  void process(Screen screen);

}
