package com.bytezone.dm3270.application;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.orders.BufferAddress;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.structuredfields.SetReplyMode;

public class ScreenField
{
  private final ScreenHandler screenHandler;
  private final int startPosition;      // position of StartFieldAttribute
  private int endPosition;              // last data position of this field

  private boolean isHidden;
  private boolean isHighIntensity;

  private final List<ScreenPosition> screenPositions = new ArrayList<> ();
  private ScreenField previous, next;

  public ScreenField (ScreenHandler screenHandler, int startPosition)
  {
    this.screenHandler = screenHandler;
    this.startPosition = startPosition;
  }

  public void add (ScreenPosition screenPosition)
  {
    screenPositions.add (screenPosition);

    if (screenPositions.size () == 1)       // must be the Start Field position
    {
      StartFieldAttribute sfa = getStartFieldAttribute ();
      assert sfa != null;
      this.isHidden = sfa.isHidden ();
      this.isHighIntensity = sfa.isIntensified ();
      screenPosition.setVisible (false);
    }
    else
    {
      screenPosition.setVisible (!this.isHidden);
      screenPosition.setHighIntensity (this.isHighIntensity);
    }
  }

  public void setEndPosition (int endPosition)
  {
    this.endPosition = endPosition;
  }

  List<ScreenPosition> getScreenPositions ()
  {
    return screenPositions;
  }

  public StartFieldAttribute getStartFieldAttribute ()
  {
    return screenPositions.get (0).getStartFieldAttribute ();
  }

  public List<Attribute> getAttributes ()
  {
    return screenPositions.get (0).getAttributes ();
  }

  public ScreenField getPrevious ()
  {
    return previous;
  }

  public ScreenField getNext ()
  {
    return next;
  }

  public int getLength ()
  {
    if (startPosition <= endPosition)
      return endPosition - startPosition;
    else
      return screenHandler.getScreenSize () - startPosition + endPosition;
  }

  public boolean contains (int position)
  {
    if (startPosition <= endPosition)
      return position > startPosition && position <= endPosition;
    else
      return position > startPosition || position < endPosition;
  }

  public boolean contains (BufferAddress bufferAddress)
  {
    return contains (bufferAddress.getLocation ());
  }

  public int getStartPosition ()
  {
    return startPosition;
  }

  public boolean isProtected ()
  {
    StartFieldAttribute sfa = getStartFieldAttribute ();
    return sfa == null ? true : getStartFieldAttribute ().isProtected ();
  }

  public boolean isModifiable ()
  {
    return !isProtected ();
  }

  public void linkToNext (ScreenField nextField)
  {
    this.next = nextField;
    nextField.previous = this;
  }

  public boolean isHidden ()
  {
    return isHidden;
  }

  public boolean matchesLocation (int location)
  {
    return startPosition == location;
  }

  public void clear ()        // NB this will assigns the attribute byte as well
  {
    setModified (true);
    for (ScreenPosition sp : screenPositions)
      sp.setCharacter ((byte) 0);
  }

  public boolean isModified ()
  {
    return getStartFieldAttribute ().isModified ();
  }

  public void setModified (boolean modified)
  {
    StartFieldAttribute sfa = getStartFieldAttribute ();
    if (sfa != null)
      sfa.setModified (modified);
    screenHandler.fieldModified (this);
  }

  public String getFieldType ()
  {
    StartFieldAttribute sfa = getStartFieldAttribute ();
    return sfa == null ? "" : sfa.getAcronym ();
  }

  public String getString ()
  {
    try
    {
      return new String (getData (), "CP1047");
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace ();
    }
    return "";
  }

  public byte[] getData ()
  {
    byte[] data = new byte[screenPositions.size () - 1];    // skip attribute byte
    int ptr = 0;
    for (int i = 1; i < screenPositions.size (); i++)
      data[ptr++] = screenPositions.get (i).getByte ();
    return data;
  }

  public int pack (byte[] buffer, int ptr)
  {
    byte replyMode = screenHandler.getReplyMode ();

    if (replyMode == SetReplyMode.RM_FIELD)
    {
      buffer[ptr++] = Order.START_FIELD;

      StartFieldAttribute sfa = getStartFieldAttribute ();
      buffer[ptr++] = sfa.getValue ();
    }
    else
    {
      buffer[ptr++] = Order.START_FIELD_EXTENDED;

      List<Attribute> attributes = getAttributes ();
      buffer[ptr++] = (byte) attributes.size ();

      for (Attribute attribute : attributes)
        ptr = attribute.pack (buffer, ptr);
    }

    for (int i = 1; i < screenPositions.size (); i++)
    {
      ScreenPosition screenPosition = screenPositions.get (i);

      if (replyMode == SetReplyMode.RM_CHARACTER)
        ptr = screenPosition.packAttributes (buffer, ptr);

      if (screenPosition.isGraphicsCharacter ())
        buffer[ptr++] = Order.GRAPHICS_ESCAPE;

      buffer[ptr++] = screenPosition.getByte ();
    }

    return ptr;
  }

  @Override
  public String toString ()
  {
    return String.format ("%04d-%04d %4d  [%s]", startPosition, endPosition,
                          getLength (), getString ());
  }
}