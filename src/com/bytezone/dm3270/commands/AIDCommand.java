package com.bytezone.dm3270.commands;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.orders.BufferAddress;
import com.bytezone.dm3270.orders.BufferAddressSource;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.orders.SetBufferAddressOrder;
import com.bytezone.dm3270.orders.TextOrder;

public class AIDCommand extends Command implements BufferAddressSource
{
  public static final byte NO_AID_SPECIFIED = 0x60;
  public static final byte AID_READ_PARTITION = 0x61;
  public static final byte AID_PA3 = 0x6B;
  public static final byte AID_PA1 = 0x6C;
  public static final byte AID_CLEAR = 0x6D;
  public static final byte AID_PA2 = 0x6E;
  public static final byte AID_ENTER = 0x7D;
  public static final byte AID_PF7 = (byte) 0xF7;
  public static final byte AID_PF8 = (byte) 0xF8;
  public static final byte AID_PF10 = (byte) 0x7A;
  public static final byte AID_PF11 = (byte) 0x7B;

  private static byte[] keys = { //
          0,
          NO_AID_SPECIFIED,
          AID_ENTER, //
          (byte) 0xF1, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4, (byte) 0xF5, (byte) 0xF6,
          (byte) 0xF7, (byte) 0xF8, (byte) 0xF9, (byte) 0x7A, (byte) 0x7B, (byte) 0x7C,
          (byte) 0xC1, (byte) 0xC2, (byte) 0xC3, (byte) 0xC4, (byte) 0xC5, (byte) 0xC6,
          (byte) 0xC7, (byte) 0xC8, (byte) 0xC9, (byte) 0x4A, (byte) 0x4B, (byte) 0x4C,
          AID_PA1, AID_PA2, AID_PA3, AID_CLEAR, (byte) 0x6A, AID_READ_PARTITION };

  private static String[] keyNames = { //
      "Not found",
          "No AID",
          "ENTR", //
          "PF1", "PF2", "PF3", "PF4", "PF5", "PF6", "PF7", "PF8", "PF9", "PF10", "PF11",
          "PF12", "PF13", "PF14", "PF15", "PF16", "PF17", "PF18", "PF19", "PF20", "PF21",
          "PF22", "PF23", "PF24", //
          "PA1", "PA2", "PA3", "CLR", "CLR Partition", "Read Partition" };

  private int key;
  private byte keyCommand;
  private BufferAddress cursorAddress;

  private final List<AIDField> aidFields = new ArrayList<> ();
  private final List<Order> orders = new ArrayList<> ();
  private int textOrders;

  // Called by Screen.readBuffer()
  public AIDCommand (Screen screen, byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length, screen);    // copies buffer[offset:length] to data[]

    keyCommand = data[0];
    key = findKey (keyCommand);

    if (length <= 1)
    {
      cursorAddress = null;
      return;
    }

    cursorAddress = new BufferAddress (data[1], data[2]);

    int ptr = 3;
    int max = length;
    Order previousOrder = null;
    AIDField currentAIDField = null;

    while (ptr < max)
    {
      Order order = Order.getOrder (data, ptr, max);
      if (!order.rejected ())
      {
        if (previousOrder != null && previousOrder.matches (order))
          previousOrder.incrementDuplicates ();
        else
        {
          orders.add (order);
          previousOrder = order;
        }

        if (order instanceof SetBufferAddressOrder)
        {
          currentAIDField = new AIDField ((SetBufferAddressOrder) order);
          aidFields.add (currentAIDField);
        }
        else if (currentAIDField != null)
          currentAIDField.addOrder (order);

        if (order instanceof TextOrder)
          textOrders++;
      }
      ptr += order.size ();
    }
  }

  public boolean isPAKey ()
  {
    // ignore any PA key reply caused by RMA
    if (data.length == 1
        && (keyCommand == AID_PA1 || keyCommand == AID_PA2 || keyCommand == AID_PA3))
      return true;
    return false;
  }

  private int findKey (byte keyCommand)
  {
    for (int i = 1; i < keys.length; i++)
      if (keys[i] == keyCommand)
        return i;
    return 0;
  }

  public void scramble ()
  {
    for (AIDField aidField : aidFields)
      aidField.scramble ();
  }

  // copy modified fields back to the screen - only used in Replay mode
  // Normally an AID is a reply command (which never has process() called)
  // Testing out whether the plugin reply should pass through here.
  @Override
  public void process ()
  {
    // test to see whether this is data entry that was null suppressed into moving
    // elsewhere on the screen (like the TSO logoff command) - purely aesthetic
    boolean done = aidFields.size () == 1 && checkForPrettyMove ();

    if (!done)
      for (AIDField aidField : aidFields)
        if (aidField.hasData ())
        {
          Field field = screen.getFieldManager ().getField (aidField.getLocation ());
          if (field != null)    // in replay mode we cannot rely on the fields list
          {
            field.setText (aidField.getBuffer ());
            field.draw ();
          }
        }

    // place cursor in new location
    if (cursorAddress != null)
      screen.getScreenCursor ().moveTo (cursorAddress.getLocation ());

    //    System.out.println ("locking: " + keyNames[key]);
    screen.lockKeyboard (keyNames[key]);
  }

  private boolean checkForPrettyMove ()
  {
    Cursor cursor = screen.getScreenCursor ();
    Field currentField = cursor.getCurrentField ();
    if (currentField != null)
    {
      int cursorOldLocation = cursor.getLocation ();
      if (cursorOldLocation != currentField.getFirstLocation ()
          && currentField.contains (cursorOldLocation))
      {
        int cursorDistance = cursorAddress.getLocation () - cursorOldLocation;
        byte[] buffer = aidFields.get (0).getBuffer ();
        if (buffer.length == cursorDistance)
        {
          // cannot call field.setText() as the data starts mid-field
          for (byte b : buffer)
            cursor.typeChar (b);   // send characters through the old cursor
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public BufferAddress getBufferAddress ()
  {
    return cursorAddress;
  }

  public byte getKeyCommand ()
  {
    return keyCommand;
  }

  @Override
  public String getName ()
  {
    return "AID : " + keyNames[key];
  }

  public static byte getKey (String name)
  {
    int ptr = 0;
    for (String keyName : keyNames)
    {
      if (keyName.equals (name))
        return keys[ptr];
      ptr++;
    }
    return -1;
  }

  public String getKeyName ()
  {
    return keyNames[key];
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("AID     : %-12s : %02X%n", keyNames[key], keyCommand));

    if (cursorAddress != null)
      text.append (String.format ("Cursor  : %s%n", cursorAddress));

    if (aidFields.size () > 0)
    {
      text.append (String.format ("%nModified fields  : %d", aidFields.size ()));
      for (AIDField aidField : aidFields)
      {
        text.append ("\nField   : ");
        text.append (aidField);
      }
    }
    // response to a read buffer request
    else if (orders.size () > 0)
    {
      text.append (String.format ("%nOrders  : %d%n", orders.size () - textOrders));
      text.append (String.format ("Text    : %d%n", textOrders));

      // if the list begins with a TextOrder then tab out the missing columns
      if (orders.size () > 0 && orders.get (0) instanceof TextOrder)
        text.append (String.format ("%40s", ""));

      for (Order order : orders)
      {
        String fmt = (order instanceof TextOrder) ? "%s" : "%n%-40s";
        text.append (String.format (fmt, order));
      }
    }
    return text.toString ();
  }

  // This class is used to collect information about each modified field specified
  // in the AIDCommand.
  private class AIDField
  {
    SetBufferAddressOrder sbaOrder;
    List<Order> orders = new ArrayList<> ();

    public AIDField (SetBufferAddressOrder sbaOrder)
    {
      this.sbaOrder = sbaOrder;
    }

    public void addOrder (Order order)
    {
      orders.add (order);
    }

    public int getLocation ()
    {
      return sbaOrder.getBufferAddress ().getLocation ();
    }

    public void scramble ()
    {
      for (Order order : orders)
        if (order instanceof TextOrder)
          ((TextOrder) order).scramble ();
    }

    public boolean hasData ()
    {
      return getBuffer ().length > 0;
    }

    public byte[] getBuffer ()
    {
      for (Order order : orders)
        if (order instanceof TextOrder)
          return ((TextOrder) order).getBuffer ();     // only returning the first one!!!
      return new byte[0];
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();
      text.append (String.format ("%-40s", sbaOrder));
      for (Order order : orders)
      {
        if (!(order instanceof TextOrder))
          text.append (String.format ("\n        : %-40s", order));
        else
          text.append (order);
      }
      return text.toString ();
    }
  }
}