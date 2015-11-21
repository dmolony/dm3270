package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.display.Screen;

public class WriteControlCharacter
{
  private final byte value;
  private final boolean resetPartition;
  private final boolean startPrinter;
  private final boolean soundAlarm;
  private final boolean restoreKeyboard;
  private final boolean resetModified;

  public WriteControlCharacter (byte value)
  {
    this.value = value;
    resetPartition = (value & 0x40) > 0;
    startPrinter = (value & 0x08) > 0;
    soundAlarm = (value & 0x04) > 0;
    restoreKeyboard = (value & 0x02) > 0;
    resetModified = (value & 0x01) > 0;
  }

  byte getValue ()
  {
    return value;
  }

  boolean isResetModified ()
  {
    return resetModified;
  }

  void process (Screen screen)
  {
    screen.resetInsertMode ();

    if (resetPartition)
      screen.resetPartition ();
    if (startPrinter)
      screen.startPrinter ();
    if (soundAlarm)
      screen.soundAlarm ();
    if (resetModified)
      screen.resetModified ();
    if (restoreKeyboard)
      screen.restoreKeyboard ();
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("reset MDT=%s", resetModified ? "yes" : "no "));
    text.append (String.format (", keyboard=%s", restoreKeyboard ? "yes" : "no "));
    text.append (String.format (", alarm=%s", soundAlarm ? "yes" : "no "));
    text.append (String.format (", printer=%s", startPrinter ? "yes" : "no "));
    text.append (String.format (", partition=%s", resetPartition ? "yes" : "no"));
    return text.toString ();
  }
}