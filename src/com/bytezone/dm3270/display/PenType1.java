package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;

import javafx.scene.paint.Color;

public class PenType1 implements Pen
{
  private final ScreenPosition[] screenPositions;
  private final ContextManager contextManager;

  private ScreenContext currentContext;
  private ScreenContext overrideContext;

  private int currentPosition;
  private int startFieldPosition;
  private int totalFields;          // used to indicate at least one SFA exists

  private final List<Attribute> pendingAttributes = new ArrayList<> ();

  // created by Screen and UserScreen
  PenType1 (ScreenPosition[] screenPositions)
  {
    this.screenPositions = screenPositions;
    contextManager = new ContextManager ();

    ScreenContext baseContext = contextManager.getDefaultScreenContect ();
    for (int i = 0; i < screenPositions.length; i++)
      screenPositions[i] = new ScreenPosition (i, baseContext);
  }

  @Override
  public ScreenContext getDefaultScreenContext ()
  {
    return contextManager.getDefaultScreenContect ();
  }

  // called from Screen.clearScreen()
  // called from UserScreen.clearScreen()
  @Override
  public void reset ()
  {
    totalFields = 0;
    //    currentPosition = 0;
  }

  // called from StartFieldAttribute.process()
  @Override
  public void startField (StartFieldAttribute startFieldAttribute)
  {
    currentContext = contextManager.getDefaultScreenContect ();
    totalFields++;

    ScreenPosition screenPosition = screenPositions[currentPosition];

    screenPosition.reset ();
    screenPosition.setStartField (startFieldAttribute);
    screenPosition.setVisible (false);

    startFieldPosition = currentPosition;
    reset ((byte) 0);
    storeCurrentContext ();

    // sometimes a reset attribute is overwritten by a new SFA
    if (pendingAttributes.size () > 0)
    {
      if (false)
      {
        System.out.printf ("Unapplied attributes at %d%n", currentPosition);
        for (Attribute attribute : pendingAttributes)
          System.out.println ("  " + attribute);
      }
      pendingAttributes.clear ();
    }
  }

  // called from SetAttributeOrder.process()
  // called from StartFieldExtendedOrder.process()
  @Override
  public void addAttribute (Attribute attribute)
  {
    pendingAttributes.add (attribute);
    if (false)
      System.out.printf ("Pending attribute at %4d : %s%n", currentPosition, attribute);
  }

  // called from InsertCursorOrder.process()
  // called from RepeatToAddressOrder.process()
  // called from StartFieldOrder.process()
  // called from StartFieldExtendedOrder.process()
  @Override
  public int getPosition ()
  {
    return currentPosition;
  }

  // called from ForegroundColor.process()
  // called from StartFieldAttribute.process()
  @Override
  public void setForeground (Color color)
  {
    if (currentPosition == startFieldPosition)
      currentContext = contextManager.setForeground (currentContext, color);
    else
      overrideContext = contextManager.setForeground (currentContext, color);
    storeCurrentContext ();
  }

  // called from BackgroundColor.process()
  // called from StartFieldAttribute.process()
  @Override
  public void setBackground (Color color)
  {
    if (currentPosition == startFieldPosition)
      currentContext = contextManager.setBackground (currentContext, color);
    else
      overrideContext = contextManager.setBackground (currentContext, color);
    storeCurrentContext ();
  }

  // called from ExtendedHighlight.process()
  // called from StartFieldAttribute.process()
  @Override
  public void setHighlight (byte value)
  {
    if (currentPosition == startFieldPosition)
      currentContext = contextManager.setHighlight (currentContext, value);
    else
      overrideContext = contextManager.setHighlight (currentContext, value);
    storeCurrentContext ();
  }

  // called from StartFieldAttribute.process()
  @Override
  public void setHighIntensity (boolean value)
  {
    if (currentPosition == startFieldPosition)
      currentContext = contextManager.setHighIntensity (currentContext, value);
    else
      overrideContext = contextManager.setHighIntensity (currentContext, value);
    storeCurrentContext ();
  }

  // called from ResetAttribute.process()
  // called from startField()
  @Override
  public void reset (byte value)
  {
    overrideContext = null;
  }

  private void storeCurrentContext ()
  {
    ScreenPosition screenPosition = screenPositions[currentPosition];
    storeContext (screenPosition);
  }

  private void storeContext (ScreenPosition screenPosition)
  {
    if (overrideContext != null)
      screenPosition.setScreenContext (overrideContext);
    else
      screenPosition.setScreenContext (currentContext);
  }

  // called from GraphicsEscapeOrder.process()
  @Override
  public void writeGraphics (byte b)
  {
    ScreenPosition screenPosition = screenPositions[currentPosition];
    screenPosition.reset ();
    storeContext (screenPosition);
    screenPosition.setGraphicsChar (b);
    moveRight ();
  }

  // called from FormatControlOrder.process()
  // called from RepeatToAddressOrder.process()
  // called from TextOrder.process()
  @Override
  public void write (byte b)
  {
    ScreenPosition screenPosition = screenPositions[currentPosition];
    screenPosition.reset ();
    storeContext (screenPosition);
    screenPosition.setChar (b);
    moveRight ();
  }

  private void applyAttributes (ScreenPosition screenPosition)
  {
    for (Attribute attribute : pendingAttributes)
      screenPosition.addAttribute (attribute);
    pendingAttributes.clear ();
  }

  // called from StartFieldOrder.process()
  // called from StartFieldExtendedOrder.process()
  // called from write()
  // called from writeGraphics()
  // called from eraseEOF()
  @Override
  public void moveRight ()
  {
    if (pendingAttributes.size () > 0)
      applyAttributes (screenPositions[currentPosition]);

    currentPosition = validate (currentPosition + 1);
  }

  // called from ProgramTabOrder.process()
  @Override
  public void eraseEOF ()
  {
    if (totalFields == 0)
    {
      System.out.println ("No fields to erase");
      return;
    }

    while (true)
    {
      ScreenPosition screenPosition = screenPositions[currentPosition];
      if (screenPosition.isStartField ())
        break;
      screenPosition.setChar ((byte) 0);
      screenPosition.clearAttributes ();
      moveRight ();
    }
  }

  // called from ProgramTabOrder.process()
  @Override
  public void tab ()
  {
    ScreenPosition screenPosition = screenPositions[currentPosition];
    if (screenPosition.isStartField ()
        && !screenPosition.getStartFieldAttribute ().isProtected ())
    {
      currentPosition = validate (currentPosition + 1);
      return;
    }
    int next = currentPosition;
    while (true)
    {
      next = findNextStartPosition (next);
      if (next < currentPosition)         // wrapped around or not found
      {
        currentPosition = 0;
        break;
      }
      screenPosition = screenPositions[next];
      if (!screenPosition.getStartFieldAttribute ().isProtected ())
      {
        currentPosition = validate (next + 1);
        break;
      }
    }
  }

  // called from SetBufferAddressOrder.process()
  @Override
  public void moveTo (int position)
  {
    if (pendingAttributes.size () > 0)
    {
      if (false)
      {
        System.out.printf ("Unapplied attributes at %d%n", currentPosition);
        for (Attribute attribute : pendingAttributes)
          System.out.println (attribute);
      }
      pendingAttributes.clear ();
    }
    currentPosition = validate (position);

    if (totalFields > 0)
    {
      int pos = findPreviousStartPosition (currentPosition);
      if (pos >= 0)
      {
        startFieldPosition = pos;
        currentContext = screenPositions[pos].getScreenContext ();
      }
    }
  }

  private int findPreviousStartPosition (int position)
  {
    int pos = position;
    while (true)
    {
      pos = validate (pos - 1);
      ScreenPosition screenPosition = screenPositions[pos];

      if (screenPosition.isStartField ())
        return pos;

      if (pos == position)
        break;
    }

    System.out.printf ("No previous start field found: %d%n", totalFields);
    return -1;
  }

  private int findNextStartPosition (int position)
  {
    int pos = position;
    while (true)
    {
      pos = validate (pos + 1);
      ScreenPosition screenPosition = screenPositions[pos];

      if (screenPosition.isStartField ())
        return pos;

      if (pos == position)
        break;
    }

    System.out.printf ("No next start field found: %d%n", totalFields);
    return -1;
  }

  @Override
  public int validate (int position)
  {
    while (position < 0)
      position += screenPositions.length;

    while (position >= screenPositions.length)
      position -= screenPositions.length;

    return position;
  }

  @Override
  public String getScreenText (int columns)
  {
    StringBuilder text = new StringBuilder ();

    int pos = 0;
    for (ScreenPosition sp : screenPositions)
    {
      if (sp.isStartField ())
        text.append ("%");
      else
        text.append (sp.getCharString ());
      if (++pos % columns == 0)
        text.append ("\n");
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  // Debugging
  // ---------------------------------------------------------------------------------//

  private void dumpScreenPositions ()
  {
    dumpScreenPositions (0, screenPositions.length);
  }

  private void dumpScreenPositions (int from, int to)
  {
    while (from < to)
      System.out.println (screenPositions[from++]);
  }

  @Override
  public Iterator<ScreenPosition> iterator ()
  {
    return new Iterator<ScreenPosition> ()
    {
      private int pos = 0;

      @Override
      public boolean hasNext ()
      {
        return screenPositions.length > pos;
      }

      @Override
      public ScreenPosition next ()
      {
        return screenPositions[pos++];
      }

      @Override
      public void remove ()
      {
        throw new UnsupportedOperationException ("Cannot remove an element of an array.");
      }
    };
  }
}
