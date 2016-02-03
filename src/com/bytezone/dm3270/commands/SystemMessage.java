package com.bytezone.dm3270.commands;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.assistant.BatchJobListener;
import com.bytezone.dm3270.console.ConsoleLog;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.orders.TextOrder;
import com.bytezone.dm3270.utilities.Dm3270Utility;

import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.scene.text.Font;

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
  private ConsoleLog consoleLog;

  private MenuItem menuItem;
  private int lastOrdersSize;

  private final String[] tempLines = new String[20];
  private String previousMessage = "";
  private int previousTotLines = 0;

  private ConsoleMode consoleMode;
  int screenWidth;

  enum ConsoleMode
  {
    IPL, CONSOLE
  }

  public SystemMessage (Screen screen, BatchJobListener batchJobListener,
      ScreenDimensions screenDimensions)
  {
    this.screen = screen;
    this.batchJobListener = batchJobListener;
    screenWidth = screenDimensions.columns;
  }

  public void setScreenDimensions (ScreenDimensions screenDimensions)
  {
    screenWidth = screenDimensions.columns;
  }

  public ConsoleLog getConsoleLog ()
  {
    return consoleLog;
  }

  void checkSystemMessage (boolean eraseWrite, List<Order> orders, int length)
  {
    if (orders.size () == 0)
    {
      if (consoleMode == ConsoleMode.IPL)
        consoleMode = ConsoleMode.CONSOLE;
      return;
    }

    if (isConsole)
    {
      switch (consoleMode)
      {
        case IPL:
          if (orders.size () == 3 && checkOrders (consoleMessage, orders))
          {
            addConsoleMessage (orders);
            return;
          }
          break;

        case CONSOLE:
          if (length == 1766)
          {
            addConsole2Message (orders);
            return;
          }
          break;
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
    String message = Dm3270Utility.getString (orders.get (2).getBuffer ());
    if (message.length () == 1600 && message.startsWith ("  IEA371I "))
    {
      int pos = message.indexOf (" SELECTED FOR IPL ");
      if (pos >= 0)
      {
        Font displayFont = Font.font ("Monospaced", 13);
        consoleLog = new ConsoleLog (displayFont);
        isConsole = true;
        consoleMode = ConsoleMode.IPL;
        screen.setIsConsole ();
        addConsoleMessage (message);
      }
      else
      {
        System.out.println ("Couldn't find IPL string in:");
        System.out.println (message);
      }
    }
  }

  private void addConsoleMessage (List<Order> orders)
  {
    addConsoleMessage (Dm3270Utility.getString (orders.get (2).getBuffer ()));
  }

  private void addConsoleMessage (String message)
  {
    // break message up into screenWidth lines
    int totLines = splitMessage (message, screenWidth);

    // remove any whole trailing blank lines from original message
    message = message.substring (0, totLines * screenWidth);

    // calculate first line that has not already been processed
    int firstUnprocessedLine = 0;
    if (!previousMessage.isEmpty ())
      for (int ptr = 0; ptr < previousMessage.length (); ptr += screenWidth)
      {
        String chunk = previousMessage.substring (ptr);
        if (message.startsWith (chunk))
        {
          firstUnprocessedLine = chunk.length () / screenWidth;
          break;
        }
      }

    // pass only new lines to the console log
    consoleLog.addLines1 (tempLines, firstUnprocessedLine, totLines);

    // save processed message to compare with the next one
    previousMessage = message;
  }

  private void addConsole2Message (List<Order> orders)
  {
    // collect screen lines into screenWidth strings
    int skipLines = -1;
    int totLines = 0;
    if (screenWidth != 80)
      System.out.println ("fix this");

    for (Order order : orders)
      if (order.isText ())
      {
        String line = ((TextOrder) order).getTextString ();
        if (line.length () == 79 || line.length () == 75)
        {
          String prefix = line.substring (1, 3);
          if (twoDigits.matcher (prefix).matches ())      // new data starts here
            skipLines = totLines;
          tempLines[totLines++] = line;
        }
      }

    // remove any trailing blank lines
    while (totLines > 0)
      if (tempLines[totLines - 1].trim ().isEmpty ())
        --totLines;
      else
        break;

    // skip any previously processed lines
    if (skipLines < 0)
    {
      skipLines = previousTotLines;
      previousTotLines = totLines;
    }

    // pass only new lines to the console log
    consoleLog.addLines2 (tempLines, skipLines, totLines);
  }

  private int splitMessage (String message, int lineLength)
  {
    int totLines = 0;
    for (int ptr = 0; ptr < message.length (); ptr += lineLength)
    {
      int max = Math.min (ptr + lineLength, message.length ());
      String line = message.substring (ptr, max);
      if (line.trim ().isEmpty ())
        break;
      tempLines[totLines++] = line;
    }
    return totLines;
  }

  public MenuItem getConsoleMenuItem ()
  {
    return menuItem;
  }
}