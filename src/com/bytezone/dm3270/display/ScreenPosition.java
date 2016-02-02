package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.utilities.Dm3270Utility;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public final class ScreenPosition
{
  // screen display characters
  private static final String[] charString = new String[256];

  // GraphicsEscape characters
  public static final byte TOP_LEFT = (byte) 0xC5;
  public static final byte TOP_RIGHT = (byte) 0xD5;
  public static final byte BOTTOM_LEFT = (byte) 0xC4;
  public static final byte BOTTOM_RIGHT = (byte) 0xD4;
  public static final byte HORIZONTAL_LINE = (byte) 0xA2;
  public static final byte VERTICAL_LINE = (byte) 0x85;

  private final GraphicsContext gc;
  private ScreenDimensions screenDimensions;
  private final int position;

  private StartFieldAttribute startFieldAttribute;
  private final List<Attribute> attributes = new ArrayList<> ();

  private byte value;
  private boolean isGraphics;
  private boolean isVisible = true;
  private ScreenContext screenContext;

  static
  {
    // build strings to use in the screen-drawing routine
    String space = " ";
    for (int i = 0; i < 33; i++)
      charString[i] = space;
    for (int i = 33; i < 256; i++)
      charString[i] = (char) i + "";
  }

  public ScreenPosition (int position, GraphicsContext gc,
      ScreenDimensions screenDimensions, ScreenContext screenContext)
  {
    this.position = position;
    this.screenDimensions = screenDimensions;
    this.gc = gc;

    this.screenContext = screenContext;
    reset ();
  }

  // called from this()
  // called from Pen.clearScreen()
  // called from Pen.startField()
  // called from Pen.write()
  // called from Pen.writeGraphics()
  void reset ()
  {
    isVisible = true;
    value = 0;
    isGraphics = false;
    startFieldAttribute = null;
    attributes.clear ();
  }

  // called from Pen.write()
  // called from Pen.eraseEOF()
  // called from Cursor.typeChar()
  // called from Field.erase()
  // called from Field.clearData()
  // called from Field.pull()
  // called from Field.push()
  // called from Field.setText()
  void setChar (byte value)
  {
    this.value = value;
    isGraphics = false;
  }

  // called from Pen.writeGraphics()
  void setGraphicsChar (byte value)
  {
    this.value = value;
    isGraphics = true;
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

  int getPosition ()
  {
    return position;
  }

  void setScreenDimensions (ScreenDimensions screenDimensions)
  {
    this.screenDimensions = screenDimensions;
  }

  ScreenDimensions getScreenDimensions ()
  {
    return screenDimensions;
  }

  // called by Field when deleting a character
  void clearAttributes ()
  {
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

  // used by Field.getText()
  char getChar ()
  {
    if ((value & 0xC0) == 0)
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

    return (char) Dm3270Utility.ebc2asc[value & 0xFF];
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

    return charString[Dm3270Utility.ebc2asc[value & 0xFF]];
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
  void draw (boolean hasCursor)
  {
    FontDetails fontDetails = screenContext.fontDetails;

    double x = 4 + position % screenDimensions.columns * fontDetails.width;
    double y = 4 + position / screenDimensions.columns * fontDetails.height;

    // Draw background
    if (isVisible)
      gc.setFill (hasCursor ^ screenContext.reverseVideo ? screenContext.foregroundColor
          : screenContext.backgroundColor);
    else
      gc.setFill (hasCursor ? screenContext.foregroundColor
          : screenContext.backgroundColor);

    gc.fillRect (x, y, fontDetails.width, fontDetails.height);

    Color foreground = hasCursor ^ screenContext.reverseVideo
        ? screenContext.backgroundColor : screenContext.foregroundColor;

    // Draw foreground
    if (isVisible)
      if (isGraphics)
      {
        gc.setStroke (foreground);
        doGraphics (x, y);
      }
      else
      {
        gc.setFill (foreground);
        gc.fillText (getCharString (), x, y + fontDetails.ascent);

        if (screenContext.underscore)
        {
          gc.setStroke (foreground);
          x += 0.5;     // stroke commands need to be offset for Windows
          y += 0.5;
          double y2 = y + fontDetails.height - 1;
          gc.strokeLine (x, y2, x + fontDetails.width, y2);
        }
      }
  }

  private void doGraphics (double x, double y)
  {
    x += 0.5;     // stroke commands need to be offset for Windows
    y += 0.5;

    FontDetails fontDetails = screenContext.fontDetails;

    int dx = fontDetails.width / 2;
    int dy = fontDetails.height / 2;

    switch (value)
    {
      case HORIZONTAL_LINE:
        gc.strokeLine (x, y + dy, x + fontDetails.width, y + dy);
        break;

      case VERTICAL_LINE:
        gc.strokeLine (x + dx, y, x + dx, y + fontDetails.height);
        break;

      case TOP_LEFT:
        gc.strokeLine (x + dx, y + dy, x + dx, y + fontDetails.height);   // vertical
        gc.strokeLine (x + dx, y + dy, x + fontDetails.width, y + dy);    // horizontal
        break;

      case TOP_RIGHT:
        gc.strokeLine (x + dx, y + dy, x + dx, y + fontDetails.height);   // vertical
        gc.strokeLine (x, y + dy, x + dx, y + dy);                        // horizontal
        break;

      case BOTTOM_LEFT:
        gc.strokeLine (x + dx, y, x + dx, y + dy);                        // vertical
        gc.strokeLine (x + dx, y + dy, x + fontDetails.width, y + dy);    // horizontal
        break;

      case BOTTOM_RIGHT:
        gc.strokeLine (x + dx, y, x + dx, y + dy);                        // vertical
        gc.strokeLine (x, y + dy, x + dx, y + dy);                        // horizontal
        break;

      default:
        gc.fillText (".", x, y + fontDetails.ascent);
    }
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