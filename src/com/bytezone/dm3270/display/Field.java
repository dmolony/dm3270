package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.attributes.StartFieldAttribute;

public class Field
{
  private final Screen screen;
  private final int startPosition;      // position of StartFieldAttribute
  private final int endPosition;        // last data position of this field

  private final List<ScreenPosition> screenPositions = new ArrayList<> ();

  public Field (Screen screen, int start, int end, List<ScreenPosition> positions)
  {
    this.screen = screen;
    startPosition = start;
    endPosition = end;
    screenPositions.addAll (positions);
  }

  public StartFieldAttribute getStartFieldAttribute ()
  {
    return screen.getScreenPosition (startPosition).getStartFieldAttribute ();
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
    StartFieldAttribute sfa = getStartFieldAttribute ();
    return sfa == null ? true : getStartFieldAttribute ().isProtected ();
  }

  public boolean isModifiable ()
  {
    return !isProtected ();
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