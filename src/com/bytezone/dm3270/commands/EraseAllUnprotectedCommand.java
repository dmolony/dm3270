package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.display.Screen;

public class EraseAllUnprotectedCommand extends Command
{
  // This command has no WCC or data.
  public EraseAllUnprotectedCommand (Screen screen, byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length, screen);
  }

  @Override
  public void process ()
  {
    screen.eraseAllUnprotected ();
    screen.setAID (AIDCommand.NO_AID_SPECIFIED);      // also reset keyboard
  }

  @Override
  public String getName ()
  {
    return "Erase All Unprotected";
  }

  @Override
  public String toString ()
  {
    return "EAU  :";
  }
}