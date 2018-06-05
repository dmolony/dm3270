package com.bytezone.dm3270.application;

public final class KeyboardStatusChangedEvent {

  public final boolean keyboardLocked;
  private final boolean insertMode;
  private final String keyName;

  public KeyboardStatusChangedEvent(boolean insertMode, boolean keyboardLocked,
      String keyName) {
    this.insertMode = insertMode;
    this.keyboardLocked = keyboardLocked;
    this.keyName = keyName;
  }

  @Override
  public String toString() {
    return String.format("Keyboard locked ... %s%n", keyboardLocked)
        + String.format("Insert mode on .... %s%n", insertMode)
        + String.format("Key pressed ....... %s%n", keyName);
  }

}
