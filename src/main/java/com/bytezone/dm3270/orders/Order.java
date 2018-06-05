package com.bytezone.dm3270.orders;

import com.bytezone.dm3270.display.DisplayScreen;

public abstract class Order {

  // Buffer Control Orders
  public static final byte PROGRAM_TAB = 0x05;
  public static final byte GRAPHICS_ESCAPE = 0x08;
  public static final byte SET_BUFFER_ADDRESS = 0x11;
  public static final byte ERASE_UNPROTECTED = 0x12;
  public static final byte INSERT_CURSOR = 0x13;
  public static final byte START_FIELD = 0x1D;
  public static final byte SET_ATTRIBUTE = 0x28;
  public static final byte START_FIELD_EXTENDED = 0x29;
  public static final byte MODIFY_FIELD = 0x2C;
  public static final byte REPEAT_TO_ADDRESS = 0x3C;

  // Format Control Orders
  public static final byte FCO_NULL = 0x00;
  public static final byte FCO_SUBSTITUTE = 0x3F;
  public static final byte FCO_DUPLICATE = 0x1C;
  public static final byte FCO_FIELD_MARK = 0x1E;
  public static final byte FCO_FORM_FEED = 0x0C;
  public static final byte FCO_CARRIAGE_RETURN = 0x0D;
  public static final byte FCO_NEWLINE = 0x15;
  public static final byte FCO_END_OF_MEDIUM = 0x19;
  public static final byte FCO_EIGHT_ONES = (byte) 0xFF;

  public static byte[] orderValues =
      {START_FIELD, START_FIELD_EXTENDED, SET_BUFFER_ADDRESS, INSERT_CURSOR,
          GRAPHICS_ESCAPE, REPEAT_TO_ADDRESS, ERASE_UNPROTECTED, PROGRAM_TAB, SET_ATTRIBUTE,
          MODIFY_FIELD, FCO_NULL, FCO_SUBSTITUTE, FCO_DUPLICATE, FCO_FIELD_MARK,
          FCO_FORM_FEED, FCO_CARRIAGE_RETURN, FCO_NEWLINE, FCO_END_OF_MEDIUM,
          FCO_EIGHT_ONES};

  protected byte[] buffer;
  protected int duplicates;

  public static Order getOrder(byte[] buffer, int ptr, int max) {
    switch (buffer[ptr]) {
      case START_FIELD:
        return new StartFieldOrder(buffer, ptr);

      case START_FIELD_EXTENDED:
        return new StartFieldExtendedOrder(buffer, ptr);

      case SET_BUFFER_ADDRESS:
        return new SetBufferAddressOrder(buffer, ptr);

      case SET_ATTRIBUTE:
        return new SetAttributeOrder(buffer, ptr);

      case MODIFY_FIELD:
        return new ModifyFieldOrder(buffer, ptr);

      case INSERT_CURSOR:
        return new InsertCursorOrder(buffer, ptr);

      case PROGRAM_TAB:
        return new ProgramTabOrder(buffer, ptr);

      case REPEAT_TO_ADDRESS:
        return new RepeatToAddressOrder(buffer, ptr);

      case ERASE_UNPROTECTED:
        return new EraseUnprotectedToAddressOrder(buffer, ptr);

      case GRAPHICS_ESCAPE:
        return new GraphicsEscapeOrder(buffer, ptr);

      case FCO_NULL:
      case FCO_SUBSTITUTE:
      case FCO_DUPLICATE:
      case FCO_FIELD_MARK:
      case FCO_FORM_FEED:
      case FCO_CARRIAGE_RETURN:
      case FCO_NEWLINE:
      case FCO_END_OF_MEDIUM:
      case FCO_EIGHT_ONES:
        return new FormatControlOrder(buffer, ptr);

      default:
        return new TextOrder(buffer, ptr, max);
    }
  }

  public void incrementDuplicates() {
    duplicates++;
  }

  public byte getType() {
    return buffer[0];
  }

  public boolean isText() {
    return false;
  }

  // this is so that a GraphicsEscapeOrder can override it - it is used to report
  // that there are x duplicate orders.
  public boolean matchesPreviousOrder(Order order) {
    return false;
  }

  public int size() {
    return buffer.length;
  }

  public byte[] getBuffer() {
    return buffer;
  }

  public abstract void process(DisplayScreen screen);

}
