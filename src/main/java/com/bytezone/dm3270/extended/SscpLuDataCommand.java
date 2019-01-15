package com.bytezone.dm3270.extended;

import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.orders.InsertCursorOrder;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.orders.TextOrder;
import java.util.ArrayList;
import java.util.List;

public class SscpLuDataCommand extends Command {

  private final List<Order> orders = new ArrayList<>();

  public SscpLuDataCommand(byte[] buffer, int offset, int length) {
    super(buffer, offset, length);

    int ptr = offset;
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
    byte[] insertCursorBuffer = {Order.INSERT_CURSOR};
    orders.add(new InsertCursorOrder(insertCursorBuffer, 0));
  }

  @Override
  public void process(Screen screen) {
    screen.setCurrentScreen(Screen.ScreenOption.DEFAULT);
    screen.lockKeyboard("Erase Write");
    screen.clearScreen();
    screen.setSscpLuData();

    if (orders.size() > 0) {
      for (Order order : orders) {
        order.process(screen);
      }

      screen.buildFields();

      screen.resetInsertMode();
      screen.restoreKeyboard();

      screen.draw();
    }

  }

  @Override
  public String getName() {
    return "SSCP_LU_DATA";
  }

  @Override
  public String toString() {
    StringBuilder text = new StringBuilder();
    text.append(getName());

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
