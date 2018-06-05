package com.bytezone.dm3270.attributes;

import com.bytezone.dm3270.display.ContextManager;
import com.bytezone.dm3270.display.ScreenContext;

import java.util.Optional;

public abstract class Attribute {

  public static final byte XA_RESET = 0x00;
  public static final byte XA_HIGHLIGHTING = 0x41;
  public static final byte XA_FGCOLOR = 0x42;
  public static final byte XA_CHARSET = 0x43;
  public static final byte XA_BGCOLOR = 0x45;
  public static final byte XA_TRANSPARENCY = 0x46;
  public static final byte XA_START_FIELD = (byte) 0xC0;

  private static final byte XA_VALIDATION = (byte) 0xC1;
  private static final byte XA_OUTLINING = (byte) 0xC2;

  protected final byte attributeValue;

  private final AttributeType attributeType;
  private final byte attributeCode;

  public enum AttributeType {
    START_FIELD, HIGHLIGHT, FOREGROUND_COLOR, BACKGROUND_COLOR, RESET
  }

  public Attribute(AttributeType attributeType, byte attributeCode, byte attributeValue) {
    this.attributeType = attributeType;
    this.attributeCode = attributeCode;
    this.attributeValue = attributeValue;
  }

  public boolean matches(byte... types) {
    for (byte type : types) {
      if (attributeCode == type) {
        return true;
      }
    }
    return false;
  }

  public int pack(byte[] buffer, int offset) {
    buffer[offset++] = attributeCode;
    buffer[offset++] = attributeValue;
    return offset;
  }

  public abstract ScreenContext process(ContextManager contextManager, ScreenContext defaultContext,
                                        ScreenContext currentContext);

  public byte getAttributeValue() {
    return attributeValue;
  }

  public AttributeType getAttributeType() {
    return attributeType;
  }

  public static Optional<Attribute> getAttribute(byte attributeCode, byte attributeValue) {
    switch (attributeCode) {
      case 0:
        return Optional.of(new ResetAttribute(attributeValue));
      case XA_START_FIELD:
        return Optional.of(new StartFieldAttribute(attributeValue));
      case XA_HIGHLIGHTING:
        return Optional.of(new ExtendedHighlight(attributeValue));
      case XA_BGCOLOR:
        return Optional.of(new BackgroundColor(attributeValue));
      case XA_FGCOLOR:
        return Optional.of(new ForegroundColor(attributeValue));
      case XA_CHARSET:
        System.out.println("Charset not written");
        return Optional.empty();
      case XA_VALIDATION:
        System.out.println("Validation not written");
        return Optional.empty();
      case XA_OUTLINING:
        System.out.println("Outlining not written");
        return Optional.empty();
      case XA_TRANSPARENCY:
        System.out.println("Transparency not written");
        return Optional.empty();
      default:
        System.out.printf("Unknown attribute: %02X%n", attributeCode);
        return Optional.empty();
    }
  }

  protected String name() {
    return getTypeName(attributeCode);
  }

  public static String getTypeName(byte type) {
    switch (type) {
      case XA_RESET:
        return "Reset";
      case XA_HIGHLIGHTING:
        return "Highlight";
      case XA_FGCOLOR:
        return "Foreground";
      case XA_BGCOLOR:
        return "Background";
      case XA_TRANSPARENCY:
        return "Transparency";
      case XA_START_FIELD:
        return "Start Field";
      case XA_VALIDATION:
        return "Validation";
      case XA_OUTLINING:
        return "Outlining";
      case XA_CHARSET:
        return "Charset";
      default:
        return "Unknown";
    }
  }

  @Override
  public String toString() {
    return String.format("%-12s : %02X", name(), attributeValue);
  }

}
