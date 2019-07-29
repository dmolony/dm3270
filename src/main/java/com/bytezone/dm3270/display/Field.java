package com.bytezone.dm3270.display;

import com.bytezone.dm3270.attributes.StartFieldAttribute;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Field implements Iterable<ScreenPosition> {

  private static final Logger LOG = LoggerFactory.getLogger(Field.class);

  private final Screen screen;

  private final int startPosition;        // position of StartFieldAttribute
  private final int endPosition;          // last data position of this field
  // unprotected fields
  private Field next;

  private final StartFieldAttribute startFieldAttribute;
  private final List<ScreenPosition> screenPositions;
  private final ScreenDimensions screenDimensions;
  
  public Field(Screen screen, List<ScreenPosition> positions) {
    this.screen = screen;
    this.screenDimensions = screen.getScreenDimensions();

    ScreenPosition firstScreenPosition = positions.get(0);
    ScreenPosition lastScreenPosition = positions.get(positions.size() - 1);

    screenPositions = new ArrayList<>(positions);
    startFieldAttribute = firstScreenPosition.getStartFieldAttribute();

    startPosition = firstScreenPosition.getPosition();
    endPosition = lastScreenPosition.getPosition();
  }

  // link two unprotected fields to each other
  public void linkToNext(Field nextField) {
    assert isUnprotected();
    assert nextField.isUnprotected();

    this.next = nextField;
  }

  public void setNext(Field field) {
    this.next = field;
  }

  public Field getNextUnprotectedField() {
    return next;
  }

  public int getDisplayLength() {
    return screenPositions.size() - 1;
  }

  public int getFirstLocation() {
    return screen.validate(startPosition + 1);
  }

  public int getCursorOffset() {
    int cursorLocation = screen.getScreenCursor().getLocation();
    if (cursorLocation >= startPosition) {
      return cursorLocation - startPosition;
    }
    return screenDimensions.size - startPosition + cursorLocation;
  }

  public boolean isHidden() {
    return startFieldAttribute.isHidden();
  }

  public boolean isProtected() {
    return startFieldAttribute.isProtected();
  }

  public boolean isUnprotected() {
    return !startFieldAttribute.isProtected();
  }

  public boolean isModified() {
    return startFieldAttribute.isModified();
  }

  public boolean isVisible() {
    return startFieldAttribute.isVisible();
  }

  public void setModified(boolean modified) {
    startFieldAttribute.setModified(modified);
  }

  public boolean contains(int position) {
    if (startPosition <= endPosition) {
      return position >= startPosition && position <= endPosition;
    }
    return position >= startPosition || position <= endPosition;
  }

  public void erase() {
    for (int i = 1; i < screenPositions.size(); i++) {
      screenPositions.get(i).setChar((byte) 0);
    }
    setModified(true);
  }

  public void clearData(boolean alterModifiedFlag) {
    // don't reset any already set flags
    if (alterModifiedFlag) {
      setModified(true);
    }

    for (int i = 1; i < screenPositions.size(); i++) {
      screenPositions.get(i).setChar((byte) 0);         // leave screenContext
    }
  }

  // overwrites each position with the position to its left (insert)
  // called from Cursor.typeChar()
  public void push(int first, int last) {
    ScreenPosition spLast = screenPositions.get(last);
    while (first < last) {
      ScreenPosition sp = screenPositions.get(--last);
      spLast.setChar(sp.getByte());
      spLast.setScreenContext(sp.getScreenContext());
      spLast = sp;
    }
  }

  public byte getByteAt(int position) {
    return screenPositions.get(position).getByte();
  }

  public String getText() {
    if (startPosition == endPosition) {
      return "";
    }

    char[] buffer = new char[getDisplayLength()];
    int ptr = 0;

    for (ScreenPosition screenPosition : screenPositions) {
      // skip the start field attribute
      if (!screenPosition.isStartField()) {
        if (ptr < buffer.length) {
          buffer[ptr++] = screenPosition.getChar();
        } else {
          LOG.warn("Too long: {}", ptr);
        }
      }
    }

    return new String(buffer);
  }

  public void setText(String text) {
    try {
      erase();                                     // sets the field to modified
      setText(text.getBytes(screen.getCharset().name()));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  public void setText(byte[] buffer) {
    int ptr = 1;
    for (byte b : buffer) {
      if (ptr < screenPositions.size()) {
        screenPositions.get(ptr++).setChar(b);
      } else {
        LOG.warn("Buffer overrun");
        break;
      }
    }
  }

  @Override
  public String toString() {
    return String.format("%04d-%04d %s [%s]", startPosition, endPosition,
        startFieldAttribute.getAcronym(), getText());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Field that = (Field) o;
    return startPosition == that.startPosition &&
        endPosition == that.endPosition &&
        startFieldAttribute.getAcronym().equals(that.startFieldAttribute.getAcronym()) && 
        getText().equals(that.getText());
  }

  @Override
  public int hashCode() {
    return Objects.hash(startPosition, endPosition, startFieldAttribute.getAcronym(), getText());
  }

  @Override
  public Iterator<ScreenPosition> iterator() {
    return screenPositions.iterator();
  }

}
