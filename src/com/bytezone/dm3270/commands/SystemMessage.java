package com.bytezone.dm3270.commands;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.utilities.Utility;

public class SystemMessage
{
  private static final Pattern jobSubmittedPattern =
      Pattern.compile ("^JOB ([A-Z0-9]{1,9})\\(JOB(\\d{5})\\) SUBMITTED");

  private static final Pattern jobCompletedPattern =
      Pattern.compile ("(^\\d\\d(?:\\.\\d\\d){2}) JOB(\\d{5})"
          + " \\$HASP\\d+ ([A-Z0-9]+) .* MAXCC=(\\d+).*");

  private static final Pattern jobFailedPattern =
      Pattern.compile ("(^\\d\\d(?:\\.\\d\\d){2}) JOB(\\d{5})"
          + " \\$HASP\\d+ ([A-Z0-9]+) .* JCL ERROR.*");

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

  private final Screen screen;

  public SystemMessage (Screen screen)
  {
    this.screen = screen;
  }

  void checkSystemMessage (boolean eraseWrite, List<Order> orders)
  {
    if (eraseWrite)
    {
      switch (orders.size ())
      {
        case 8:
          if (checkOrders (systemMessage3, orders))
            checkSystemMessage (Utility.getString (orders.get (4).getBuffer ()));
          return;

        case 11:
          if (checkOrders (systemMessage2, orders))
            checkSystemMessage (Utility.getString (orders.get (4).getBuffer ()));
          return;

        case 15:
          if (checkOrders (systemMessage4, orders))
          {
            checkSystemMessage (Utility.getString (orders.get (4).getBuffer ()));
            checkSystemMessage (Utility.getString (orders.get (8).getBuffer ()));
          }
          return;

        case 17:
          if (checkOrders (profileMessage, orders))
            checkProfileMessage (Utility.getString (orders.get (4).getBuffer ())
                + Utility.getString (orders.get (6).getBuffer ()),
                                 Utility.getString (orders.get (10).getBuffer ()));
          return;
      }
    }
    else
    {
      switch (orders.size ())
      {
        case 6:
          if (checkOrders (systemMessage1, orders))
            checkSystemMessage (Utility.getString (orders.get (2).getBuffer ()));
          return;

        case 9:
          if (checkOrders (systemMessage5, orders))
            checkSystemMessage (Utility.getString (orders.get (2).getBuffer ()));
          return;
      }
    }

    if (orders.size () < 20 && false)
    {
      System.out.printf ("Orders: %d%n", orders.size ());
      System.out.printf ("Erase : %s%n", eraseWrite);
      for (Order order : orders)
        System.out.println (order);
      System.out.println ("-------------------------------");
    }
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
      screen.getAssistantStage ().batchJobSubmitted (jobNumber, jobName);
      return;
    }

    matcher = jobCompletedPattern.matcher (systemMessageText);
    if (matcher.matches ())
    {
      int jobNumber = Integer.parseInt (matcher.group (2));
      String jobName = matcher.group (3);
      String time = matcher.group (1);
      int conditionCode = Integer.parseInt (matcher.group (4));
      screen.getAssistantStage ().batchJobEnded (jobNumber, jobName, time, conditionCode);
      return;
    }

    matcher = jobFailedPattern.matcher (systemMessageText);
    if (matcher.matches ())
    {
      int jobNumber = Integer.parseInt (matcher.group (2));
      String jobName = matcher.group (3);
      String time = matcher.group (1);
      screen.getAssistantStage ().batchJobFailed (jobNumber, jobName, time);
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
      System.out.println ("Time is: " + matcher.group (3));   // hh:mm:ss
      return;
    }
  }

  private void checkProfileMessage (String profileMessageText1,
      String profileMessageText2)
  {
    System.out.println ("Profile tokens:");
    for (String token : profileMessageText1.split ("\\s+"))
    {
      System.out.println (token);
      if (token.startsWith ("PREFIX(") && token.endsWith (")"))
        System.out.printf ("Prefix=%s%n", token.substring (7, token.length () - 1));
    }
  }
}