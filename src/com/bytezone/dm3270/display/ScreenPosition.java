package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.utilities.Utility;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public final class ScreenPosition
{
  private static final ScreenContext baseContext =
      new ScreenContext (Color.BLACK, Color.BLACK, (byte) 0, false);

  // screen display characters
  private static final String[] charString = new String[256];

  // GraphicsEscape characters
  public static final byte TOP_LEFT = (byte) 0xC5;
  public static final byte TOP_RIGHT = (byte) 0xD5;
  public static final byte BOTTOM_LEFT = (byte) 0xC4;
  public static final byte BOTTOM_RIGHT = (byte) 0xD4;
  public static final byte HORIZONTAL_LINE = (byte) 0xA2;
  public static final byte VERTICAL_LINE = (byte) 0x85;

  private StartFieldAttribute startFieldAttribute;
  private final List<Attribute> attributes = new ArrayList<> ();

  public final int position;
  private byte value;
  private boolean isGraphics;
  private boolean isVisible = true;

  private ScreenContext screenContext = baseContext;

  static
  {
    // build strings to use in the screen-drawing routine
    String space = " ";
    for (int i = 0; i < 33; i++)
      charString[i] = space;
    for (int i = 33; i < 256; i++)
      charString[i] = (char) i + "";
  }

  public ScreenPosition (int position)
  {
    this.position = position;
    reset ();
  }

  StartFieldAttribute getStartFieldAttribute ()
  {
    return startFieldAttribute;
  }

  void setStartField (StartFieldAttribute startFieldAttribute)
  {
    if (startFieldAttribute == null)
    {
      isVisible = true;
      if (this.startFieldAttribute != null)
        attributes.clear ();
    }
    this.startFieldAttribute = startFieldAttribute;
  }

  void addAttribute (Attribute attribute)
  {
    attributes.add (attribute);
  }

  List<Attribute> getAttributes ()
  {
    return attributes;
  }

  // called by Field when deleting a character
  void clearAttributes ()
  {
    attributes.clear ();
  }

  void reset ()
  {
    isVisible = true;
    value = 0;
    isGraphics = false;
    startFieldAttribute = null;
    attributes.clear ();
  }

  // Password fields etc
  // called from PenType1.startField()
  // called from Field constructor
  void setVisible (boolean visible)
  {
    this.isVisible = visible;
  }

  // All the colour and highlight options
  void setScreenContext (ScreenContext screenContext)
  {
    if (screenContext == null)
      throw new IllegalArgumentException ("ScreenContext cannot be null");
    this.screenContext = screenContext;
  }

  ScreenContext getScreenContext ()
  {
    return screenContext;
  }

  boolean isStartField ()
  {
    return startFieldAttribute != null;
  }

  boolean isGraphicsChar ()
  {
    return isGraphics;
  }

  public void setChar (byte value)
  {
    this.value = value;
    isGraphics = false;
  }

  void setGraphicsChar (byte value)
  {
    this.value = value;
    isGraphics = true;
  }

  // used by Field.getText()
  char getChar ()
  {
    if ((value & 0xC0) == 0)
      //    if (isStartField () || (value <= 32 && value >= 0))
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

  String getCharString ()
  {
    if (isStartField ())
      return " ";

    if (isGraphics)
      switch (value)
      {
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

    return charString[Utility.ebc2asc[value & 0xFF]];
  }

  byte getByte ()
  {
    return value;
  }

  boolean isNull ()
  {
    return value == 0;
  }

  int pack (byte[] buffer, int ptr, byte order)
  {
    assert isStartField ();

    buffer[ptr++] = order;

    if (order == Order.START_FIELD)
      buffer[ptr++] = startFieldAttribute.getAttributeValue ();
    else if (order == Order.START_FIELD_EXTENDED)
    {
      buffer[ptr++] = (byte) (attributes.size () + 1);      // includes the SFA
      ptr = startFieldAttribute.pack (buffer, ptr);
      for (Attribute attribute : attributes)
        ptr = attribute.pack (buffer, ptr);
    }
    else
      System.out.println ("I should throw an exception here");

    return ptr;
  }

  int pack (byte[] buffer, int ptr, byte[] replyTypes)
  {
    assert !isStartField ();

    for (Attribute attribute : attributes)
      if (attribute.matches (Attribute.XA_RESET) || attribute.matches (replyTypes))
      {
        buffer[ptr++] = Order.SET_ATTRIBUTE;
        ptr = attribute.pack (buffer, ptr);         // packs type/value pair
      }

    buffer[ptr++] = value;

    return ptr;
  }

  // called by Screen.draw()
  // called by Screen.drawPosition()
  // called by UserScreen.drawScreen()
  void draw (GraphicsContext gc, double x, double y, boolean hasCursor, int charWidth,
      int charHeight, int ascent, int descent)
  {
    // Draw background
    if (isVisible)
      gc.setFill (hasCursor ^ screenContext.reverseVideo ? screenContext.foregroundColor
          : screenContext.backgroundColor);
    else
      gc.setFill (hasCursor ? screenContext.foregroundColor
          : screenContext.backgroundColor);

    gc.fillRect (x, y, charWidth, charHeight);

    Color foreground = hasCursor ^ screenContext.reverseVideo
        ? screenContext.backgroundColor : screenContext.foregroundColor;

    // Draw foreground
    if (isVisible)
      if (isGraphics)
      {
        gc.setStroke (foreground);
        doGraphics (gc, hasCursor, x, y, charWidth, charHeight, ascent, descent);
      }
      else
      {
        gc.setFill (foreground);
        gc.fillText (getCharString (), x, y + ascent);

        if (screenContext.underscore)
        {
          gc.setStroke (foreground);
          x += 0.5;
          y += 0.5;
          double y2 = y + ascent + descent;
          gc.strokeLine (x, y2, x + charWidth, y2);
        }
      }
  }

  private void doGraphics (GraphicsContext gc, boolean hasCursor, double x, double y,
      int width, int height, int ascent, int descent)
  {
    x += 0.5;
    y += 0.5;
    int dx = width / 2;
    int dy = height / 2;

    //    gc.setStroke (foregroundColor);

    switch (value)
    {
      case HORIZONTAL_LINE:
        gc.strokeLine (x, y + dy, x + width, y + dy);
        break;

      case VERTICAL_LINE:
        gc.strokeLine (x + dx, y, x + dx, y + height);
        break;

      case TOP_LEFT:
        gc.strokeLine (x + dx, y + dy, x + dx, y + height);   // vertical
        gc.strokeLine (x + dx, y + dy, x + width, y + dy);    // horizontal
        break;

      case TOP_RIGHT:
        gc.strokeLine (x + dx, y + dy, x + dx, y + height);   // vertical
        gc.strokeLine (x, y + dy, x + dx, y + dy);            // horizontal
        break;

      case BOTTOM_LEFT:
        gc.strokeLine (x + dx, y, x + dx, y + dy);            // vertical
        gc.strokeLine (x + dx, y + dy, x + width, y + dy);    // horizontal
        break;

      case BOTTOM_RIGHT:
        gc.strokeLine (x + dx, y, x + dx, y + dy);            // vertical
        gc.strokeLine (x, y + dy, x + dx, y + dy);            // horizontal
        break;

      default:
        //        gc.setFill (foregroundColor);
        gc.fillText (".", x, y + ascent);
    }

    //    if (hasCursor && (value == VERTICAL_LINE || value == TOP_LEFT || value == TOP_RIGHT))
    //    {
    //      // extend the vertical line through the leading
    //      gc.setStroke (backgroundColor);
    //      dy = y + ascent + descent + 1;
    //      gc.strokeLine (x + dx, dy, x + dx, y + height);
    //    }
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    if (isStartField ())
      text.append (".." + startFieldAttribute);
    else
      for (Attribute attribute : attributes)
        text.append ("--" + attribute);

    text.append (", byte: " + getCharString ());

    return text.toString ();
  }
}