package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.display.Screen;

public class EraseAllUnprotectedCommand extends Command
{
  // This command has no WCC or data.
  public EraseAllUnprotectedCommand (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);
    assert buffer[offset] == Command.ERASE_ALL_UNPROTECTED_0F
        || buffer[offset] == Command.ERASE_ALL_UNPROTECTED_6F;
  }

  @Override
  public void process (Screen screen)
  {
    screen.eraseAllUnprotected ();
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