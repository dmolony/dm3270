package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.Attribute.AttributeType;
import com.bytezone.dm3270.attributes.StartFieldAttribute;

public class Field
{
  private final Screen screen;
  private final int startPosition;      // position of StartFieldAttribute
  private final int endPosition;        // last data position of this field
  private final StartFieldAttribute startFieldAttribute;

  private final List<ScreenPosition2> screenPositions = new ArrayList<> ();

  private Field next, previous;

  public Field (Screen screen, int start, int end, List<ScreenPosition2> positions)
  {
    assert positions.size () == (start > end ? screen.screenSize - start + end + 1 : end
        - start + 1);

    this.screen = screen;
    startPosition = start;
    endPosition = end;
    screenPositions.addAll (positions);
    startFieldAttribute = positions.get (0).getStartFieldAttribute ();

    if (startFieldAttribute.isHidden ())
      for (ScreenPosition2 screenPosition : positions)
        screenPosition.setVisible (false);
  }

  public StartFieldAttribute getStartFieldAttribute ()
  {
    return startFieldAttribute;
  }

  void setScreenContexts ()
  {
    ContextManager contextHandler = screen.getContextHandler ();
    ScreenPosition2 sp = screenPositions.get (0);
    StartFieldAttribute sfa = sp.getStartFieldAttribute ();
    ScreenContext screenContext = contextHandler.getBase ();

    screenContext = sfa.process (contextHandler, screenContext);

    for (ScreenPosition2 sp2 : screenPositions)
    {
      for (Attribute attribute : sp2.getAttributes ())
      {
        if (attribute.getAttributeType () == AttributeType.RESET)
        {
          screenContext = contextHandler.getBase ();
          screenContext = sfa.process (contextHandler, screenContext);
        }
        else
          screenContext = attribute.process (contextHandler, screenContext);
      }
      sp2.setScreenContext (screenContext);
    }
  }

  // link two unprotected fields to each other
  void linkToNext (Field nextField)
  {
    assert !isProtected ();
    this.next = nextField;
    nextField.previous = this;
  }

  void setPrevious (Field field)
  {
    this.previous = field;
  }

  void setNext (Field field)
  {
    this.next = field;
  }

  public Field getNextUnprotectedField ()
  {
    return next;
  }

  public Field getPreviousUnprotectedField ()
  {
    return previous;
  }

  public int getDisplayLength ()
  {
    return screenPositions.size () - 1;
  }

  public int getFirstLocation ()
  {
    return screen.validate (startPosition + 1);
  }

  public boolean isHidden ()
  {
    return startFieldAttribute.isHidden ();
  }

  public boolean isProtected ()
  {
    return startFieldAttribute.isProtected ();
  }

  public boolean isUnprotected ()
  {
    return !startFieldAttribute.isProtected ();
  }

  public boolean isModified ()
  {
    return startFieldAttribute.isModified ();
  }

  public void setModified (boolean modified)
  {
    startFieldAttribute.setModified (modified);
    //    screen.fieldModified (this);     // display the new status
  }

  public boolean contains (int position)
  {
    if (startPosition <= endPosition)
      return position >= startPosition && position <= endPosition;
    return position >= startPosition || position <= endPosition;
  }

  public void draw ()
  {
    int position = startPosition;
    while (true)
    {
      screen.drawPosition (position, false);
      if (position == endPosition)
        break;
      position = screen.validate (position + 1);
    }
  }

  public void clear (boolean setModified)
  {
    if (setModified)                 // don't reset any already set flags
      setModified (true);

    for (int i = 1; i < screenPositions.size (); i++)
      screenPositions.get (i).reset ();
  }

  public String getText ()
  {
    if (startPosition == endPosition)
      return "[]";

    char[] buffer = new char[getDisplayLength ()];
    int position = screen.validate (startPosition + 1);
    int ptr = 0;

    while (true)
    {
      buffer[ptr++] = screen.getScreenPosition (position).getChar ();
      if (position == endPosition)
        break;
      position = screen.validate (position + 1);
    }
    return "[" + new String (buffer) + "]";
  }

  public void setText (byte[] buffer)
  {
    assert startPosition != endPosition;
    int position = screen.validate (startPosition + 1);
    int ptr = 0;

    while (true)
    {
      screen.getScreenPosition (position).setChar (buffer[ptr++]);
      if (position == endPosition || ptr == buffer.length)
        break;
      position = screen.validate (position + 1);
    }
  }

  public String toStringWithLinks ()
  {
    StringBuilder text = new StringBuilder (toString ());
    if (previous != null)
    {
      text.append ("\n          <-- ");
      text.append (previous);
    }
    if (next != null)
    {
      text.append ("\n          --> ");
      text.append (next);
    }
    return text.toString ();
  }

  @Override
  public String toString ()
  {
    return String.format ("%04d-%04d %s %s", startPosition, endPosition,
                          startFieldAttribute.getAcronym (), getText ());
  }
}