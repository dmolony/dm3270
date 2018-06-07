package com.bytezone.dm3270;

import com.bytezone.dm3270.display.ScreenDimensions;

/**
 * Type of terminal emulator to be used to connect to the server.
 * <p>
 * This allows specifying screen dimensions and other properties of the emulated terminal to connect
 * to the server.
 */
public class TerminalType {

  public static final TerminalType DEFAULT_TERMINAL_TYPE = new TerminalType(2,
      new ScreenDimensions(24, 80), false);

  private final int model;
  private final ScreenDimensions screenDimensions;
  private final boolean usesExtended3270;

  public TerminalType(int model, ScreenDimensions screenDimensions, boolean usesExtended3270) {
    this.model = model;
    this.screenDimensions = screenDimensions;
    this.usesExtended3270 = usesExtended3270;
  }

  public int getModel() {
    return model;
  }

  public ScreenDimensions getScreenDimensions() {
    return screenDimensions;
  }

  public boolean usesExtended3270() {
    return usesExtended3270;
  }

}
