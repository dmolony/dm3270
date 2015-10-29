package com.bytezone.dm3270.extended;

import com.bytezone.dm3270.buffers.AbstractReplyBuffer;
import com.bytezone.dm3270.display.Screen;

public abstract class AbstractExtendedCommand extends AbstractReplyBuffer
{
  protected final CommandHeader commandHeader;

  public AbstractExtendedCommand (CommandHeader commandHeader)
  {
    this.commandHeader = commandHeader;
  }

  public AbstractExtendedCommand (CommandHeader commandHeader, byte[] buffer, int offset,
      int length)
  {
    super (buffer, offset, length);
    this.commandHeader = commandHeader;
  }

  @Override
  public byte[] getData ()
  {
    byte[] buffer = new byte[data.length + 5];
    System.arraycopy (commandHeader.getData (), 0, buffer, 0, 5);
    System.arraycopy (data, 0, buffer, 5, data.length);
    return buffer;
  }

  @Override
  public int size ()
  {
    return data.length + 5;
  }

  @Override
  public byte[] getTelnetData ()
  {
    byte[] data = getData ();                       // prepend the command header
    int length = data.length + countFF (data) + 2;  // add in expanded 0xFF and IAC/EOR
    byte[] buffer = new byte[length];
    copyAndExpand (data, buffer, 0);
    buffer[--length] = (byte) 0xEF;                 // EOR
    buffer[--length] = (byte) 0xFF;                 // IAC
    return buffer;
  }

  public abstract String getName ();

  @Override
  public void process (Screen screen)
  {
    commandHeader.process (screen);
  }

  public CommandHeader getCommandHeader ()
  {
    return commandHeader;
  }
}