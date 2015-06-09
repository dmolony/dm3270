package com.bytezone.dm3270.display;

import javafx.scene.paint.Color;

public class Pen
{
  private final Screen screen;
  private final ContextManager contextManager;

  private ScreenContext currentContext;
  private int currentPosition;
  private int startFieldPosition;

  public Pen (Screen screen)
  {
    this.screen = screen;
    contextManager = new ContextManager ();
  }

  public void startField ()
  {
    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    currentContext = contextManager.getBase ();
    startFieldPosition = currentPosition;
    screenPosition.reset ();
    screenPosition.setVisible (false);
  }

  public int getPosition ()
  {
    return currentPosition;
  }

  public void setForeground (Color color)
  {
    currentContext = contextManager.setForeground (currentContext, color);
  }

  public void setBackground (Color color)
  {
    currentContext = contextManager.setBackground (currentContext, color);
  }

  public void setHighlight (byte value)
  {
    currentContext = contextManager.setHighlight (currentContext, value);
  }

  public void setHighIntensity (boolean value)
  {
    currentContext = contextManager.setHighIntensity (currentContext, value);
  }

  public void reset (byte value)
  {
    currentContext = screen.getScreenPosition (startFieldPosition).getScreenContext ();
  }

  public void writeGraphics (byte b)
  {
    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    screenPosition.setScreenContext (currentContext);
    screenPosition.setGraphicsChar (b);
    moveRight ();
  }

  public void write (byte b)
  {
    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    screenPosition.setScreenContext (currentContext);
    screenPosition.setChar (b);
    moveRight ();
  }

  public void write (String text)
  {
    for (byte b : text.getBytes ())
      write (b);
  }

  public void moveTo (int position)
  {
    currentPosition = screen.validate (position);
  }

  public void moveRight ()
  {
    moveTo (currentPosition + 1);
  }
}
