package com.bytezone.dm3270.display;

import javafx.scene.paint.Color;

import com.bytezone.dm3270.attributes.StartFieldAttribute;

// should the pen be a stack? each context change is pushed, each reset is popped.
public class Pen
{
  private final Screen screen;
  private final ContextManager contextManager;

  private ScreenContext currentContext;
  private ScreenContext overrideContext;
  private int currentPosition;
  private int startFieldPosition;

  public Pen (Screen screen)
  {
    this.screen = screen;
    contextManager = new ContextManager ();
  }

  public ScreenContext getBase ()
  {
    return contextManager.getBase ();
  }

  public void startField (StartFieldAttribute startFieldAttribute)
  {
    currentContext = contextManager.getBase ();

    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    screenPosition.reset ();
    screenPosition.setStartField (startFieldAttribute);
    screenPosition.setVisible (false);

    startFieldPosition = currentPosition;
    reset ((byte) 0);
    storeCurrentContext ();
  }

  public int getPosition ()
  {
    return currentPosition;
  }

  public void setForeground (Color color)
  {
    if (currentPosition == startFieldPosition)
      currentContext = contextManager.setForeground (currentContext, color);
    else
      overrideContext = contextManager.setForeground (currentContext, color);
    storeCurrentContext ();
  }

  public void setBackground (Color color)
  {
    currentContext = contextManager.setBackground (currentContext, color);
    storeCurrentContext ();
  }

  public void setHighlight (byte value)
  {
    //    System.out.printf ("%d %d%n", startFieldPosition, currentPosition);
    if (currentPosition == startFieldPosition)
    {
      currentContext = contextManager.setHighlight (currentContext, value);
      //      System.out.println (currentContext);
    }
    else
    {
      overrideContext = contextManager.setHighlight (currentContext, value);
      //      System.out.println (overrideContext);
    }
    storeCurrentContext ();
  }

  public void setHighIntensity (boolean value)
  {
    currentContext = contextManager.setHighIntensity (currentContext, value);
    storeCurrentContext ();
  }

  public void reset (byte value)
  {
    overrideContext = null;
  }

  private void storeCurrentContext ()
  {
    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    storeContext (screenPosition);
  }

  private void storeContext (ScreenPosition screenPosition)
  {
    if (overrideContext != null)
    {
      screenPosition.setScreenContext (overrideContext);
      //      System.out.printf ("Storing: %s%n", overrideContext);
    }
    else
    {
      screenPosition.setScreenContext (currentContext);
      //      System.out.printf ("Storing: %s%n", currentContext);
    }
  }

  public void writeGraphics (byte b)
  {
    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    screenPosition.reset ();
    storeContext (screenPosition);
    screenPosition.setGraphicsChar (b);
    moveRight ();
  }

  public void write (byte b)
  {
    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    screenPosition.reset ();
    storeContext (screenPosition);
    screenPosition.setChar (b);
    moveRight ();
  }

  public void write (String text)
  {
    for (byte b : text.getBytes ())
      write (b);
  }

  private int findStartPosition (int position)
  {
    // find previous start field attribute
    int pos = position;
    while (true)
    {
      pos = screen.validate (pos - 1);
      ScreenPosition sp = screen.getScreenPosition (pos);
      if (sp.isStartField ())
      {
        //        System.out.println ("new start pos " + pos);
        return pos;
      }
      if (pos == position)
      {
        //        System.out.println ("wrapped around");
        break;
      }
    }
    System.out.println ("No start field found");
    return -1;
  }

  public void moveTo (int position)
  {
    currentPosition = screen.validate (position);

    int pos = findStartPosition (currentPosition);
    if (pos >= 0)
    {
      startFieldPosition = pos;
      currentContext = screen.getScreenPosition (startFieldPosition).getScreenContext ();
      storeCurrentContext ();
    }
  }

  public void moveRight ()
  {
    currentPosition = screen.validate (currentPosition + 1);
  }
}
