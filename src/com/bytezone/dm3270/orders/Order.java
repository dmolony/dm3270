package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.display.Screen;

public abstract class Order
{
  public final static byte PROGRAM_TAB = 0x05;
  public final static byte GRAPHICS_ESCAPE = 0x08;
  public final static byte SET_BUFFER_ADDRESS = 0x11;
  public final static byte ERASE_UNPROTECTED = 0x12;
  public final static byte INSERT_CURSOR = 0x13;
  public final static byte START_FIELD = 0x1D;
  public final static byte SET_ATTRIBUTE = 0x28;
  public final static byte START_FIELD_EXTENDED = 0x29;
  public final static byte MODIFY_FIELD = 0x2C;
  public final static byte REPEAT_TO_ADDRESS = 0x3C;

  protected boolean rejected;

  protected byte[] buffer;
  protected int duplicates;

  protected boolean oldWay = true;

  public static Order getOrder (byte[] buffer, int ptr, int max)
  {
    switch (buffer[ptr])
    {
      case Order.START_FIELD:
        return new StartFieldOrder (buffer, ptr);

      case Order.START_FIELD_EXTENDED:
        return new StartFieldExtendedOrder (buffer, ptr);

      case Order.SET_BUFFER_ADDRESS:
        return new SetBufferAddressOrder (buffer, ptr);

      case Order.SET_ATTRIBUTE:
        return new SetAttributeOrder (buffer, ptr);

      case Order.MODIFY_FIELD:
        return new ModifyFieldOrder (buffer, ptr);

      case Order.INSERT_CURSOR:
        return new InsertCursorOrder (buffer, ptr);

      case Order.PROGRAM_TAB:
        return new ProgramTabOrder (buffer, ptr);

      case Order.REPEAT_TO_ADDRESS:
        return new RepeatToAddressOrder (buffer, ptr);

      case Order.ERASE_UNPROTECTED:
        return new EraseUnprotectedToAddressOrder (buffer, ptr);

      case Order.GRAPHICS_ESCAPE:
        return new GraphicsEscapeOrder (buffer, ptr);

      default:
        return new TextOrder (buffer, ptr, max);
    }
  }

  public boolean rejected ()
  {
    return rejected;
  }

  public void incrementDuplicates ()
  {
    duplicates++;
  }

  public byte getType ()
  {
    return buffer[0];
  }

  // this is so that a GraphicsEscapeOrder can override it - it is used to report
  // that there are x duplicate orders.
  public boolean matches (Order order)
  {
    return false;
  }

  public int size ()
  {
    return buffer.length;
  }

  public byte[] getBuffer ()
  {
    return buffer;
  }

  public int pack (byte[] buffer, int offset)
  {
    int ptr = offset;
    System.arraycopy (this.buffer, 0, buffer, ptr, this.buffer.length);
    ptr += this.buffer.length;
    return ptr;
  }

  public abstract void process (Screen screen);
}