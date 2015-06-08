package com.bytezone.dm3270.display;

import javafx.scene.paint.Color;

public class Pen
{
  private Screen screen;
  private ContextManager contextManager;
  private ScreenContext currentContext;
  private int currentPosition;

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
    System.out.println ("reset Not finished!");
  }

  public void write (String text)
  {
    ScreenPosition screenPosition = screen.getScreenPosition (currentPosition);
    screenPosition.setScreenContext (currentContext);

    for (char c : text.toCharArray ())
    {
      screenPosition.setChar ((byte) c);
      moveTo (currentPosition + 1);
    }
  }

  public void moveTo (int position)
  {
    currentPosition = screen.validate (position);
  }
}
