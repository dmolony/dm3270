package com.bytezone.dm3270.commands;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.application.Cursor;
import com.bytezone.dm3270.application.ScreenCanvas;
import com.bytezone.dm3270.application.ScreenField;
import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.application.ScreenHandler.FieldProtectionType;
import com.bytezone.dm3270.application.ScreenPosition;
import com.bytezone.dm3270.display.Cursor2;
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

  private static byte[] keys = { //
      0, (byte) 0x60, (byte) 0x7D, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4,
          (byte) 0xF5, (byte) 0xF6, (byte) 0xF7, (byte) 0xF8, (byte) 0xF9, (byte) 0x7A,
          (byte) 0x7B, (byte) 0x7C, (byte) 0xC1, (byte) 0xC2, (byte) 0xC3, (byte) 0xC4,
          (byte) 0xC5, (byte) 0xC6, (byte) 0xC7, (byte) 0xC8, (byte) 0xC9, (byte) 0x4A,
          (byte) 0x4B, (byte) 0x4C, (byte) 0x6C, (byte) 0x6E, (byte) 0x6B, (byte) 0x6D,
          (byte) 0x6A, (byte) 0x61 };

  private static String[] keyNames = { //
      "Not found", "No AID", "ENTR", "PF1", "PF2", "PF3", "PF4", "PF5", "PF6", "PF7",
          "PF8", "PF9", "PF10", "PF11", "PF12", "PF13", "PF14", "PF15", "PF16", "PF17",
          "PF18", "PF19", "PF20", "PF21", "PF22", "PF23", "PF24", "PA1", "PA2", "PA3",
          "CLR", "CLR Partition", "Read Partition" };

  private int key;
  private byte keyCommand;
  private BufferAddress cursorAddress;

  private final List<ModifiedField> modifiedFields = new ArrayList<> ();
  private final List<Order> orders = new ArrayList<> ();

  // Constructor used for replies to Read Partition (Read Buffer) and user actions.
  // This method creates an AID from the current screen.
  // Should be replaced with a static factory method

  public AIDCommand (ScreenHandler screenHandler, Screen screen)
  {
    super (screenHandler, screen);
    AIDCommand command = readModified (screenHandler.getAID ());

    this.key = command.key;
    this.cursorAddress = command.cursorAddress;
    this.orders.addAll (command.orders);
    this.data = command.data;
  }

  public AIDCommand (ScreenHandler screenHandler, Screen screen, byte type)
  {
    super (screenHandler, screen);

    AIDCommand command = null;

    switch (type)
    {
      case Command.READ_BUFFER_F2:
      case Command.READ_BUFFER_02:
        command = readBuffer ();
        break;

      case Command.READ_MODIFIED_F6:
      case Command.READ_MODIFIED_06:
        command = readModified (AIDCommand.NO_AID_SPECIFIED);
        break;

      case Command.READ_MODIFIED_ALL_6E:
      case Command.READ_MODIFIED_ALL_0E:
        command = readModifiedAll (AIDCommand.NO_AID_SPECIFIED);
        break;

      default:
        System.out.printf ("Unknown value: %02X%n", data[0]);
    }

    if (command != null)
    {
      this.key = command.key;
      this.cursorAddress = command.cursorAddress;
      this.orders.addAll (command.orders);
      this.data = command.data;
    }
  }

  public AIDCommand (ScreenHandler screenHandler, Screen screen, byte[] buffer)
  {
    this (screenHandler, screen, buffer, 0, buffer.length);
  }

  public AIDCommand (ScreenHandler screenHandler, Screen screen, byte[] buffer,
      int offset, int length)
  {
    super (buffer, offset, length, screenHandler, screen);

    keyCommand = buffer[offset];
    key = findKey (keyCommand);

    if (length <= 1)
    {
      cursorAddress = null;
      return;
    }

    cursorAddress = new BufferAddress (buffer[offset + 1], buffer[offset + 2]);

    int ptr = offset + 3;
    int max = offset + length;
    Order previousOrder = null;
    SetBufferAddressOrder sba = null;

    while (ptr < max)
    {
      Order order = Order.getOrder (buffer, ptr, max);
      if (!order.rejected ())
      {
        if (previousOrder != null && previousOrder.matches (order))
          previousOrder.incrementDuplicates ();
        else
        {
          orders.add (order);
          previousOrder = order;
        }

        if (sba != null && order instanceof TextOrder)
          modifiedFields.add (new ModifiedField (sba, (TextOrder) order));
        sba =
            (order instanceof SetBufferAddressOrder) ? (SetBufferAddressOrder) order
                : null;
      }
      ptr += order.size ();
    }
  }

  private int findKey (byte keyCommand)
  {
    for (int i = 1; i < keys.length; i++)
      if (keys[i] == keyCommand)
        return i;
    return 0;
  }

  private AIDCommand readBuffer ()
  {
    byte[] buffer = new byte[4096];
    int ptr = 0;
    buffer[ptr++] = AID_READ_PARTITION;

    BufferAddress ba = screenHandler.getCursor ().getAddress ();
    ptr = ba.packAddress (buffer, ptr);

    for (ScreenField sf : screenHandler.getScreenFields ())
      ptr = sf.pack (buffer, ptr);

    return new AIDCommand (screenHandler, screen, buffer, 0, ptr);
  }

  private AIDCommand readModified (byte aid)
  {
    byte[] buffer = new byte[4096];
    int ptr = 0;
    buffer[ptr++] = aid;

    BufferAddress ba = screenHandler.getCursor ().getAddress ();
    ptr = ba.packAddress (buffer, ptr);

    for (ScreenField sf : screenHandler.getScreenFields (FieldProtectionType.MODIFIABLE))
      if (sf.isModified ())
      {
        buffer[ptr++] = Order.SET_BUFFER_ADDRESS;

        int startPos = sf.getStartPosition () + 1;      // wrapping??
        ba = new BufferAddress (startPos);
        ptr = ba.packAddress (buffer, ptr);

        for (byte b : sf.getData ())
          if (b != 0)                       // null suppression (is this sufficient?)
            buffer[ptr++] = b;
      }

    return new AIDCommand (screenHandler, screen, buffer, 0, ptr);
  }

  // not written yet
  private AIDCommand readModifiedAll (byte aid)
  {
    byte[] buffer = new byte[4096];
    int ptr = 0;
    buffer[ptr++] = aid;

    return new AIDCommand (screenHandler, screen, buffer, 0, ptr);
  }

  // copy modified fields back to the screen - only used in Replay mode
  // Normally an AID is a reply command (which is never processed)

  @Override
  public void process ()
  {
    Cursor cursor = screenHandler.getCursor ();
    Cursor2 cursor2 = screen.getScreenCursor ();
    cursor.setVisible (false);
    //    cursor2.setVisible (false);

    // test to see whether this is a field that was null suppressed into moving
    // elsewhere on the screen (like the TSO logoff command) - purely aesthetic
    ScreenField field = null;
    boolean done = false;

    if (modifiedFields.size () == 1 && true)
    {
      int cursorOldLocation = cursor2.getLocation ();
      int cursorDistance = cursorAddress.getLocation () - cursorOldLocation;
      //      System.out.printf ("%d %d%n", cursorOldLocation, cursorDistance);

      ModifiedField modifiedField = modifiedFields.get (0);
      if (modifiedField.textOrder.getBuffer ().length == cursorDistance)
      {
        //        ScreenField screenField = screenHandler.getField (modifiedField.sbaOrder);
        //        if (screenField.contains (modifiedField.sbaOrder.getBufferAddress ())
        //            && screenField.contains (cursorAddress)
        //            && screenField.contains (cursorOldLocation))
        //        {
        //          field = screenField;
        //          cursor.setLocation (cursorOldLocation);
        //          updateScreenField (modifiedField, cursor, screenHandler, screen);
        //        }
        Field field2 = cursor2.getCurrentField ();
        if (field2.contains (cursorOldLocation))
        {
          for (byte b : modifiedField.textOrder.getBuffer ())
            cursor2.typeChar (b);
          done = true;
        }
      }
    }

    if (field == null && !done)
      for (ModifiedField modifiedField : modifiedFields)
      {
        cursor.setAddress (modifiedField.sbaOrder);
        updateScreenField (modifiedField, cursor, screenHandler, screen);
      }

    // place cursor in new location
    if (cursorAddress != null)
    {
      cursor.setAddress (cursorAddress);
      cursor2.moveTo (cursorAddress.getLocation ());
    }

    cursor.setVisible (true);
    //    cursor2.setVisible (true);
  }

  private void updateScreenField (ModifiedField mf, Cursor cursor,
      ScreenHandler screenHandler, Screen screen)
  {
    ScreenCanvas canvas = screenHandler.getScreenCanvas ();
    for (byte b : mf.textOrder.getBuffer ())
    {
      ScreenPosition sp = cursor.getScreenPosition ();
      sp.setCharacter (b);
      cursor.moveRight ();
      canvas.draw (sp);
    }

    // set the actual field's modified flag
    ScreenField sf = screenHandler.getField (mf.sbaOrder);
    if (sf != null)
      sf.setModified (true);
    else
      System.out.println ("Screen field not found: " + mf);

    Field field = screen.getField (mf.sbaOrder.getBufferAddress ().getLocation ());
    //    System.out.println (field);
    field.setText (mf.textOrder.getBuffer ());
    field.draw ();
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

  @Override
  public String brief ()
  {
    return keyNames[key];
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("AID     : %-12s : %02X%n", keyNames[key], keyCommand));
    text.append (String.format ("Cursor  : %s%n", cursorAddress));

    if (modifiedFields.size () > 0)
    {
      text.append (String.format ("%nModified fields  : %d", modifiedFields.size ()));
      for (ModifiedField mf : modifiedFields)
      {
        text.append ("\nField   : ");
        text.append (mf);
      }
    }

    if (orders.size () > 0 && modifiedFields.size () == 0)
    {
      text.append (String.format ("%nOrders  : %d%n", orders.size ()));

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

  private class ModifiedField
  {
    SetBufferAddressOrder sbaOrder;
    TextOrder textOrder;

    public ModifiedField (SetBufferAddressOrder sbaOrder, TextOrder textOrder)
    {
      this.sbaOrder = sbaOrder;
      this.textOrder = textOrder;
    }

    @Override
    public String toString ()
    {
      return String.format ("%s : %s", sbaOrder, textOrder);
    }
  }
}