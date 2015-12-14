package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bytezone.dm3270.attributes.ColorAttribute;

import javafx.scene.paint.Color;

public class ContextManager
{
  private static final List<ScreenContext> contextPool = new ArrayList<> ();
  private FontDetails fontDetails;

  public ContextManager ()
  {
    addNewContext (ColorAttribute.colors[0], ColorAttribute.colors[8], (byte) 0, false);
  }

  public ScreenContext getDefaultScreenContext ()
  {
    return contextPool.get (0);
  }

  void setFontDetails (FontDetails fontDetails)
  {
    this.fontDetails = fontDetails;
    contextPool.forEach (sc -> sc.setFontDetails (fontDetails));
  }

  public void dump ()
  {
    System.out.println ();
    contextPool.forEach (System.out::println);
  }

  public ScreenContext getScreenContext (Color foregroundColor, Color backgroundColor,
      byte highlight, boolean highIntensity)
  {
    Optional<ScreenContext> opt = contextPool.stream ().filter (sc -> sc
        .matches (foregroundColor, backgroundColor, highlight, highIntensity))
        .findFirst ();

    return opt.isPresent () ? opt.get ()
        : addNewContext (foregroundColor, backgroundColor, highlight, highIntensity);
  }

  public ScreenContext setForeground (ScreenContext oldContext, Color foregroundColor)
  {
    Optional<ScreenContext> opt = contextPool.stream ()
        .filter (sc -> sc.matches (foregroundColor, oldContext.backgroundColor,
                                   oldContext.highlight, oldContext.highIntensity))
        .findFirst ();

    return opt.isPresent () ? opt.get ()
        : addNewContext (foregroundColor, oldContext.backgroundColor,
                         oldContext.highlight, oldContext.highIntensity);
  }

  public ScreenContext setBackground (ScreenContext oldContext, Color backgroundColor)
  {
    Optional<ScreenContext> opt = contextPool.stream ()
        .filter (sc -> sc.matches (oldContext.foregroundColor, backgroundColor,
                                   oldContext.highlight, oldContext.highIntensity))
        .findFirst ();

    return opt.isPresent () ? opt.get ()
        : addNewContext (oldContext.foregroundColor, backgroundColor,
                         oldContext.highlight, oldContext.highIntensity);
  }

  public ScreenContext setHighlight (ScreenContext oldContext, byte highlight)
  {
    Optional<ScreenContext> opt =
        contextPool.stream ()
            .filter (sc -> sc.matches (oldContext.foregroundColor,
                                       oldContext.backgroundColor, highlight,
                                       oldContext.highIntensity))
            .findFirst ();

    return opt.isPresent () ? opt.get ()
        : addNewContext (oldContext.foregroundColor, oldContext.backgroundColor,
                         highlight, oldContext.highIntensity);
  }

  public ScreenContext setHighIntensity (ScreenContext oldContext, boolean highIntensity)
  {
    Optional<ScreenContext> opt =
        contextPool.stream ()
            .filter (sc -> sc.matches (oldContext.foregroundColor,
                                       oldContext.backgroundColor, oldContext.highlight,
                                       highIntensity))
            .findFirst ();

    return opt.isPresent () ? opt.get ()
        : addNewContext (oldContext.foregroundColor, oldContext.backgroundColor,
                         oldContext.highlight, highIntensity);
  }

  private ScreenContext addNewContext (Color foregroundColor, Color backgroundColor,
      byte highlight, boolean highIntensity)
  {
    ScreenContext newContext = new ScreenContext (foregroundColor, backgroundColor,
        highlight, highIntensity, fontDetails);
    contextPool.add (newContext);
    return newContext;
  }
}