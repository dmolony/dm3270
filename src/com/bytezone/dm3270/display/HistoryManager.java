package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.commands.AIDCommand;

public class HistoryManager
{
  private static final int MAX_SCREENS = 20;

  private final List<HistoryScreen> screens = new ArrayList<> (MAX_SCREENS);
  private final ContextManager contextManager;
  private final FieldManager fieldManager;
  private ScreenDimensions screenDimensions;

  private boolean keyboardLocked;       // save previous setting
  private boolean paused;
  private int currentScreen = -1;       // never been set

  public HistoryManager (ScreenDimensions screenDimensions, ContextManager contextManager,
      FieldManager fieldManager)
  {
    this.screenDimensions = screenDimensions;
    this.contextManager = contextManager;
    this.fieldManager = fieldManager;
  }

  // called from Screen.checkRecording()
  void saveScreen (AIDCommand command)
  {
    // check for duplicates
    if (screens.size () > 0)
    {
      HistoryScreen previousScreen = screens.get (screens.size () - 1);
      if (previousScreen.matches (command))
        return;
    }

    // check that the screen contains displayable data
    if (command.countTextOrders () > 3)
      add (command);
    else
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
  }

  public void setScreenDimensions (ScreenDimensions screenDimensions)
  {
    this.screenDimensions = screenDimensions;
  }

  private void add (AIDCommand command)
  {
    if (screens.size () == MAX_SCREENS)
    {
      screens.remove (0);
      if (currentScreen > 0)
        --currentScreen;
    }
    screens.add (new HistoryScreen (screenDimensions, command, contextManager,
        fieldManager));
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

  public HistoryScreen next ()
  {
    return hasNext () ? screens.get (++currentScreen) : null;
  }

  public HistoryScreen current ()
  {
    assert paused;
    return screens.get (currentScreen);
  }

  public HistoryScreen previous ()
  {
    if (hasPrevious ())
      return screens.get (--currentScreen);
    return null;
  }
}