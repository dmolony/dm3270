package com.bytezone.dm3270.application;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import com.bytezone.dm3270.display.Cursor2;
import com.bytezone.dm3270.display.Cursor2.Direction;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.Screen;

public class ConsoleKeyEvent implements EventHandler<KeyEvent>
{
  private final ScreenHandler screenHandler;
  private final Screen screen;
  private boolean keyboardLocked;

  public ConsoleKeyEvent (ScreenHandler screenHandler, Screen screen)
  {
    this.screenHandler = screenHandler;
    this.screen = screen;
  }

  @Override
  public void handle (KeyEvent e)       // onKeyTyped
  {
    if (keyboardLocked)
    {
      System.out.println ("Locked keyboard - ignoring : " + e);
      return;
    }

    if (e.getCode () == KeyCode.UNDEFINED && !e.isMetaDown ())
    {
      Cursor cursor = screenHandler.getCursor ();
      Cursor2 cursor2 = screen.getScreenCursor ();

      String c = e.getCharacter ();
      if (!c.isEmpty ())
      {
        char ch = c.charAt (0);
        if (ch >= 32 && ch < 0x7F)
        {
          ScreenPosition screenPosition = cursor.getScreenPosition ();
          ScreenField screenField = screenHandler.getCurrentField ();

          if (screenField != null && screenField.isModifiable ())
          {
            screenPosition.setCharacter ((byte) Utility.asc2ebc[ch]);
            screenField.setModified (true);
            screenHandler.moveCursor (KeyCode.RIGHT);
          }

          Field currentField = cursor2.getCurrentField ();
          if (currentField != null && currentField.isUnprotected ())
          {
            cursor2.setChar ((byte) Utility.asc2ebc[ch]);
            cursor2.move (Direction.RIGHT);
            currentField.setModified (true);
          }
        }
      }
    }
  }
}