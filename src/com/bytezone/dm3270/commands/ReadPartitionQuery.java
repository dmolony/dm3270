package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.display.Screen;

public class ReadPartitionQuery extends Command
{

  public ReadPartitionQuery (byte[] buffer, int offset, int length)
  {
    super (buffer, offset, length);
  }

  @Override
  public void process (Screen screen)
  {
    if (reply != null)
      return;

    switch (data[2])
    {
      case (byte) 0x02:
        reply = new ReadStructuredFieldCommand ();      // build a QueryReply
        break;

      case (byte) 0x03:
        switch (data[3])
        {
          case 0:
            System.out.println ("QCode List not written yet");
            break;

          case 1:
            System.out.println ("Equivalent + QCode List not written yet");
            break;

          case 2:
            reply = new ReadStructuredFieldCommand ();      // build a QueryReply
            break;

          default:
            System.out.printf ("Unknown query type: %02X%n", data[3]);
        }
        break;

      default:
        System.out.printf ("Unknown ReadStructuredField type: %02X%n", data[2]);
    }
  }

  @Override
  public String getName ()
  {
    return "ReadPartitionQuery";
  }
}