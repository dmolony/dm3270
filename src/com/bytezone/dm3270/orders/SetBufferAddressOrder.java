package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.application.Cursor;
import com.bytezone.dm3270.application.ScreenHandler;

public class SetBufferAddressOrder extends Order implements BufferAddressSource
{
  private final BufferAddress bufferAddress;

  public SetBufferAddressOrder (byte[] buffer, int offset)
  {
    assert buffer[offset] == Order.SET_BUFFER_ADDRESS;

    bufferAddress = new BufferAddress (buffer[offset + 1], buffer[offset + 2]);

    this.buffer = new byte[3];
    System.arraycopy (buffer, offset, this.buffer, 0, 3);
  }

  public SetBufferAddressOrder (int location)
  {
    buffer = new byte[3];
    buffer[0] = Order.SET_BUFFER_ADDRESS;
    bufferAddress = new BufferAddress (location);
    bufferAddress.packAddress (buffer, 1);
  }

  @Override
  public BufferAddress getBufferAddress ()
  {
    return bufferAddress;
  }

  @Override
  public void process (ScreenHandler screenHandler)
  {
    Cursor cursor = screenHandler.getCursor ();
    cursor.setAddress (bufferAddress);
    //    showText (cursor);
  }

  @Override
  public String toString ()
  {
    return String.format ("SBA : %s", bufferAddress);
  }
}