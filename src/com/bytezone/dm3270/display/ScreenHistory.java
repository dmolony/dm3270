package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.commands.AIDCommand;

public class ScreenHistory
{
  private static final int MAX_SCREENS = 20;

  //  private final List<ImageView> screens = new ArrayList<> ();
  private final List<UserScreen> screens = new ArrayList<> ();

  private boolean keyboardLocked;
  private boolean paused;
  private int currentScreen = -1;       // never been set

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