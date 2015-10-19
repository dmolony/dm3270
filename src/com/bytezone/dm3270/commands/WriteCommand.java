package com.bytezone.dm3270.commands;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.orders.TextOrder;

public class WriteCommand extends Command
{
  private final boolean eraseWrite;
  private final WriteControlCharacter writeControlCharacter;
  private final List<Order> orders = new ArrayList<Order> ();

  private final SystemMessage systemMessage;

  public WriteCommand (byte[] buffer, int offset, int length, Screen screen,
      boolean erase)
  {
    super (buffer, offset, length, screen);

    this.eraseWrite = erase;

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

      if (order.matchesPreviousOrder (previousOrder))
        previousOrder.incrementDuplicates ();           // and discard this Order
      else
      {
        orders.add (order);
        previousOrder = order;
      }

      ptr += order.size ();
    }
    systemMessage = new SystemMessage (screen);
  }

  // Used by MainframeStage.createCommand() when building a screen
  public WriteCommand (WriteControlCharacter wcc, boolean erase, List<Order> orders)
  {
    super (null);

    this.writeControlCharacter = wcc;
    this.eraseWrite = erase;
    this.orders.addAll (orders);

    // create new data buffer
    int length = 2;                 // command + WCC
    for (Order order : orders)
      length += order.size ();
    data = new byte[length];

    int ptr = 0;

    // add the command and WCC
    data[ptr++] = erase ? ERASE_WRITE_F5 : WRITE_F1;
    data[ptr++] = wcc.getValue ();

    // add each order
    for (Order order : orders)
      ptr = order.pack (data, ptr);

    assert ptr == data.length;
    systemMessage = null;
  }

  @Override
  public void process ()
  {
    Cursor cursor = screen.getScreenCursor ();
    int cursorLocation = cursor.getLocation ();
    screen.lockKeyboard ("Inhibit");
    boolean screenDrawRequired = false;

    if (eraseWrite)
      screen.clearScreen ();            // resets pen
    //    else
    //      screen.getPen ().moveTo (0);

    if (orders.size () > 0)
    {
      for (Order order : orders)
        order.process (screen);         // modifies pen

      cursor.moveTo (cursorLocation);
      screen.buildFields (writeControlCharacter);
      screenDrawRequired = true;
    }

    if (writeControlCharacter != null)
    {
      writeControlCharacter.process (screen);       // may unlock the keyboard
      screen.checkRecording ();
    }

    if (!screen.isKeyboardLocked () && screen.getFieldManager ().size () > 0)
    {
      reply = screen.getPluginsStage ().processPluginAuto ();// check for suppressDisplay
    }

    if (screenDrawRequired)
      screen.draw ();

    // check screen for jobs submitted or finished
    if (orders.size () > 0 && systemMessage != null)
      systemMessage.checkSystemMessage (eraseWrite, orders);
  }

  // Used by Session.checkServerName() when searching for the server's name
  public List<Order> getOrdersList ()
  {
    return orders;
  }

  @Override
  public String getName ()
  {
    return eraseWrite ? "Erase Write" : "Write";
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