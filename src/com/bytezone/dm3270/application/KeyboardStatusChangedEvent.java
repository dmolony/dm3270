package com.bytezone.dm3270.application;

public class KeyboardStatusChangedEvent
{
  boolean insertMode;
  boolean keyboardLocked;
  String keyName;

  public KeyboardStatusChangedEvent (boolean insertMode, boolean keyboardLocked,
      String keyName)
  {
    this.insertMode = insertMode;
    this.keyboardLocked = keyboardLocked;
    this.keyName = keyName;
  }
}
