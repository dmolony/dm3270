package com.bytezone.dm3270.orders;

import java.util.List;

import com.bytezone.dm3270.application.Cursor;
import com.bytezone.dm3270.application.ScreenField;
import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.application.ScreenHandler.FieldProtectionType;

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
  public void process (ScreenHandler screenHandler)
  {
    Cursor cursor = screenHandler.getCursor ();
    BufferAddress cursorAddress = cursor.getAddress ();
    List<ScreenField> fields =
        screenHandler.getScreenFields (FieldProtectionType.MODIFIABLE);

    // find the cursor field
    ScreenField currentField = null;
    for (ScreenField sf : fields)
    {
      if (sf.contains (cursorAddress))
      {
        currentField = sf;
        break;
      }
    }

    while (currentField != null)
    {
      currentField.clear ();
      if (currentField.contains (stopAddress))
      {
        cursor.setLocation (currentField.getStartPosition ());
        break;
      }
      currentField = currentField.getNext ();
    }
  }

  @Override
  public String toString ()
  {
    return "EUA : " + stopAddress;
  }
}