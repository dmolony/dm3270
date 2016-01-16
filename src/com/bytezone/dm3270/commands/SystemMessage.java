package com.bytezone.dm3270.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.assistant.BatchJobListener;
import com.bytezone.dm3270.console.ConsoleLog2;
import com.bytezone.dm3270.console.ConsoleLogListener;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.orders.TextOrder;
import com.bytezone.dm3270.utilities.Dm3270Utility;

import javafx.application.Platform;
import javafx.scene.control.MenuItem;

public class SystemMessage
{
  private static final Pattern jobSubmittedPattern = Pattern
      .compile ("^(?:[A-Z0-9]{1,9} )?JOB ([A-Z0-9]{1,9})\\(JOB(\\d{5})\\) SUBMITTED");

  private static final Pattern jobCompletedPattern =
      Pattern.compile ("(^\\d\\d(?:\\.\\d\\d){2}) JOB(\\d{5})"
          + " \\$HASP\\d+ ([A-Z0-9]+) .* MAXCC=(\\d+).*");

  private static final Pattern jobFailedPattern =
      Pattern.compile ("(^\\d\\d(?:\\.\\d\\d){2}) JOB(\\d{5})"
          + " \\$HASP\\d+ ([A-Z0-9]+) .* (JCL ERROR|ABENDED).*");

  private static final Pattern timePattern =
      Pattern.compile ("^TIME-(\\d{2}:\\d{2}:\\d{2}) (AM|PM).*");

  private static final Pattern datePattern =
      Pattern.compile ("^(\\d{2}/\\d{2}/\\d{4}) = (\\d{2}\\.\\d{3})"
          + " \\(\\w{3}\\) (\\d{2}:\\d{2}:\\d{2}).*");

  private static final byte[] systemMessage1 =
      { Order.SET_BUFFER_ADDRESS, Order.START_FIELD, 0x00, Order.START_FIELD,
        Order.SET_BUFFER_ADDRESS, Order.INSERT_CURSOR };

  private static final byte[] systemMessage2 =
      { Order.SET_BUFFER_ADDRESS, Order.START_FIELD, Order.SET_BUFFER_ADDRESS,
        Order.START_FIELD, 0x00, Order.START_FIELD, Order.SET_BUFFER_ADDRESS,
        Order.START_FIELD, 0x00, Order.START_FIELD, Order.INSERT_CURSOR };

  private static final byte[] systemMessage3 =
      { Order.SET_BUFFER_ADDRESS, Order.START_FIELD, Order.SET_BUFFER_ADDRESS,
        Order.START_FIELD, 0x00, Order.START_FIELD, Order.SET_BUFFER_ADDRESS,
        Order.INSERT_CURSOR };

  private static final byte[] systemMessage4 =
      { Order.SET_BUFFER_ADDRESS, Order.START_FIELD, Order.SET_BUFFER_ADDRESS,
        Order.START_FIELD, 0x00, Order.START_FIELD, Order.SET_BUFFER_ADDRESS,
        Order.START_FIELD, 0x00, Order.START_FIELD, Order.SET_BUFFER_ADDRESS,
        Order.START_FIELD, 0x00, Order.START_FIELD, Order.INSERT_CURSOR };

  private static final byte[] systemMessage5 =
      { Order.SET_BUFFER_ADDRESS, Order.START_FIELD, 0x00, Order.START_FIELD,
        Order.SET_BUFFER_ADDRESS, Order.START_FIELD, 0x00, Order.START_FIELD,
        Order.INSERT_CURSOR };

  private static final byte[] profileMessage =
      { Order.SET_BUFFER_ADDRESS, Order.START_FIELD, Order.SET_BUFFER_ADDRESS,
        Order.START_FIELD, 0x00, Order.SET_BUFFER_ADDRESS, 0x00, Order.START_FIELD,
        Order.SET_BUFFER_ADDRESS, Order.START_FIELD, 0x00, Order.START_FIELD,
        Order.SET_BUFFER_ADDRESS, Order.START_FIELD, 0x00, Order.START_FIELD,
        Order.INSERT_CURSOR };

  private static final byte[] consoleMessage =
      { Order.SET_BUFFER_ADDRESS, Order.START_FIELD, 0x00 };

  private final Screen screen;
  private final BatchJobListener batchJobListener;
  private final ConsoleLogListener consoleLogListener;
  private Profile profile;
  private boolean isConsole;
  private final ConsoleLog2 consoleLog2 = new ConsoleLog2 ();
  private MenuItem menuItem;
  private int lastOrdersSize;

  public SystemMessage (Screen screen, BatchJobListener batchJobListener,
      ConsoleLogListener consoleLogListener)
  {
    this.screen = screen;
    this.batchJobListener = batchJobListener;
    this.consoleLogListener = consoleLogListener;
  }

  void checkSystemMessage (boolean eraseWrite, List<Order> orders)
  {
    if (isConsole)
    {
      if (orders.size () == 3)
      {
        if (checkOrders (consoleMessage, orders))
        {
          //          checkConsoleOutput (orders);
          String text = Dm3270Utility.getString (orders.get (2).getBuffer ());
          consoleLogListener.addScreenText (text);
        }
      }
      else if (orders.size () == 2)
      {
        //        System.out.println ("two");
      }
      else
      {
        checkConsole2Output (orders);
      }
    }
    else if (eraseWrite)
    {
      switch (orders.size ())
      {
        case 8:
          if (checkOrders (systemMessage3, orders))
            checkSystemMessage (Dm3270Utility.getString (orders.get (4).getBuffer ()));
          return;

        case 11:
          if (checkOrders (systemMessage2, orders))
            checkSystemMessage (Dm3270Utility.getString (orders.get (4).getBuffer ()));
          return;

        case 15:
          if (checkOrders (systemMessage4, orders))
          {
            checkSystemMessage (Dm3270Utility.getString (orders.get (4).getBuffer ()));
            checkSystemMessage (Dm3270Utility.getString (orders.get (8).getBuffer ()));
          }
          return;

        case 17:
          if (checkOrders (profileMessage, orders))
            checkProfileMessage (Dm3270Utility.getString (orders.get (4).getBuffer ())
                + Dm3270Utility.getString (orders.get (6).getBuffer ()),
                                 Dm3270Utility.getString (orders.get (10).getBuffer ()));
          return;
      }
    }
    else
    {
      switch (orders.size ())
      {
        case 3:                                      // will only happen the first time
          if (lastOrdersSize == 2 && checkOrders (consoleMessage, orders))
            checkConsoleOutput (orders);
          return;

        case 6:
          if (checkOrders (systemMessage1, orders))
            checkSystemMessage (Dm3270Utility.getString (orders.get (2).getBuffer ()));
          return;

        case 9:
          if (checkOrders (systemMessage5, orders))
            checkSystemMessage (Dm3270Utility.getString (orders.get (2).getBuffer ()));
          return;
      }
    }
    lastOrdersSize = orders.size ();
  }

  public void dump (List<Order> orders)
  {
    System.out.printf ("Orders: %d%n", orders.size ());
    for (Order order : orders)
      System.out.println (order);
    System.out.println ("-------------------------------");
  }

  private boolean checkOrders (byte[] systemMessage, List<Order> orders)
  {
    int ptr = 0;
    for (Order order : orders)
    {
      byte reqType = systemMessage[ptr++];
      if (reqType != 0 && reqType != order.getType ())
        return false;
    }
    return true;
  }

  private void checkSystemMessage (String systemMessageText)
  {
    Matcher matcher = jobSubmittedPattern.matcher (systemMessageText);
    if (matcher.matches ())
    {
      String jobName = matcher.group (1);
      int jobNumber = Integer.parseInt (matcher.group (2));
      batchJobListener.batchJobSubmitted (jobNumber, jobName);
      return;
    }

    matcher = jobCompletedPattern.matcher (systemMessageText);
    if (matcher.matches ())
    {
      int jobNumber = Integer.parseInt (matcher.group (2));
      String jobName = matcher.group (3);
      String time = matcher.group (1);
      int conditionCode = Integer.parseInt (matcher.group (4));
      batchJobListener.batchJobEnded (jobNumber, jobName, time, conditionCode);
      return;
    }

    matcher = jobFailedPattern.matcher (systemMessageText);
    if (matcher.matches ())
    {
      int jobNumber = Integer.parseInt (matcher.group (2));
      String jobName = matcher.group (3);
      String time = matcher.group (1);
      batchJobListener.batchJobFailed (jobNumber, jobName, time);
      return;
    }

    // TIME-11:04:34 PM. CPU-00:00:03 SERVICE-8464 SESSION-00:00:25 NOVEMBER 2,2015

    matcher = timePattern.matcher (systemMessageText);
    if (matcher.matches ())
    {
      System.out.print ("Time is: " + matcher.group (1));     // hh:mm:ss
      System.out.println (" " + matcher.group (2));           // AM or PM
      return;
    }

    // 11/02/2015 = 15.306 (MON) 23:04:28

    matcher = datePattern.matcher (systemMessageText);
    if (matcher.matches ())
    {
      System.out.print ("Date is: " + matcher.group (1));     // mm/dd/yyyy
      System.out.println (" Time is: " + matcher.group (3));  // hh:mm:ss
      return;
    }
  }

  private void checkProfileMessage (String profileMessageText1,
      String profileMessageText2)
  {
    int pos1 = profileMessageText1.indexOf ("CHAR(");
    int pos2 = profileMessageText1.indexOf ("LINE(");
    int pos3 = profileMessageText1.indexOf ("PREFIX(");

    if (pos1 >= 0 && pos2 >= 0 && pos3 >= 0)
    {
      profile = new Profile (profileMessageText1, profileMessageText2);
      Platform.runLater ( () -> profile.showAndWait ());
    }
  }

  private void checkConsoleOutput (List<Order> orders)
  {
    String text = Dm3270Utility.getString (orders.get (2).getBuffer ());
    if (text.startsWith ("  IEA371I "))
    {
      consoleLogListener.addScreenText (text);
      isConsole = true;
      screen.setIsConsole (true);
    }
  }

  private void checkConsole2Output (List<Order> orders)
  {
    // collect text orders
    List<String> lines = new ArrayList<> (20);
    for (Order order : orders)
      if (order.isText ())
      {
        String line = ((TextOrder) order).getTextString ();
        if (line.length () == 79)
          lines.add (line);
      }

    // remove trailing blank lines
    for (int i = lines.size () - 1; i >= 0; i--)
    {
      String line = lines.get (i);
      if (!line.trim ().isEmpty ())
        break;
      lines.remove (i);
    }

    consoleLog2.addLines (lines);
  }

  public MenuItem getConsoleMenuItem ()
  {
    return menuItem;
  }
}