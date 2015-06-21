package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.structuredfields.SetReplyMode;

public class ScreenHistory
{
  private static final int MAX_SCREENS = 20;
  private static final byte[] replyTypes = { //
      Attribute.XA_HIGHLIGHTING, Attribute.XA_FGCOLOR, Attribute.XA_CHARSET,
          Attribute.XA_BGCOLOR, Attribute.XA_TRANSPARENCY };

  private final List<UserScreen> screens = new ArrayList<> ();

  private boolean keyboardLocked;       // save previous setting
  private boolean paused;
  private int currentScreen = -1;       // never been set

  void requestScreen (Screen screen)
  {
    screen.setReplyMode (SetReplyMode.RM_CHARACTER, replyTypes);

    AIDCommand command = screen.readBuffer ();

    // check for duplicates
    if (screens.size () > 0)
    {
      UserScreen previousScreen = screens.get (screens.size () - 1);
      if (previousScreen.matches (command))
        return;
    }

    // check that the screen contains displayable data
    if (command.countTextOrders () <= 3)
    {
      boolean display = false;
      for (int i = 0; i < command.countTextOrders (); i++)
      {
        byte[] buffer = command.getText (i);
        for (byte b : buffer)
          if (b != 0)
          {
            display = true;
            break;
          }
      }
      if (display)
        add (command);
    }
    else
      add (command);
  }

  void add (AIDCommand command)
  {
    screens.add (new UserScreen (command));

    if (screens.size () > MAX_SCREENS)
    {
      screens.remove (0);
      if (currentScreen > 0)
        --currentScreen;
    }
  }

  public int size ()
  {
    return screens.size ();
  }

  public boolean isPaused ()
  {
    return paused;
  }

  public void pause (boolean keyboardLocked)
  {
    assert !paused;
    this.keyboardLocked = keyboardLocked;
    paused = true;
    if (currentScreen < 0)
      currentScreen = screens.size () - 1;
  }

  public boolean resume ()
  {
    assert paused;
    paused = false;
    return keyboardLocked;
  }

  public int getCurrentIndex ()
  {
    return currentScreen;
  }

  public boolean hasNext ()
  {
    assert paused;
    return currentScreen < screens.size () - 1;
  }

  public boolean hasPrevious ()
  {
    assert paused;
    return currentScreen > 0;
  }

  public UserScreen next ()
  {
    if (hasNext ())
      return screens.get (++currentScreen);
    return null;
  }

  public UserScreen current ()
  {
    assert paused;
    return screens.get (currentScreen);
  }

  public UserScreen previous ()
  {
    if (hasPrevious ())
      return screens.get (--currentScreen);
    return null;
  }
}