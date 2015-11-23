package com.bytezone.dm3270.extended;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bytezone.dm3270.buffers.Buffer;
import com.bytezone.dm3270.buffers.MultiBuffer;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.display.Screen;

public class TN3270ExtendedCommand extends AbstractExtendedCommand
{
  private final Command command;

  public TN3270ExtendedCommand (CommandHeader commandHeader, Command command)
  {
    super (commandHeader);
    this.command = command;
  }

  public Command getCommand ()
  {
    return command;
  }

  @Override
  public byte[] getData ()
  {
    byte[] combinedData = new byte[command.size () + 5];
    System.arraycopy (commandHeader.getData (), 0, combinedData, 0,
                      commandHeader.size ());
    System.arraycopy (command.getData (), 0, combinedData, commandHeader.size (),
                      command.size ());
    return combinedData;
  }

  @Override
  public int size ()
  {
    return command.size () + commandHeader.size ();
  }

  @Override
  public void process (Screen screen)
  {
    commandHeader.process (screen);
    command.process (screen);
  }

  @Override
  public Optional<Buffer> getReply ()
  {
    List<Buffer> buffers = new ArrayList<> ();

    Optional<Buffer> headerReply = commandHeader.getReply ();
    if (headerReply.isPresent ())
      buffers.add (headerReply.get ());

    // need to add a header for the command reply before the reply
    Optional<Buffer> reply = command.getReply ();
    if (reply.isPresent ())
    {
      byte[] headerBuffer = new byte[5];
      byte[] commandHeaderBuffer = commandHeader.getData ();
      headerBuffer[3] = commandHeaderBuffer[3];
      headerBuffer[4] = commandHeaderBuffer[4];
      CommandHeader header = new CommandHeader (headerBuffer);
      TN3270ExtendedCommand command =
          new TN3270ExtendedCommand (header, (Command) reply.get ());
      buffers.add (command);
    }

    if (buffers.size () == 0)
      return Optional.empty ();

    if (buffers.size () == 1)
      return Optional.of (buffers.get (0));

    MultiBuffer multiBuffer = new MultiBuffer ();
    for (Buffer buffer : buffers)
      multiBuffer.addBuffer (buffer);
    return Optional.of (multiBuffer);
  }

  @Override
  public byte[] getTelnetData ()
  {
    byte[] headerTelnetBuffer = commandHeader.getTelnetData ();
    byte[] commandTelnetBuffer = command.getTelnetData ();

    int length = headerTelnetBuffer.length + commandTelnetBuffer.length;
    byte[] returnBuffer = new byte[length];
    System.arraycopy (headerTelnetBuffer, 0, returnBuffer, 0, headerTelnetBuffer.length);
    System.arraycopy (commandTelnetBuffer, 0, returnBuffer, headerTelnetBuffer.length,
                      commandTelnetBuffer.length);

    return returnBuffer;
  }

  @Override
  public String getName ()
  {
    return command.getName ();
  }

  @Override
  public String toString ()
  {
    return command.toString ();
  }
}