package com.bytezone.dm3270.display;

import javafx.scene.paint.Color;

import com.bytezone.dm3270.attributes.StartFieldAttribute;

public class Pen
{
  private final Screen screen;
  private final ContextManager contextManager;

  private ScreenContext currentContext;
  private int currentPosition;
  private int startFieldPosition;

  private boolean erase;

  public Pen (Screen screen)
  {
    this.screen = screen;
    contextManager = new ContextManager ();
  }

  public ScreenContext getBase ()
  {
    return contextManager.getBase ();
  }

  public void setErase (boolean erase)
  {
    this.erase = erase;
  }

  //  public void dump ()
  //  {
  //    contextManager.dump ();
  //  }

  public void startField (StartFieldAttribute startFieldAttribute)
  {
    currentContext = contextManager.getBase ();

    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    screenPosition.reset ();
    screenPosition.setStartField (startFieldAttribute);
    screenPosition.setVisible (false);
    //    screenPosition.setScreenContext (currentContext);

    startFieldPosition = currentPosition;
  }

  public int getPosition ()
  {
    return currentPosition;
  }

  public void setForeground (Color color)
  {
    currentContext = contextManager.setForeground (currentContext, color);
    //    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    //    screenPosition.setScreenContext (currentContext);
    storeCurrentContext ();
  }

  public void setBackground (Color color)
  {
    currentContext = contextManager.setBackground (currentContext, color);
    //    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    //    screenPosition.setScreenContext (currentContext);
    storeCurrentContext ();
  }

  public void setHighlight (byte value)
  {
    currentContext = contextManager.setHighlight (currentContext, value);
    //    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    //    screenPosition.setScreenContext (currentContext);
    storeCurrentContext ();
  }

  public void setHighIntensity (boolean value)
  {
    currentContext = contextManager.setHighIntensity (currentContext, value);
    //    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    //    screenPosition.setScreenContext (currentContext);
    storeCurrentContext ();
  }

  public void reset (byte value)
  {
    assert value == 0;
    System.out.println ();
    System.out.println ("reset at:        " + currentPosition);
    System.out.println ("current setting: " + startFieldPosition);

    if (!erase)
      startFieldPosition = findStartPosition (currentPosition);

    currentContext = screen.getScreenPosition (startFieldPosition).getScreenContext ();
    //    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    //    screenPosition.setScreenContext (currentContext);
    storeCurrentContext ();
  }

  private void storeCurrentContext ()
  {
    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    screenPosition.setScreenContext (currentContext);
  }

  public void writeGraphics (byte b)
  {
    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    screenPosition.reset ();
    screenPosition.setScreenContext (currentContext);
    screenPosition.setGraphicsChar (b);
    moveRight ();
  }

  public void write (byte b)
  {
    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    screenPosition.reset ();
    screenPosition.setScreenContext (currentContext);
    screenPosition.setChar (b);
    moveRight ();
  }

  public void write (String text)
  {
    for (byte b : text.getBytes ())
      write (b);
  }

  public void jumpTo (int position)
  {
    System.out.printf ("Moved to %d%n", position);
    moveTo (position);

    if (!erase)
    {
      startFieldPosition = findStartPosition (position);
      currentContext = screen.getScreenPosition (startFieldPosition).getScreenContext ();

      ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
      screenPosition.setScreenContext (currentContext);
    }
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
        System.out.println ("new start pos " + pos);
        return pos;
      }
      if (pos == position)
      {
        System.out.println ("wrapped around");
        break;
      }
    }
    assert false;
    return -1;
  }

  private void moveTo (int position)
  {
    currentPosition = screen.validate (position);
  }

  public void moveRight ()
  {
    moveTo (currentPosition + 1);
  }
}
