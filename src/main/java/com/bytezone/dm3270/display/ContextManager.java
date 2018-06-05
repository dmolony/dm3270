package com.bytezone.dm3270.display;

import com.bytezone.dm3270.attributes.ColorAttribute;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContextManager {

  private static final List<ScreenContext> CONTEXT_POOL = new ArrayList<>();

  public ContextManager() {
    addNewContext(ColorAttribute.COLORS[0], ColorAttribute.COLORS[8], (byte) 0, false);
  }

  public ScreenContext getDefaultScreenContext() {
    return CONTEXT_POOL.get(0);
  }

  public ScreenContext getScreenContext(Color foregroundColor, Color backgroundColor,
                                        byte highlight, boolean highIntensity) {
    Optional<ScreenContext> opt = CONTEXT_POOL.stream().filter(sc -> sc
        .matches(foregroundColor, backgroundColor, highlight, highIntensity))
        .findFirst();

    return opt
        .orElseGet(() -> addNewContext(foregroundColor, backgroundColor, highlight, highIntensity));
  }

  public ScreenContext setForeground(ScreenContext oldContext, Color foregroundColor) {
    Optional<ScreenContext> opt = CONTEXT_POOL.stream()
        .filter(sc -> sc.matches(foregroundColor, oldContext.backgroundColor,
            oldContext.highlight, oldContext.highIntensity))
        .findFirst();

    return opt.orElseGet(() -> addNewContext(foregroundColor, oldContext.backgroundColor,
        oldContext.highlight, oldContext.highIntensity));
  }

  public ScreenContext setBackground(ScreenContext oldContext, Color backgroundColor) {
    Optional<ScreenContext> opt = CONTEXT_POOL.stream()
        .filter(sc -> sc.matches(oldContext.foregroundColor, backgroundColor,
            oldContext.highlight, oldContext.highIntensity))
        .findFirst();

    return opt.orElseGet(() -> addNewContext(oldContext.foregroundColor, backgroundColor,
        oldContext.highlight, oldContext.highIntensity));
  }

  public ScreenContext setHighlight(ScreenContext oldContext, byte highlight) {
    Optional<ScreenContext> opt =
        CONTEXT_POOL.stream()
            .filter(sc -> sc.matches(oldContext.foregroundColor,
                oldContext.backgroundColor, highlight,
                oldContext.highIntensity))
            .findFirst();

    return opt.orElseGet(() -> addNewContext(oldContext.foregroundColor, oldContext.backgroundColor,
        highlight, oldContext.highIntensity));
  }

  private ScreenContext addNewContext(Color foregroundColor, Color backgroundColor,
      byte highlight, boolean highIntensity) {
    ScreenContext newContext = new ScreenContext(foregroundColor, backgroundColor,
        highlight, highIntensity);
    CONTEXT_POOL.add(newContext);
    return newContext;
  }

}
