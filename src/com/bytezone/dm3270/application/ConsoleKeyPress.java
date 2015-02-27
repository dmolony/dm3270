package com.bytezone.dm3270.application;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.Screen;

class ConsoleKeyPress implements EventHandler<KeyEvent>
{
  private static final KeyCode[] keyCodes = { KeyCode.F1, KeyCode.F2, KeyCode.F3,
                                             KeyCode.F4, KeyCode.F5, KeyCode.F6,
                                             KeyCode.F7, KeyCode.F8, KeyCode.F9,
                                             KeyCode.F10, KeyCode.F11, KeyCode.F12 };
  private final ScreenHandler screenHandler;
  private final Screen screen;
  private final ConsoleStage console;

  public ConsoleKeyPress (ConsoleStage console, ScreenHandler screenHandler, Screen screen)
  {
    this.console = console;
    this.screenHandler = screenHandler;
    this.screen = screen;
  }

  @Override
  public void handle (KeyEvent e)
  {
    if (e.isMetaDown ())
      return;

    if (screenHandler.isKeyboardLocked ())
    {
      System.out.println ("Locked keyboard - ignoring : " + e);
      return;
    }

    int pfKey = 0;

    KeyCode keyCodePressed = e.getCode ();

    switch (keyCodePressed)
    {
      case ENTER:
        screenHandler.getCursor ().setVisible (false);
        screenHandler.setAID ("ENTR");
        AIDCommand command = new AIDCommand (screenHandler, screen);
        console.sendData (command.getTelnetData ());
        break;

      case TAB:
        screenHandler.tab (e.isShiftDown ());
        break;

      case LEFT:
      case RIGHT:
      case UP:
      case DOWN:
        screenHandler.moveCursor (e.getCode ());
        break;

      case BACK_SPACE:
        screenHandler.backspace ();
        break;

      case DELETE:
        System.out.println ("delete");
        break;

      case ESCAPE:
        System.out.println ("escape");    // CLR key?
        break;

      default:
        boolean found = false;
        for (KeyCode keyCode : keyCodes)
        {
          ++pfKey;
          if (keyCode == keyCodePressed)
          {
            found = true;
            break;
          }
        }
        if (found)
        {
          if (e.isShiftDown ())
            pfKey += 12;

          screenHandler.getCursor ().setVisible (false);
          screenHandler.setAID ("PF" + pfKey);
          command = new AIDCommand (screenHandler, screen);
          console.sendData (command.getTelnetData ());
        }
        break;
    }
  }
}