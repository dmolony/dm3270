package com.bytezone.dm3270.commands;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.assistant.BatchJobListener;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.orders.Order;

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
    addBatchJobListener (screen.getAssistantStage ());// this is clumsy

    if (eraseWrite && orders.size () == 8)
    {
      if (checkOrders (systemMessage3, orders))
        checkSystemMessage (Utility.getString (orders.get (4).getBuffer ()));
      return;
    }

    if (eraseWrite && orders.size () == 11)
    {
      if (checkOrders (systemMessage2, orders))
        checkSystemMessage (Utility.getString (orders.get (4).getBuffer ()));
      return;
    }

    if (eraseWrite && orders.size () == 15)
    {
      if (checkOrders (systemMessage4, orders))
      {
        checkSystemMessage (Utility.getString (orders.get (4).getBuffer ()));
        checkSystemMessage (Utility.getString (orders.get (8).getBuffer ()));
      }
      return;
    }

    if (!eraseWrite && orders.size () == 6)
    {
      if (checkOrders (systemMessage1, orders))
        checkSystemMessage (Utility.getString (orders.get (2).getBuffer ()));
      return;
    }

    if (!eraseWrite && orders.size () == 9)
    {
      if (checkOrders (systemMessage5, orders))
        checkSystemMessage (Utility.getString (orders.get (2).getBuffer ()));
      return;
    }

    if (eraseWrite && orders.size () == 17)
    {
      if (checkOrders (profileMessage, orders))
      {
        checkProfileMessage (Utility.getString (orders.get (4).getBuffer ())
            + Utility.getString (orders.get (6).getBuffer ()),
                             Utility.getString (orders.get (10).getBuffer ()));
      }
    }

    if (orders.size () < 30 && false)
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
      fireBatchJobSubmitted (Integer.parseInt (matcher.group (2)), matcher.group (1));
      return;
    }

    matcher = jobCompletedPattern.matcher (systemMessageText);
    if (matcher.matches ())
    {
      int jobNumber = Integer.parseInt (matcher.group (2));
      int conditionCode = Integer.parseInt (matcher.group (4));
      fireBatchJobEnded (jobNumber, matcher.group (3), matcher.group (1), conditionCode);
      return;
    }

    matcher = jobFailedPattern.matcher (systemMessageText);
    if (matcher.matches ())
    {
      int jobNumber = Integer.parseInt (matcher.group (2));
      String jobName = matcher.group (3);
      String time = matcher.group (1);
      fireBatchJobFailed (jobNumber, jobName, time);
      return;
    }
  }

  private void checkProfileMessage (String profileMessageText1,
      String profileMessageText2)
  {
    System.out.println (profileMessageText1);
    System.out.println (profileMessageText2);
  }

  // ---------------------------------------------------------------------------------//
  // BatchJobListener
  // ---------------------------------------------------------------------------------//

  private final Set<BatchJobListener> batchJobListeners = new HashSet<> ();

  void fireBatchJobSubmitted (int jobNumber, String jobName)
  {
    for (BatchJobListener listener : batchJobListeners)
      listener.batchJobSubmitted (jobNumber, jobName);
  }

  void fireBatchJobEnded (int jobNumber, String jobName, String time, int conditionCode)
  {
    for (BatchJobListener listener : batchJobListeners)
      listener.batchJobEnded (jobNumber, jobName, time, conditionCode);
  }

  void fireBatchJobFailed (int jobNumber, String jobName, String time)
  {
    for (BatchJobListener listener : batchJobListeners)
      listener.batchJobFailed (jobNumber, jobName, time);
  }

  public void addBatchJobListener (BatchJobListener listener)
  {
    batchJobListeners.add (listener);
  }

  public void removeBatchJobListener (BatchJobListener listener)
  {
    batchJobListeners.remove (listener);
  }
}