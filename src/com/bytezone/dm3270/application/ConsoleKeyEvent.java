package com.bytezone.dm3270.application;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import com.bytezone.dm3270.display.Screen;

public class ConsoleKeyEvent implements EventHandler<KeyEvent>
{
  private final Screen screen;

  public ConsoleKeyEvent (Screen screen)
  {
    this.screen = screen;
  }

  @Override
  public void handle (KeyEvent e)       // onKeyTyped
  {
    e.consume ();

    if (screen.isKeyboardLocked ())
    {
      //      System.out.println ("Locked keyboard event - ignoring : " + e);
      return;
    }

    if (e.getCode () == KeyCode.UNDEFINED && !e.isMetaDown ())
    {
      String c = e.getCharacter ();
      if (!c.isEmpty ())
      {
        char ch = c.charAt (0);
        if (ch >= 32 && ch < 0x7F)
          screen.getScreenCursor ().typeChar ((byte) Utility.asc2ebc[ch]);
      }
    }
  }
}