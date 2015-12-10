package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;

public class ContextManager
{
  private static final List<ScreenContext> contextPool = new ArrayList<> ();
  //  private final FontDetails fontDetails;
  //  private final ScreenDimensions screenDimensions;

  public ContextManager (ScreenDimensions screenDimensions, FontDetails fontDetails)
  {
    //    this.fontDetails = fontDetails;
    //    this.screenDimensions = screenDimensions;

    ScreenContext base = new ScreenContext (Color.GREEN, Color.YELLOW, (byte) 0, true,
        fontDetails, screenDimensions);
    contextPool.add (base);         // obviously garish and noticeable
  }

  public ScreenContext getDefaultScreenContext ()
  {
    return contextPool.get (0);
  }

  public void dump ()
  {
    System.out.println ();
    for (ScreenContext screenContext : contextPool)
      System.out.println (screenContext);
  }

  public ScreenContext setForeground (ScreenContext oldContext, Color color)
  {
    for (ScreenContext sc : contextPool)
      if (sc.foregroundColor == color //
          && sc.backgroundColor == oldContext.backgroundColor
          && sc.highlight == oldContext.highlight
          && sc.highIntensity == oldContext.highIntensity)
        return sc;
    ScreenContext newContext = new ScreenContext (color, oldContext.backgroundColor,
        oldContext.highlight, oldContext.highIntensity, oldContext.fontDetails,
        oldContext.screenDimensions);
    contextPool.add (newContext);
    return newContext;
  }

  public ScreenContext setBackground (ScreenContext oldContext, Color color)
  {
    for (ScreenContext sc : contextPool)
      if (sc.backgroundColor == color //
          && sc.foregroundColor == oldContext.foregroundColor
          && sc.highlight == oldContext.highlight
          && sc.highIntensity == oldContext.highIntensity)
        return sc;
    ScreenContext newContext = new ScreenContext (oldContext.foregroundColor, color,
        oldContext.highlight, oldContext.highIntensity, oldContext.fontDetails,
        oldContext.screenDimensions);
    contextPool.add (newContext);
    return newContext;
  }

  public ScreenContext setHighlight (ScreenContext oldContext, byte highlight)
  {
    for (ScreenContext sc : contextPool)
      if (sc.backgroundColor == oldContext.backgroundColor
          && sc.foregroundColor == oldContext.foregroundColor //
          && sc.highlight == highlight //
          && sc.highIntensity == oldContext.highIntensity)
        return sc;
    ScreenContext newContext = new ScreenContext (oldContext.foregroundColor,
        oldContext.backgroundColor, highlight, oldContext.highIntensity,
        oldContext.fontDetails, oldContext.screenDimensions);
    contextPool.add (newContext);
    return newContext;
  }

  public ScreenContext setHighIntensity (ScreenContext oldContext, boolean highIntensity)
  {
    for (ScreenContext sc : contextPool)
      if (sc.backgroundColor == oldContext.backgroundColor
          && sc.foregroundColor == oldContext.foregroundColor
          && sc.highlight == oldContext.highlight //
          && sc.highIntensity == highIntensity)
        return sc;
    ScreenContext newContext = new ScreenContext (oldContext.foregroundColor,
        oldContext.backgroundColor, oldContext.highlight, highIntensity,
        oldContext.fontDetails, oldContext.screenDimensions);
    contextPool.add (newContext);
    return newContext;
  }
}