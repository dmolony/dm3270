package com.bytezone.dm3270.display;

import java.util.List;

import javafx.scene.image.ImageView;

public class ScreenHistory
{
  private List<ImageView> screens;
  private boolean keyboardLocked;
  private boolean paused;
  private int currentScreen;

  public boolean isPaused ()
  {
    return paused;
  }

  public void pause (boolean keyboardLocked, List<ImageView> screens)
  {
    this.keyboardLocked = keyboardLocked;
    this.screens = screens;
    paused = true;
  }

  public boolean resume ()
  {
    screens = null;
    paused = false;
    return keyboardLocked;
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

  //    public ImageView getImageView (int index)
  //    {
  //      int position = screens.size () - index;
  //      if (position >= 0 && position < screens.size ())
  //        return screens.get (position);
  //      return null;
  //    }
}