package com.bytezone.dm3270.application;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;

import com.bytezone.dm3270.attributes.ColorAttribute;

public class ContextHandler
{
  private final List<ScreenContext> contextPool = new ArrayList<> ();

  public ContextHandler ()
  {
    ScreenContext base =
        new ScreenContext (ColorAttribute.colors[0], ColorAttribute.colors[8], false,
            false, false);
    contextPool.add (base);
  }

  public ScreenContext getBase ()
  {
    return contextPool.get (0);
  }

  public ScreenContext setForeground (ScreenContext oldContext, Color color)
  {
    for (ScreenContext sc : contextPool)
      if (sc.foregroundColor == color //
          && sc.backgroundColor == oldContext.backgroundColor
          && sc.underscore == oldContext.underscore
          && sc.reverseVideo == oldContext.reverseVideo
          && sc.highIntensity == oldContext.highIntensity)
        return sc;
    ScreenContext newContext =
        new ScreenContext (color, oldContext.backgroundColor, oldContext.underscore,
            oldContext.reverseVideo, oldContext.highIntensity);
    contextPool.add (newContext);
    return newContext;
  }

  public ScreenContext setBackground (ScreenContext oldContext, Color color)
  {
    for (ScreenContext sc : contextPool)
      if (sc.backgroundColor == color //
          && sc.foregroundColor == oldContext.foregroundColor
          && sc.underscore == oldContext.underscore
          && sc.reverseVideo == oldContext.reverseVideo
          && sc.highIntensity == oldContext.highIntensity)
        return sc;
    ScreenContext newContext =
        new ScreenContext (oldContext.foregroundColor, color, oldContext.underscore,
            oldContext.reverseVideo, oldContext.highIntensity);
    contextPool.add (newContext);
    return newContext;
  }

  public ScreenContext setUnderscore (ScreenContext oldContext, boolean underscore)
  {
    for (ScreenContext sc : contextPool)
      if (sc.backgroundColor == oldContext.backgroundColor
          && sc.foregroundColor == oldContext.foregroundColor
          && sc.underscore == underscore && sc.reverseVideo == oldContext.reverseVideo
          && sc.highIntensity == oldContext.highIntensity)
        return sc;
    ScreenContext newContext =
        new ScreenContext (oldContext.foregroundColor, oldContext.backgroundColor,
            underscore, oldContext.reverseVideo, oldContext.highIntensity);
    contextPool.add (newContext);
    return newContext;
  }

  public ScreenContext setReverseVideo (ScreenContext oldContext, boolean reverseVideo)
  {
    for (ScreenContext sc : contextPool)
      if (sc.backgroundColor == oldContext.backgroundColor
          && sc.foregroundColor == oldContext.foregroundColor
          && sc.underscore == oldContext.underscore && sc.reverseVideo == reverseVideo
          && sc.highIntensity == oldContext.highIntensity)
        return sc;
    ScreenContext newContext =
        new ScreenContext (oldContext.foregroundColor, oldContext.backgroundColor,
            oldContext.underscore, reverseVideo, oldContext.highIntensity);
    contextPool.add (newContext);
    return newContext;
  }

  public ScreenContext setHighIntensity (ScreenContext oldContext, boolean highIntensity)
  {
    for (ScreenContext sc : contextPool)
      if (sc.backgroundColor == oldContext.backgroundColor
          && sc.foregroundColor == oldContext.foregroundColor
          && sc.underscore == oldContext.underscore
          && sc.reverseVideo == oldContext.reverseVideo
          && sc.highIntensity == highIntensity)
        return sc;
    ScreenContext newContext =
        new ScreenContext (oldContext.foregroundColor, oldContext.backgroundColor,
            oldContext.underscore, oldContext.reverseVideo, highIntensity);
    contextPool.add (newContext);
    return newContext;
  }

  public ScreenContext resetAttribute (ScreenContext oldContext)
  {
    return getBase ();
  }
}