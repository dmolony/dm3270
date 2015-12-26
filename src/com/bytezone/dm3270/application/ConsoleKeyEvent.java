package com.bytezone.dm3270.application;

import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.utilities.Dm3270Utility;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

public class ConsoleKeyEvent implements EventHandler<KeyEvent>
{
  private final Screen screen;
  private final Cursor cursor;

  public ConsoleKeyEvent (Screen screen)
  {
    this.screen = screen;
    this.cursor = screen.getScreenCursor ();
  }

  @Override
  public void handle (KeyEvent e)                     // onKeyTyped
  {
    if (e.getEventType () != KeyEvent.KEY_TYPED)
      return;
    String c = e.getCharacter ();
    if (c.isEmpty ())
      return;
    char ch = c.charAt (0);

    if (screen.isKeyboardLocked () || e.isMetaDown () || e.isControlDown ()
        || e.isAltDown ())
    {
      // seems to be a bug in java
      if (e.isMetaDown ())
      {
        if (ch == '-')                    // osx fix
        {
          screen.getFontManager ().smaller ();
          e.consume ();
        }
        else if (ch == '=')               // osx fix
        {
          screen.getFontManager ().bigger ();
          e.consume ();
        }
      }

      return;
    }

    if (ch >= 32 && ch < 0x7F)
    {
      cursor.typeChar ((byte) Dm3270Utility.asc2ebc[ch]);
      e.consume ();
    }
  }
}