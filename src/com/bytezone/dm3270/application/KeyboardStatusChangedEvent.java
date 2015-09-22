package com.bytezone.dm3270.application;

public final class KeyboardStatusChangedEvent
{
  public final boolean insertMode;
  public final boolean keyboardLocked;
  public final String keyName;

  public KeyboardStatusChangedEvent (boolean insertMode, boolean keyboardLocked,
      String keyName)
  {
    this.insertMode = insertMode;
    this.keyboardLocked = keyboardLocked;
    this.keyName = keyName;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Keyboard locked ... %s%n", keyboardLocked));
    text.append (String.format ("Insert mode on .... %s%n", insertMode));
    text.append (String.format ("Key pressed ....... %s%n", keyName));

    return text.toString ();
  }
}
