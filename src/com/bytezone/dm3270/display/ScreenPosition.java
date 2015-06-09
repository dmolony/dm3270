package com.bytezone.dm3270.display;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.attributes.StartFieldAttribute;

public final class ScreenPosition
{
  // GraphicsEscape characters
  private static final byte TOP_LEFT = (byte) 0xC5;
  private static final byte TOP_RIGHT = (byte) 0xD5;
  private static final byte BOTTOM_LEFT = (byte) 0xC4;
  private static final byte BOTTOM_RIGHT = (byte) 0xD4;
  private static final byte HORIZONTAL_LINE = (byte) 0xA2;
  private static final byte VERTICAL_LINE = (byte) 0x85;

  private StartFieldAttribute startFieldAttribute;
  //  private final List<Attribute> attributes = new ArrayList<> ();

  private byte value;
  private boolean isGraphics;
  private boolean isVisible = true;
  private ScreenContext screenContext;
  //  private final ScreenContext baseContext;

  private final CharacterSize characterSize;
  private final GraphicsContext gc;

  public ScreenPosition (GraphicsContext gc, CharacterSize characterSize,
      ScreenContext baseContext)
  {
    this.gc = gc;
    this.characterSize = characterSize;
    //    this.baseContext = baseContext;
    screenContext = baseContext;
  }

  //  public void addAttribute (Attribute attribute)
  //  {
  //    if (attribute instanceof StartFieldAttribute)
  //    {
  //      startFieldAttribute = (StartFieldAttribute) attribute;
  //      setVisible (false);
  //    }
  //    else
  //      attributes.add (attribute);
  //  }

  // Password fields etc
  public void setVisible (boolean visible)
  {
    this.isVisible = visible;
  }

  // All the colour and highlight options
  public void setScreenContext (ScreenContext screenContext)
  {
    this.screenContext = screenContext;
  }

  public ScreenContext getScreenContext ()
  {
    return screenContext;
  }

  public boolean isStartField ()
  {
    return startFieldAttribute != null;
  }

  public boolean isGraphicsChar ()
  {
    return isGraphics;
  }

  public StartFieldAttribute getStartFieldAttribute ()
  {
    return startFieldAttribute;
  }

  //  public boolean hasAttributes ()
  //  {
  //    return attributes.size () > 0;
  //  }

  //  public List<Attribute> getAttributes ()
  //  {
  //    return attributes;
  //  }

  public void setStartField (StartFieldAttribute startFieldAttribute)
  {
    this.startFieldAttribute = startFieldAttribute;
  }

  public void setChar (byte value)
  {
    this.value = value;
    isGraphics = false;
  }

  public void setGraphicsChar (byte value)
  {
    this.value = value;
    isGraphics = true;
  }

  public char getChar ()
  {
    if (value == 0 || isStartField ())
      return ' ';

    if (isGraphics)
      switch (value)
      {
        case HORIZONTAL_LINE:
          return '-';
        case VERTICAL_LINE:
          return '|';
        default:
          return '*';
      }

    return (char) Utility.ebc2asc[value & 0xFF];
  }

  public byte getByte ()
  {
    return value;
  }

  public boolean isNull ()
  {
    return value == 0;
  }

  public void reset ()
  {
    isVisible = true;
    value = 0;
    isGraphics = false;
    startFieldAttribute = null;
  }

  //  public int pack (byte[] buffer, int ptr, byte order)
  //  {
  //    assert isStartField ();
  //
  //    buffer[ptr++] = order;
  //
  //    if (order == Order.START_FIELD)
  //      buffer[ptr++] = startFieldAttribute.getAttributeValue ();
  //    else if (order == Order.START_FIELD_EXTENDED)
  //    {
  //      buffer[ptr++] = (byte) (attributes.size () + 1);    // includes the SFA
  //      ptr = startFieldAttribute.pack (buffer, ptr);
  //      for (Attribute attribute : attributes)
  //        ptr = attribute.pack (buffer, ptr);
  //    }
  //    else
  //      System.out.println ("I should throw an exception here");
  //
  //    return ptr;
  //  }

  //  public int pack (byte[] buffer, int ptr, byte[] replyTypes)
  //  {
  //    assert !isStartField ();
  //
  //    for (Attribute attribute : attributes)
  //      if (attribute.matches (Attribute.XA_RESET) || attribute.matches (replyTypes))
  //      {
  //        buffer[ptr++] = Order.SET_ATTRIBUTE;
  //        ptr = attribute.pack (buffer, ptr);       // packs type/value pair
  //      }
  //
  //    buffer[ptr++] = value;
  //
  //    return ptr;
  //  }

  public void draw (int x, int y, boolean hasCursor)
  {
    int charWidth = characterSize.getWidth ();
    int charHeight = characterSize.getHeight ();
    int ascent = characterSize.getAscent ();
    int descent = characterSize.getDescent ();

    Color foregroundColor = null;
    Color backgroundColor = null;

    gc.translate (0.5, 0.5);      // move coordinate grid to use the center of pixels

    // Draw background
    if (isVisible)
    {
      if (hasCursor)
        if (screenContext.reverseVideo)
        {
          backgroundColor = screenContext.backgroundColor;
          foregroundColor = screenContext.foregroundColor;
        }
        else
        {
          backgroundColor = screenContext.foregroundColor;
          foregroundColor = screenContext.backgroundColor;
        }
      else if (screenContext.reverseVideo)
      {
        backgroundColor = screenContext.foregroundColor;
        foregroundColor = screenContext.backgroundColor;
      }
      else
      {
        backgroundColor = screenContext.backgroundColor;
        foregroundColor = screenContext.foregroundColor;
      }
    }
    else if (hasCursor)
      backgroundColor = screenContext.foregroundColor;
    else
      backgroundColor = screenContext.backgroundColor;

    gc.setFill (backgroundColor);

    // without the offset Windows will leave ghosting behind (even though we have
    // translated the screen coordinates)
    if (hasCursor)
      gc.fillRect (x + 0.5, y + 0.5, charWidth, ascent + descent);
    else
      gc.fillRect (x + 0.5, y + 0.5, charWidth, charHeight);

    if (screenContext.highIntensity)
    {
      //      System.out.println ("intensified : " + getChar ());
      if (foregroundColor == Color.WHITESMOKE)
        foregroundColor = Color.WHITE;
    }

    // Draw foreground
    if (isVisible)
      if (isGraphics)
        doGraphics (foregroundColor, backgroundColor, hasCursor, x, y);
      else
      {
        gc.setFill (foregroundColor);
        gc.fillText (getChar () + "", x, y + ascent);       // can we speed this up?

        if (screenContext.underscore)
        {
          gc.setStroke (screenContext.foregroundColor);
          double y2 = y + ascent + descent + 1;
          gc.strokeLine (x + 1, y2, x + charWidth, y2);
        }
      }
    gc.translate (-0.5, -0.5);        // restore coordinate grid
  }

  private void doGraphics (Color foregroundColor, Color backgroundColor,
      boolean hasCursor, int x, int y)
  {
    int width = characterSize.getWidth ();
    int height = characterSize.getHeight ();
    int dx = width / 2;
    int dy = height / 2;

    gc.setStroke (foregroundColor);

    switch (value)
    {
      case HORIZONTAL_LINE:
        gc.strokeLine (x, y + dy, x + width, y + dy);
        break;

      case VERTICAL_LINE:
        gc.strokeLine (x + dx, y, x + dx, y + height);
        break;

      case TOP_LEFT:
        gc.strokeLine (x + dx, y + dy, x + dx, y + height);         // vertical
        gc.strokeLine (x + dx, y + dy, x + width, y + dy);          // horizontal
        break;

      case TOP_RIGHT:
        gc.strokeLine (x + dx, y + dy, x + dx, y + height);         // vertical
        gc.strokeLine (x, y + dy, x + dx, y + dy);                  // horizontal
        break;

      case BOTTOM_LEFT:
        gc.strokeLine (x + dx, y, x + dx, y + dy);                  // vertical
        gc.strokeLine (x + dx, y + dy, x + width, y + dy);          // horizontal
        break;

      case BOTTOM_RIGHT:
        gc.strokeLine (x + dx, y, x + dx, y + dy);                  // vertical
        gc.strokeLine (x, y + dy, x + dx, y + dy);                  // horizontal
        break;

      default:
        gc.fillText (getChar () + "", x, y + characterSize.getAscent ());
        System.out.printf ("Unknown graphics character: %02X%n", value);
    }

    if (hasCursor && (value == VERTICAL_LINE || value == TOP_LEFT || value == TOP_RIGHT))
    {
      gc.setStroke (backgroundColor);
      dy = y + characterSize.getAscent () + characterSize.getDescent () + 1;
      gc.strokeLine (x + dx, dy, x + dx, y + height);
    }
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    if (isStartField ())
      text.append (startFieldAttribute);

    //    for (Attribute attribute : attributes)
    //    {
    //      if (text.length () > 0)
    //        text.append ("\n       ");
    //      text.append (String.format ("%-30s", attribute));
    //    }
    return text.toString ();
  }
}