package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.orders.TextOrder;

import java.util.ArrayList;
import java.util.List;

public class WriteCommand extends Command {

  private final boolean eraseWrite;
  private final boolean alternate;
  private final WriteControlCharacter writeControlCharacter;
  private final List<Order> orders = new ArrayList<>();

  public WriteCommand(byte[] buffer, int offset, int length) {
    super(buffer, offset, length);

    assert buffer[offset] == Command.WRITE_01 || buffer[offset] == Command.WRITE_F1
        || buffer[offset] == Command.ERASE_WRITE_05
        || buffer[offset] == Command.ERASE_WRITE_F5
        || buffer[offset] == Command.ERASE_WRITE_ALTERNATE_0D
        || buffer[offset] == Command.ERASE_WRITE_ALTERNATE_7E;

    eraseWrite = buffer[offset] != Command.WRITE_F1 && buffer[offset] != Command.WRITE_01;
    alternate = buffer[offset] == Command.ERASE_WRITE_ALTERNATE_0D
        || buffer[offset] == Command.ERASE_WRITE_ALTERNATE_7E;
    writeControlCharacter =
        length > 1 ? new WriteControlCharacter(buffer[offset + 1]) : null;

    int ptr = offset + 2;
    Order previousOrder = null;

    int max = offset + length;
    while (ptr < max) {
      Order order = Order.getOrder(buffer, ptr, max);

      if (order.matchesPreviousOrder(previousOrder)) {
        previousOrder.incrementDuplicates();           // and discard this Order
      } else {
        orders.add(order);
        previousOrder = order;
      }

      ptr += order.size();
    }
  }

  @Override
  public void process(Screen screen) {
    Cursor cursor = screen.getScreenCursor();
    int cursorLocation = cursor.getLocation();
    boolean screenDrawRequired = false;

    if (eraseWrite) {
      screen.setCurrentScreen(
          alternate ? Screen.ScreenOption.ALTERNATE : Screen.ScreenOption.DEFAULT);
      screen.lockKeyboard("Erase Write");
      screen.clearScreen();            // resets pen
    } else {
      screen.lockKeyboard("Write");
    }

    if (orders.size() > 0) {
      for (Order order : orders) {
        order.process(screen);         // modifies pen
      }

      cursor.moveTo(cursorLocation);
      screen.buildFields();
      screenDrawRequired = true;
    }

    if (writeControlCharacter != null) {
      writeControlCharacter.process(screen);       // may unlock the keyboard
      if (screen.getFieldManager().size() > 0 && !screen.isKeyboardLocked()) {
        screen.checkRecording();                   // make a copy of the screen
      }
    }

    // should check for suppressDisplay
    if (!screen.isKeyboardLocked() && screen.getFieldManager().size() > 0) {
      if (orders.size() > 0 || !writeControlCharacter.isResetModified()) {
        setReply(null);
      }
    }

    if (screenDrawRequired) {
      screen.draw();
    }

  }

  @Override
  public String getName() {
    return eraseWrite ? alternate ? "Erase Write Alternate" : "Erase Write" : "Write";
  }

  @Override
  public String toString() {
    StringBuilder text = new StringBuilder();
    text.append(getName());
    text.append("\nWCC : ").append(writeControlCharacter);

    // if the list begins with a TextOrder then tab out the missing columns
    if (orders.size() > 0 && orders.get(0) instanceof TextOrder) {
      text.append(String.format("%40s", ""));
    }

    for (Order order : orders) {
      String fmt = (order.isText()) ? "%s" : "%n%-40s";
      text.append(String.format(fmt, order));
    }

    return text.toString();
  }

}
