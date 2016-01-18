package com.bytezone.dm3270.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.assistant.BatchJobListener;
import com.bytezone.dm3270.console.ConsoleLog1;
import com.bytezone.dm3270.console.ConsoleLog2;
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

  private static final Pattern twoDigits = Pattern.compile ("\\d\\d");

  private final Screen screen;
  private final BatchJobListener batchJobListener;
  private Profile profile;
  private boolean isConsole;
  private ConsoleLog1 consoleLog1;
  private ConsoleLog2 consoleLog2;
  private MenuItem menuItem;
  private int lastOrdersSize;

  private final List<String> lines = new ArrayList<> (20);
  private final List<String> newLines = new ArrayList<> (20);

  public SystemMessage (Screen screen, BatchJobListener batchJobListener)
  {
    this.screen = screen;
    this.batchJobListener = batchJobListener;
  }

  public ConsoleLog1 getConsoleLog1 ()
  {
    return consoleLog1;
  }

  public ConsoleLog2 getConsoleLog2 ()
  {
    return consoleLog2;
  }

  void checkSystemMessage (boolean eraseWrite, List<Order> orders)
  {
    if (isConsole)
    {
      if (orders.size () == 3)
      {
        if (checkOrders (consoleMessage, orders))
        {
          String text = Dm3270Utility.getString (orders.get (2).getBuffer ());
          consoleLog1.addLines (text);
          return;
        }
      }
      //      else if (orders.size () == 2)
      //      {
      //        //        System.out.println ("two");
      //      }
      //      else
      //      {
      checkConsole2Output (orders);
      //      }
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
    if (text.length () == 1600 && text.startsWith ("  IEA371I "))
    {
      int pos = text.indexOf (" SELECTED FOR IPL ");
      if (pos >= 0)
      {
        consoleLog1 = new ConsoleLog1 ();
        consoleLog2 = new ConsoleLog2 ();
        consoleLog1.addLines (text);
        isConsole = true;
        screen.setIsConsole (true);
      }
      else
      {
        System.out.println ("Couldn't find IPL string");
        System.out.println (text);
      }
    }
  }

  private void checkConsole2Output (List<Order> orders)
  {
    boolean debug = false;
    boolean display = false;

    // collect text orders
    lines.clear ();
    int skipLines = 0;

    if (display)
    {
      System.out.print ("-------------------------------------------");
      System.out.println ("----------------------------------------");
    }

    for (Order order : orders)
      if (order.isText ())
      {
        String line = ((TextOrder) order).getTextString ();
        if (line.length () == 79 || line.length () == 75)
        {
          String prefix = line.substring (1, 3);
          if (twoDigits.matcher (prefix).matches ())
            skipLines = lines.size ();
          lines.add (line);

          if (display)
            System.out.printf ("%02d: %s %s%n", lines.size (), line,
                               line.length () != 79 ? " C" : "");
        }
      }

    if (debug)
      System.out.printf ("skiplines: %d%n", skipLines);

    if (lines.size () == 20)
    {
      // remove trailing blank lines
      for (int i = lines.size () - 1; i >= 0; i--)
      {
        String line = lines.get (i);
        if (!line.trim ().isEmpty ())
          break;
        lines.remove (i);
      }

      if (lines.size () > 0)
        if (skipLines == 0)
          consoleLog2.addLines (lines);
        else
        {
          newLines.clear ();
          for (int i = skipLines; i < lines.size (); i++)
            newLines.add (lines.get (i));
          consoleLog2.addLines (newLines);
        }
    }
    else if (lines.size () > 0)
    {
      System.out.println ("skipping:");
      for (int i = 0; i < lines.size (); i++)
        System.out.printf ("%02d : %s%n", i, lines.get (i));
    }
  }

  public MenuItem getConsoleMenuItem ()
  {
    return menuItem;
  }
}