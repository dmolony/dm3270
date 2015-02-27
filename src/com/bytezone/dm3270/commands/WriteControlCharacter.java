package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.application.ScreenHandler;
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

  public byte getValue ()
  {
    return value;
  }

  public boolean process (ScreenHandler screenHandler, Screen screen)
  {
    if (resetPartition)
    {
      screenHandler.resetPartition ();      // this moves the cursor to 0/0 maybe?
      screen.resetPartition ();      // this moves the cursor to 0/0 maybe?
    }
    if (startPrinter)
    {
      screenHandler.startPrinter ();
      screen.startPrinter ();
    }
    if (soundAlarm)
    {
      screenHandler.soundAlarm ();
      screen.soundAlarm ();
    }
    if (restoreKeyboard)
    {
      screenHandler.restoreKeyboard ();
      screen.restoreKeyboard ();
    }
    if (resetModified)
    {
      screenHandler.resetModified ();
      screen.resetModified ();
    }

    return true;
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