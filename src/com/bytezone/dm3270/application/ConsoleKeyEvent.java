package com.bytezone.dm3270.application;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ConsoleKeyEvent implements EventHandler<KeyEvent>
{
  private final ScreenHandler screenHandler;
  private boolean keyboardLocked;

  public ConsoleKeyEvent (ScreenHandler screenHandler)
  {
    this.screenHandler = screenHandler;
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
      String c = e.getCharacter ();
      if (!c.isEmpty ())
      {
        char ch = c.charAt (0);
        if (ch >= 32 && ch < 0x7F)
        {
          ScreenPosition sp = cursor.getScreenPosition ();
          sp.setCharacter ((byte) Utility.asc2ebc[ch]);
          cursor.setVisible (false);

          if (screenHandler.getCurrentField () != null)
            screenHandler.getCurrentField ().setModified (true);

          sp = screenHandler.getScreenPosition (cursor.moveRight ());
          //          if (sp.isStartField ())
          //          {
          //            screenHandler.setCurrentField (screenHandler.getCurrentField ().getNext ());
          //            cursor.setLocation (screenHandler.getCurrentField ().getStartPosition () + 1);
          //            sp = screenHandler.getScreenPosition (cursor.getLocation ());
          //          }

          cursor.setVisible (true);
        }
      }
    }
  }
}