package com.bytezone.dm3270.commands;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.application.ScreenHandler;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.orders.TextOrder;

public class WriteCommand extends Command
{
  private static final boolean REBUILD_FIELDS = true;
  private final boolean erase;
  private WriteControlCharacter writeControlCharacter;
  protected final List<Order> orders = new ArrayList<Order> ();

  public WriteCommand (byte[] buffer, int offset, int length,
      ScreenHandler screenHandler, Screen screen, boolean erase)
  {
    super (buffer, offset, length, screenHandler, screen);

    this.erase = erase;

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
      {
        System.out.println ("Rejected");
        System.out.println (order);
        break;
      }

      if (order.size () == 0)
      {
        System.out.println ("Not finished");
        System.out.println (order);
        break;
      }

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

  public WriteCommand (WriteControlCharacter wcc, boolean erase, List<Order> orders)
  {
    super (null, null);
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
    screenHandler.eraseScreen (erase);

    if (erase)
      screen.clearScreen ();

    if (writeControlCharacter != null)
      writeControlCharacter.process (screenHandler, screen);

    for (Order order : orders)
      order.process (screenHandler, screen);

    screenHandler.draw (REBUILD_FIELDS);

    screen.buildFields ();
    //    screen.drawScreen ();
  }

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