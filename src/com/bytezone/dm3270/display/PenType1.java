package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;

import javafx.scene.canvas.GraphicsContext;

class PenType1 implements Pen
{
  private final ScreenPosition[] screenPositions;   // owned by Screen

  private int currentPosition;
  private boolean formattedScreen;
  private ScreenDimensions screenDimensions;

  private final List<Attribute> pendingAttributes = new ArrayList<> ();

  // created by Screen and HistoryScreen
  PenType1 (ScreenPosition[] screenPositions, GraphicsContext gc,
      ContextManager contextManager, ScreenDimensions screenDimensions)
  {
    this.screenPositions = screenPositions;
    this.screenDimensions = screenDimensions;
    //    columns = screenDimensions.columns;

    ScreenContext defaultContext = contextManager.getDefaultScreenContext ();

    for (int i = 0; i < screenPositions.length; i++)
      screenPositions[i] = new ScreenPosition (i, gc, screenDimensions, defaultContext);
  }

  // called from Screen.clearScreen()
  // called from UserScreen.clearScreen()
  @Override
  public void clearScreen ()
  {
    for (ScreenPosition screenPosition : screenPositions)
      screenPosition.reset ();

    formattedScreen = false;
  }

  // called from StartFieldAttribute.process()
  @Override
  public void startField (StartFieldAttribute startFieldAttribute)
  {
    formattedScreen = true;

    ScreenPosition screenPosition = screenPositions[currentPosition];

    screenPosition.reset ();
    screenPosition.setStartField (startFieldAttribute);
    screenPosition.setVisible (false);

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

  // called from GraphicsEscapeOrder.process()
  @Override
  public void writeGraphics (byte b)
  {
    ScreenPosition screenPosition = screenPositions[currentPosition];
    screenPosition.reset ();
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
    if (!formattedScreen)
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
      if (true)
      {
        System.out.printf ("Unapplied attributes at %d%n", currentPosition);
        for (Attribute attribute : pendingAttributes)
          System.out.println (attribute);
      }
      applyAttributes (screenPositions[currentPosition]);
    }
    currentPosition = validate (position);
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

    System.out.printf ("No next start field found: %d%n", position);
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
  public void setScreenDimensions (ScreenDimensions screenDimensions)
  {
    this.screenDimensions = screenDimensions;
    for (int i = 0; i < screenDimensions.size; i++)
      screenPositions[i].setScreenDimensions (screenDimensions);
  }

  @Override
  public String getScreenText ()
  {
    StringBuilder text = new StringBuilder ();

    int pos = 0;
    for (ScreenPosition sp : screenPositions)
    {
      if (sp.isStartField ())
        text.append ("%");
      else
        text.append (sp.getCharString ());
      if (++pos % screenDimensions.columns == 0)
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

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("[Pos:%d, columns:%d, formatted:%s]", currentPosition,
                                screenDimensions.columns, formattedScreen));

    return text.toString ();
  }
}
