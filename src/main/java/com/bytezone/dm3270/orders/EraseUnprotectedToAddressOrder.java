package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.display.DisplayScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EraseUnprotectedToAddressOrder extends Order {

  private static final Logger LOG = LoggerFactory.getLogger(EraseUnprotectedToAddressOrder.class);

  private final BufferAddress stopAddress;

  public EraseUnprotectedToAddressOrder(byte[] buffer, int offset) {
    assert buffer[offset] == Order.ERASE_UNPROTECTED;
    stopAddress = new BufferAddress(buffer[offset + 1], buffer[offset + 2]);

    this.buffer = new byte[3];
    System.arraycopy(buffer, offset, this.buffer, 0, this.buffer.length);
  }

  @Override
  public void process(DisplayScreen screen) {
    LOG.warn("EraseUnprotectedToAddress not finished");
  }

  @Override
  public String toString() {
    return "EUA : " + stopAddress;
  }

}
