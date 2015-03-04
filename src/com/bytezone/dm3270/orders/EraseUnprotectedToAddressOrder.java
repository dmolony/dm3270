package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.Screen;

public class EraseUnprotectedToAddressOrder extends Order
{
  private final BufferAddress stopAddress;

  public EraseUnprotectedToAddressOrder (byte[] buffer, int offset)
  {
    assert buffer[offset] == Order.ERASE_UNPROTECTED;
    stopAddress = new BufferAddress (buffer[offset + 1], buffer[offset + 2]);

    this.buffer = new byte[3];
    System.arraycopy (buffer, offset, this.buffer, 0, this.buffer.length);
  }

  @Override
  public void process (Screen screen)
  {
    //    Cursor cursor = screenHandler.getCursor ();
    //    BufferAddress cursorAddress = cursor.getAddress ();
    //    List<ScreenField> fields =
    //        screenHandler.getScreenFields (FieldProtectionType.MODIFIABLE);
    //
    //    // find the cursor field
    //    ScreenField currentField = null;
    //    for (ScreenField sf : fields)
    //    {
    //      if (sf.contains (cursorAddress))
    //      {
    //        currentField = sf;
    //        break;
    //      }
    //    }
    //
    //    while (currentField != null)
    //    {
    //      currentField.clear ();
    //      if (currentField.contains (stopAddress))
    //      {
    //        cursor.setLocation (currentField.getStartPosition ());
    //        break;
    //      }
    //      currentField = currentField.getNext ();
    //    }

    Cursor cursor2 = screen.getScreenCursor ();
    int cursorPostion = cursor2.getLocation ();
    Field resetField = null;

    for (Field field : screen.getUnprotectedFields ())
      if (field.contains (cursorPostion))
      {
        resetField = field;
        break;
      }

    while (resetField != null)
    {
      resetField.clear (false);       // don't set modified (is this correct?)
      if (resetField.contains (stopAddress.getLocation ()))
      {
        cursor2.moveTo (resetField.getFirstLocation ());
        break;
      }
      resetField = resetField.getNextUnprotectedField ();
    }
  }

  @Override
  public String toString ()
  {
    return "EUA : " + stopAddress;
  }
}