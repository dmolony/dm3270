package com.bytezone.dm3270.commands;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.Screen.ScreenOption;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.orders.TextOrder;

public class WriteCommand extends Command
{
  private final boolean eraseWrite;
  private final boolean alternate;
  private final WriteControlCharacter writeControlCharacter;
  private final List<Order> orders = new ArrayList<Order> ();

  private SystemMessage systemMessage;

  public WriteCommand (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);

    assert buffer[offset] == Command.WRITE_01 || buffer[offset] == Command.WRITE_F1
        || buffer[offset] == Command.ERASE_WRITE_05
        || buffer[offset] == Command.ERASE_WRITE_F5
        || buffer[offset] == Command.ERASE_WRITE_ALTERNATE_0D
        || buffer[offset] == Command.ERASE_WRITE_ALTERNATE_7E;

    eraseWrite = buffer[offset] != Command.WRITE_F1 && buffer[offset] != Command.WRITE_01;
    alternate = buffer[offset] == Command.ERASE_WRITE_ALTERNATE_0D
        || buffer[offset] == Command.ERASE_WRITE_ALTERNATE_7E;
    writeControlCharacter =
        length > 1 ? new WriteControlCharacter (buffer[offset + 1]) : null;

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
  }

  // Used by MainframeStage.createCommand() when building a screen
  public WriteCommand (WriteControlCharacter wcc, boolean erase, boolean alternate,
      List<Order> orders)
  {
    this.writeControlCharacter = wcc;
    this.eraseWrite = erase;
    this.alternate = alternate;
    this.orders.addAll (orders);

    // create new data buffer
    int length = 2;                 // command + WCC
    for (Order order : orders)
      length += order.size ();
    data = new byte[length];

    int ptr = 0;

    // add the command and WCC
    data[ptr++] =
        erase ? alternate ? ERASE_WRITE_ALTERNATE_7E : ERASE_WRITE_F5 : WRITE_F1;
    data[ptr++] = wcc.getValue ();

    // add each order
    for (Order order : orders)
      ptr = order.pack (data, ptr);

    assert ptr == data.length;
  }

  @Override
  public void process (Screen screen)
  {
    Cursor cursor = screen.getScreenCursor ();
    int cursorLocation = cursor.getLocation ();
    //    screen.lockKeyboard ("Inhibit");
    boolean screenDrawRequired = false;

    if (eraseWrite)
    {
      screen.setCurrentScreen (alternate ? ScreenOption.ALTERNATE : ScreenOption.DEFAULT);
      screen.lockKeyboard ("Erase Write");
      screen.clearScreen ();            // resets pen
    }
    else
      screen.lockKeyboard ("Write");

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
      if (screen.getFieldManager ().size () > 0 && !screen.isKeyboardLocked ())
        screen.checkRecording ();                   // make a copy of the screen
    }

    if (!screen.isKeyboardLocked () && screen.getFieldManager ().size () > 0)
    {
      if (orders.size () > 0 || !writeControlCharacter.isResetModified ())
        // should check for suppressDisplay
        setReply (screen.getPluginsStage ().processPluginAuto ());
    }

    if (screenDrawRequired)
      screen.draw ();

    // check screen for jobs submitted or finished
    systemMessage = screen.getSystemMessage ();
    systemMessage.checkSystemMessage (eraseWrite, orders, data.length);
  }

  // Used by Session.checkServerName() when searching for the server's name
  public List<Order> getOrdersList ()
  {
    return orders;
  }

  @Override
  public String getName ()
  {
    return eraseWrite ? alternate ? "Erase Write Alternate" : "Erase Write" : "Write";
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
      String fmt = (order.isText ()) ? "%s" : "%n%-40s";
      text.append (String.format (fmt, order));
    }

    return text.toString ();
  }
}