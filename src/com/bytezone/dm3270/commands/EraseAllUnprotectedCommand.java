package com.bytezone.dm3270.commands;

import java.util.List;

import com.bytezone.dm3270.application.Cursor;
import com.bytezone.dm3270.application.ScreenField;
import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.application.ScreenHandler.FieldProtectionType;

public class EraseAllUnprotectedCommand extends Command
{
  private static final boolean DONT_REBUILD_FIELDS = false;

  // This command has no WCC or data.
  public EraseAllUnprotectedCommand (ScreenHandler screenHandler, byte[] buffer,
      int offset, int length)
  {
    super (buffer, offset, length, screenHandler);
  }

  @Override
  public void process ()
  {
    // set all unprotected character locations to null
    for (ScreenField sf : screenHandler.getScreenFields (FieldProtectionType.MODIFIABLE))
      sf.clear ();

    screenHandler.restoreKeyboard ();
    screenHandler.resetModified ();

    // position cursor in first unprotected field
    List<ScreenField> fields =
        screenHandler.getScreenFields (FieldProtectionType.MODIFIABLE);
    Cursor cursor = screenHandler.getCursor ();
    if (fields.size () > 0)
      cursor.setLocation (fields.get (0).getStartPosition () + 1);
    else
      cursor.setLocation (0);

    screenHandler.draw (DONT_REBUILD_FIELDS);
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