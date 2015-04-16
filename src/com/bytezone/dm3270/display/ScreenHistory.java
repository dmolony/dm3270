package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.ImageView;

public class ScreenHistory
{
  private static final int MAX_SCREENS = 15;

  private final List<ImageView> screens = new ArrayList<> ();
  private boolean keyboardLocked;
  private boolean paused;
  private int currentScreen = -1;       // never been set

  public boolean isPaused ()
  {
    return paused;
  }

  public void pause (boolean keyboardLocked)
  {
    this.keyboardLocked = keyboardLocked;
    paused = true;
    if (currentScreen < 0)
      currentScreen = screens.size () - 1;
  }

  public boolean resume ()
  {
    paused = false;
    return keyboardLocked;
  }

  public void add (ImageView imageView)
  {
    screens.add (imageView);
    if (screens.size () > MAX_SCREENS)
      screens.remove (0);
    else if (currentScreen >= 0 && currentScreen < screens.size () - 1)
      ++currentScreen;
  }

  public boolean hasNext ()
  {
    return currentScreen < screens.size () - 1;
  }

  public boolean hasPrevious ()
  {
    return currentScreen > 0;
  }

  public ImageView next ()
  {
    if (hasNext ())
      return screens.get (++currentScreen);
    return null;
  }

  public ImageView current ()
  {
    return screens.get (currentScreen);
  }

  public ImageView previous ()
  {
    if (hasPrevious ())
      return screens.get (--currentScreen);
    return null;
  }
}