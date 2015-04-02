package com.bytezone.dm3270.attributes;

import com.bytezone.dm3270.display.ContextManager;
import com.bytezone.dm3270.display.ScreenContext;

public abstract class Attribute
{
  public final static byte XA_RESET = 0x00;
  public final static byte XA_HIGHLIGHTING = 0x41;
  public final static byte XA_FGCOLOR = 0x42;
  public final static byte XA_CHARSET = 0x43;
  public final static byte XA_BGCOLOR = 0x45;
  public final static byte XA_TRANSPARENCY = 0x46;
  public final static byte XA_START_FIELD = (byte) 0xC0;
  public final static byte XA_VALIDATION = (byte) 0xC1;
  public final static byte XA_OUTLINING = (byte) 0xC2;

  protected final AttributeType attributeType;
  protected final byte attributeCode;
  protected final byte attributeValue;

  public enum AttributeType
  {
    START_FIELD, HIGHLIGHT, FOREGROUND_COLOR, BACKGROUND_COLOR, RESET
  }

  public Attribute (AttributeType attributeType, byte attributeCode, byte attributeValue)
  {
    this.attributeType = attributeType;
    this.attributeCode = attributeCode;
    this.attributeValue = attributeValue;
  }

  public String name ()
  {
    switch (attributeType)
    {
      case START_FIELD:
        return "Start field";
      case HIGHLIGHT:
        return "Highlight";
      case FOREGROUND_COLOR:
        return "Foreground";
      case BACKGROUND_COLOR:
        return "Background";
      case RESET:
        return "Reset";
      default:
        return "Unknown";
    }
  }

  public boolean matches (byte... types)
  {
    for (byte type : types)
      if (attributeCode == type)
        return true;
    return false;
  }

  public int pack (byte[] buffer, int offset)
  {
    switch (attributeType)
    {
      case RESET:
        buffer[offset++] = XA_RESET;
        break;

      case START_FIELD:
        buffer[offset++] = XA_START_FIELD;
        break;

      case HIGHLIGHT:
        buffer[offset++] = XA_HIGHLIGHTING;
        break;

      case FOREGROUND_COLOR:
        buffer[offset++] = XA_FGCOLOR;
        break;

      case BACKGROUND_COLOR:
        buffer[offset++] = XA_BGCOLOR;
        break;

      default:
        buffer[offset++] = 0x00;
        System.out.println ("***** pack not written *****");
    }
    buffer[offset++] = attributeValue;
    return offset;
  }

  public abstract ScreenContext process (ContextManager contextHandler,
      ScreenContext screenContext);

  public byte getAttributeValue ()
  {
    return attributeValue;
  }

  public AttributeType getAttributeType ()
  {
    return attributeType;
  }

  public static Attribute getAttribute (byte attributeCode, byte attributeValue)
  {
    switch (attributeCode)
    {
      case 0:
        return new ResetAttribute (attributeValue);
      case XA_START_FIELD:
        return new StartFieldAttribute (attributeValue);
      case XA_HIGHLIGHTING:
        return new ExtendedHighlight (attributeValue);
      case XA_BGCOLOR:
        return new BackgroundColor (attributeValue);
      case XA_FGCOLOR:
        return new ForegroundColor (attributeValue);
      case XA_CHARSET:
        System.out.println ("Charset not written");
        return null;
      case XA_VALIDATION:
        System.out.println ("Validation not written");
        return null;
      case XA_OUTLINING:
        System.out.println ("Outlining not written");
        return null;
      case XA_TRANSPARENCY:
        System.out.println ("Transparency not written");
        return null;
      default:
        System.out.printf ("Unknown attribute: %02X%n", attributeCode);
        return null;
    }
  }

  @Override
  public String toString ()
  {
    return String.format ("%-12s : %02X", name (), attributeValue);
  }
}