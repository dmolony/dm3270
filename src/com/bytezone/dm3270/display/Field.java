package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.attributes.StartFieldAttribute;

public class Field
{
  private final Screen screen;
  private final int startPosition;      // position of StartFieldAttribute
  private final int endPosition;        // last data position of this field
  private final StartFieldAttribute startFieldAttribute;

  private final List<ScreenPosition> screenPositions = new ArrayList<> ();

  public Field (Screen screen, int start, int end, List<ScreenPosition> positions)
  {
    this.screen = screen;
    startPosition = start;
    endPosition = end;
    screenPositions.addAll (positions);
    startFieldAttribute = positions.get (0).getStartFieldAttribute ();
  }

  public StartFieldAttribute getStartFieldAttribute ()
  {
    return startFieldAttribute;
  }

  public int getDisplayLength ()
  {
    return screenPositions.size () - 1;
  }

  public ScreenPosition getScreenPosition (int relativePosition)
  {
    return screenPositions.get (relativePosition + 1);
  }

  public boolean isProtected ()
  {
    return startFieldAttribute.isProtected ();
  }

  public boolean isModifiable ()
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
}