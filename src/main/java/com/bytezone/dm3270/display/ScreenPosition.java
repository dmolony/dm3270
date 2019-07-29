package com.bytezone.dm3270.display;

import com.bytezone.dm3270.Charset;
import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import java.util.ArrayList;
import java.util.List;

public final class ScreenPosition {

  // GraphicsEscape characters
  private static final byte TOP_LEFT = (byte) 0xC5;
  private static final byte TOP_RIGHT = (byte) 0xD5;
  private static final byte BOTTOM_LEFT = (byte) 0xC4;
  private static final byte BOTTOM_RIGHT = (byte) 0xD4;
  private static final byte HORIZONTAL_LINE = (byte) 0xA2;
  private static final byte VERTICAL_LINE = (byte) 0x85;

  private final int position;

  private StartFieldAttribute startFieldAttribute;
  private final List<Attribute> attributes = new ArrayList<>();

  private byte value;
  private boolean isGraphics;
  private ScreenContext screenContext;
  private final Charset charset;

  public ScreenPosition(int position, ScreenContext screenContext,
      Charset charset) {
    this.position = position;
    this.screenContext = screenContext;
    this.charset = charset;
    reset();
  }

  public void reset() {
    value = 0;
    isGraphics = false;
    startFieldAttribute = null;
    attributes.clear();
  }

  public void setChar(byte value) {
    this.value = value;
    isGraphics = false;
  }

  public void setGraphicsChar(byte value) {
    this.value = value;
    isGraphics = true;
  }

  public StartFieldAttribute getStartFieldAttribute() {
    return startFieldAttribute;
  }

  public void setStartField(StartFieldAttribute startFieldAttribute) {
    if (startFieldAttribute == null) {
      if (this.startFieldAttribute != null) {
        attributes.clear();
      }
    }
    this.startFieldAttribute = startFieldAttribute;
  }

  public void addAttribute(Attribute attribute) {
    attributes.add(attribute);
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public int getPosition() {
    return position;
  }

  // All the colour and highlight options
  public void setScreenContext(ScreenContext screenContext) {
    if (screenContext == null) {
      throw new IllegalArgumentException("ScreenContext cannot be null");
    }
    this.screenContext = screenContext;
  }

  public ScreenContext getScreenContext() {
    return screenContext;
  }

  public boolean isStartField() {
    return startFieldAttribute != null;
  }

  public boolean isGraphicsChar() {
    return isGraphics;
  }

  public char getChar() {
    if (value == 0) {
      return '\u0000';
    }
    if ((value & 0xC0) == 0) {
      return ' ';
    }

    if (isGraphics) {
      switch (value) {
        case HORIZONTAL_LINE:
          return '-';
        case VERTICAL_LINE:
          return '|';
        default:
          return '*';
      }
    }

    return charset.getChar(value);
  }

  public String getCharString() {
    if (isStartField()) {
      return " ";
    }

    if (isGraphics) {
      switch (value) {
        case HORIZONTAL_LINE:
          return "-";
        case VERTICAL_LINE:
          return "|";
        case TOP_LEFT:
        case TOP_RIGHT:
        case BOTTOM_LEFT:
        case BOTTOM_RIGHT:
          return "*";
        default:
          return ".";
      }
    }

    char ret = charset.getChar(value);
    return ret < ' ' ? " " : String.valueOf(ret);
  }

  public byte getByte() {
    return value;
  }

  public boolean isNull() {
    return value == 0;
  }

  @Override
  public String toString() {
    StringBuilder text = new StringBuilder();
    if (isStartField()) {
      text.append("..").append(startFieldAttribute);
    } else {
      for (Attribute attribute : attributes) {
        text.append("--").append(attribute);
      }
    }

    text.append(", byte: ").append(getCharString());

    return text.toString();
  }

}
