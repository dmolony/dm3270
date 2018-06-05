package com.bytezone.dm3270.display;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Cursor {

  private final Screen screen;

  private int currentPosition;
  private Field currentField;
  private boolean visible = false;    // this should match the keyboard locked status
  private final Set<FieldChangeListener> fieldChangeListeners = new HashSet<>();

  private final Set<CursorMoveListener> cursorMoveListeners = new HashSet<>();

  public Cursor(Screen screen) {
    this.screen = screen;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
    if (visible) {
      setCurrentField();
      notifyCursorMove(0, currentPosition, currentField);
    } else {
      resetCurrentField();
    }
  }

  public boolean isVisible() {
    return visible;
  }

  public Field getCurrentField() {
    if (currentField == null) {
      setCurrentField();
    }
    return currentField;
  }

  public int getLocation() {
    return currentPosition;
  }

  // ---------------------------------------------------------------------------------//
  // Update screen contents
  // ---------------------------------------------------------------------------------//

  public void typeChar(byte value) {
    if (currentField != null && currentField.isUnprotected()
        && currentField.getCursorOffset() > 0) {
      if (screen.isInsertMode()) {
        int start = currentField.getCursorOffset();
        int end = currentField.getDisplayLength();

        // don't lose data from the end of the field
        byte lastByte = currentField.getByteAt(end);
        if (lastByte != 0x00 && lastByte != 0x40) {
          return;
        }

        currentField.push(start, end);
      }

      screen.getScreenPosition(currentPosition).setChar(value);
      currentField.setModified(true);

      int newPosition = screen.validate(currentPosition + 1);
      if (!currentField.contains(newPosition)) {
        newPosition = currentField.getNextUnprotectedField().getFirstLocation();
      }

      moveTo(newPosition);
    }
  }

  // ---------------------------------------------------------------------------------//
  // Cursor movement
  // ---------------------------------------------------------------------------------//

  public void moveTo(int newPosition) {
    int oldPosition = currentPosition;
    currentPosition = screen.validate(newPosition);

    if (currentPosition != oldPosition) {
      notifyCursorMove(oldPosition, currentPosition, currentField);

      if (currentField != null && !currentField.contains(currentPosition)) {
        setCurrentField();
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // Update currentField
  // ---------------------------------------------------------------------------------//

  private void resetCurrentField() {
    Field lastField = currentField;
    currentField = null;
    if (null != lastField) {
      notifyFieldChange(lastField, null);
    }
  }

  private void setCurrentField() {
    Field lastField = currentField;
    Optional<Field> field = screen.getFieldManager().getFieldAt(currentPosition);
    if (field.isPresent()) {
      currentField = field.get();
      if (currentField != lastField) {
        notifyFieldChange(lastField, currentField);
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  // FieldChangeListener
  // ---------------------------------------------------------------------------------//

  private void notifyFieldChange(Field oldField, Field currentField) {
    fieldChangeListeners.forEach(l -> l.fieldChanged(oldField, currentField));
  }

  public void addFieldChangeListener(FieldChangeListener listener) {
    fieldChangeListeners.add(listener);
  }

  // ---------------------------------------------------------------------------------//
  // CursorMoveListener
  // ---------------------------------------------------------------------------------//

  private void notifyCursorMove(int oldLocation, int currentLocation, Field currentField) {
    cursorMoveListeners
        .forEach(l -> l.cursorMoved(oldLocation, currentLocation, currentField));
  }

  public void addCursorMoveListener(CursorMoveListener listener) {
    cursorMoveListeners.add(listener);
  }

  public void removeCursorMoveListener(CursorMoveListener listener) {
    cursorMoveListeners.remove(listener);
  }

}
