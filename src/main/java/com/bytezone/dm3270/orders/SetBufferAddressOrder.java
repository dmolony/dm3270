package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.display.DisplayScreen;
import com.bytezone.dm3270.display.Pen;

public class SetBufferAddressOrder extends Order implements BufferAddressSource {

  private final BufferAddress bufferAddress;

  public SetBufferAddressOrder(byte[] buffer, int offset) {
    assert buffer[offset] == Order.SET_BUFFER_ADDRESS;

    bufferAddress = new BufferAddress(buffer[offset + 1], buffer[offset + 2]);

    this.buffer = new byte[3];
    System.arraycopy(buffer, offset, this.buffer, 0, 3);
  }

  @Override
  public BufferAddress getBufferAddress() {
    return bufferAddress;
  }

  @Override
  public void process(DisplayScreen screen) {
    Pen pen = screen.getPen();
    pen.moveTo(bufferAddress.getLocation());
  }

  @Override
  public String toString() {
    return String.format("SBA : %s", bufferAddress);
  }

}
