package com.bytezone.dm3270.structuredfields;

import com.bytezone.dm3270.display.Screen;

public class EraseResetSF extends StructuredField {

  private byte flags;
  private Size size;

  private enum Size {
    DEFAULT, ALTERNATE
  }

  public EraseResetSF(byte[] buffer, int offset, int length) {
    super(buffer, offset, length);

    assert data[0] == StructuredField.ERASE_RESET;
    flags = data[1];

    if ((flags & 0xC0) == 0) {
      size = Size.DEFAULT;
    } else {
      size = Size.ALTERNATE;
    }
  }

  @Override
  public void process(Screen screen) {
  }

  @Override
  public String toString() {
    return "Struct Field : 03 Erase/Reset\n" + String
        .format("   flags     : %02X (%s)", flags, size);
  }

}
