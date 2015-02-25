package com.bytezone.dm3270.application;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.Attribute.AttributeType;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.orders.Order;

public class ScreenPosition
{
  private final ScreenHandler screenHandler;
  private final ContextHandler contextHandler;

  private final int location;        // change to BufferAddress?
  private final int row;
  private final int column;

  private ScreenContext screenContext;
  private byte character;

  private boolean isGraphicsCharacter;
  private boolean isStartField;
  private boolean isVisible;

  private final List<Attribute> fieldAttributes = new ArrayList<> ();

  public ScreenPosition (ScreenHandler screenHandler, int location)
  {
    this.screenHandler = screenHandler;
    this.location = location;

    contextHandler = screenHandler.getContextHandler ();
    screenContext = contextHandler.getBase ();

    int columns = screenHandler.getColumns ();
    row = location / columns;
    column = location % columns;
  }

  public void addAttribute (Attribute attribute)
  {
    fieldAttributes.add (attribute);
    if (attribute.getAttributeType () == AttributeType.START_FIELD)
      isStartField = true;
  }

  public StartFieldAttribute getStartFieldAttribute ()
  {
    for (Attribute attribute : fieldAttributes)
      if (attribute instanceof StartFieldAttribute)
        return (StartFieldAttribute) attribute;

    return null;
  }

  public void applyAttributes ()
  {
    // apply all attributes to the ScreenContext at this ScreenPosition
    for (Attribute attribute : fieldAttributes)
      attribute.process (this);
  }

  public void reset ()
  {
    isStartField = false;
    isVisible = false;
    character = 0;
    isGraphicsCharacter = false;
    screenContext = contextHandler.getBase ();
  }

  public boolean isStartField ()
  {
    return isStartField;
  }

  public boolean isHidden ()
  {
    return isStartField && getStartFieldAttribute ().isHidden ();
  }

  public boolean isHighIntensity ()
  {
    return screenContext.highIntensity;
  }

  public boolean isVisible ()
  {
    return isVisible && !isStartField;
  }

  public boolean isProtected ()
  {
    return isStartField && getStartFieldAttribute ().isProtected ();
  }

  public void resetCharacter ()
  {
    character = 0;
    isGraphicsCharacter = false;
  }

  public void setCharacter (byte b)
  {
    character = b;
    isGraphicsCharacter = false;
  }

  public void setGraphicsCharacter (byte b)
  {
    character = b;
    isGraphicsCharacter = true;
  }

  public void setForeground (Color color)
  {
    screenContext = contextHandler.setForeground (screenContext, color);
  }

  public void setBackground (Color color)
  {
    screenContext = contextHandler.setBackground (screenContext, color);
  }

  public void setReverseVideo (boolean value)
  {
    screenContext = contextHandler.setReverseVideo (screenContext, value);
  }

  public void setUnderscore (boolean value)
  {
    screenContext = contextHandler.setUnderscore (screenContext, value);
  }

  public void setHighIntensity (boolean value)
  {
    screenContext = contextHandler.setHighIntensity (screenContext, value);
  }

  public void resetAttribute ()
  {
    screenContext = contextHandler.resetAttribute (screenContext);
  }

  public boolean hasCursor ()
  {
    Cursor cursor = screenHandler.getCursor ();
    return (cursor.isVisible () && cursor.getLocation () == location);
  }

  public void setVisible (boolean isVisible)
  {
    this.isVisible = isVisible;
  }

  public void clearAttributes ()
  {
    fieldAttributes.clear ();
  }

  public byte getByte ()
  {
    return character;
  }

  public char getChar ()
  {
    if (isStartField || character == 0)
      return ' ';

    return (char) Utility.ebc2asc[(character & 0xFF)];
  }

  public boolean isGraphicsCharacter ()
  {
    return isGraphicsCharacter;
  }

  public int getLocation ()
  {
    return location;
  }

  public int getRow ()
  {
    return row;
  }

  public int getColumn ()
  {
    return column;
  }

  public void setScreenContext (ScreenContext screenContext)
  {
    this.screenContext = screenContext;
  }

  public ScreenContext getScreenContext ()
  {
    return screenContext;
  }

  //  public boolean isEmpty ()
  //  {
  //    return (getChar () == ' ' && fieldAttributes.size () == 0);
  //  }

  public boolean hasAttributes ()
  {
    return fieldAttributes.size () > 0;
  }

  public boolean hasData ()
  {
    return character != 0;
  }

  public List<Attribute> getAttributes ()
  {
    return fieldAttributes;
  }

  public int packAttributes (byte[] buffer, int ptr)
  {
    for (Attribute attribute : fieldAttributes)
      if (attribute.matches (Attribute.XA_RESET)
          || attribute.matches (screenHandler.getReplyTypes ()))
      {
        buffer[ptr++] = Order.SET_ATTRIBUTE;
        ptr = attribute.pack (buffer, ptr);
      }

    return ptr;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("%03d/%03d %d %s", row, column, fieldAttributes.size (),
                                getChar ()));

    for (Attribute attr : fieldAttributes)
    {
      text.append ("\n        ");
      text.append (attr);
    }

    return text.toString ();
  }
}