package com.bytezone.dm3270.commands;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.orders.TextOrder;

public class WriteCommand extends Command
{
  private static final boolean REBUILD_FIELDS = true;

  private final boolean erase;
  private final WriteControlCharacter writeControlCharacter;
  private final List<Order> orders = new ArrayList<Order> ();

  public WriteCommand (byte[] buffer, int offset, int length, Screen screen, boolean erase)
  {
    super (buffer, offset, length, screen);

    this.erase = erase;

    // ?????
    // I think that this command (when sourced from a WSF command) has an address
    // field after the WCC. Perhaps the constructor could be passed the WCC and
    // starting address.

    if (length > 1)
      writeControlCharacter = new WriteControlCharacter (buffer[offset + 1]);
    else
      writeControlCharacter = null;

    int ptr = offset + 2;
    Order previousOrder = null;

    int max = offset + length;
    while (ptr < max)
    {
      Order order = Order.getOrder (buffer, ptr, max);

      if (order.rejected ())
        break;

      if (previousOrder != null && previousOrder.matches (order))
        previousOrder.incrementDuplicates ();
      else
      {
        orders.add (order);
        previousOrder = order;
      }

      ptr += order.size ();
    }
  }

  // Used by MainframeStage.createCommand() when building a screen
  public WriteCommand (WriteControlCharacter wcc, boolean erase, List<Order> orders)
  {
    super (null);
    this.writeControlCharacter = wcc;
    this.erase = erase;
    this.orders.addAll (orders);

    // create buffer
    int length = 2;     // command + wcc
    for (Order order : orders)
      length += order.size ();

    data = new byte[length];
    int ptr = 0;
    data[ptr++] = erase ? ERASE_WRITE_F5 : WRITE_F1;
    data[ptr++] = wcc.getValue ();
    for (Order order : orders)
      ptr = order.pack (data, ptr);
  }

  @Override
  public void process ()
  {
    Cursor cursor = screen.getScreenCursor ();
    int cursorLocation = cursor.getLocation ();
    screen.lockKeyboard ();

    if (erase)
      screen.clearScreen ();

    if (orders.size () > 0)
    {
      for (Order order : orders)
        order.process (screen);

      cursor.moveTo (cursorLocation);
      screen.drawScreen (REBUILD_FIELDS);
    }

    if (writeControlCharacter != null)
      writeControlCharacter.process (screen);     // may unlock the keyboard

    if (false)
    {
      screen.getScreenCursor ().moveTo (cursorLocation);
      int newCursorLocation = screen.getScreenCursor ().getLocation ();
      System.out.printf ("Cursor now at %d%n", newCursorLocation);
    }

    if (!screen.isKeyboardLocked () && orders.size () > 0)
      reply = screen.processPluginAuto ();
  }

  // Used by Session.checkServerName() when searching for the server's name
  public List<Order> getOrdersList ()
  {
    return orders;
  }

  @Override
  public String getName ()
  {
    return erase ? "Erase Write" : "Write";
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (getName ());
    text.append ("\nWCC : " + writeControlCharacter);

    // if the list begins with a TextOrder then tab out the missing columns
    if (orders.size () > 0 && orders.get (0) instanceof TextOrder)
      text.append (String.format ("%40s", ""));

    for (Order order : orders)
    {
      String fmt = (order instanceof TextOrder) ? "%s" : "%n%-40s";
      text.append (String.format (fmt, order));
    }

    return text.toString ();
  }
}