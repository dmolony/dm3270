package com.bytezone.dm3270.display;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FieldManager {

  private final Screen screen;
  private ScreenWatcher screenWatcher;
  private ScreenDimensions screenDimensions;

  private final List<Field> fields = new ArrayList<>();
  private final List<Field> unprotectedFields = new ArrayList<>();

  private final Set<ScreenChangeListener> screenChangeListeners = ConcurrentHashMap.newKeySet();

  public FieldManager(Screen screen, ScreenDimensions screenDimensions) {
    this.screen = screen;
    this.screenDimensions = screenDimensions;
    screenWatcher = new ScreenWatcher(this, screenDimensions);
  }

  public void setScreenDimensions(ScreenDimensions screenDimensions) {
    this.screenDimensions = screen.getScreenDimensions();
    screenWatcher = new ScreenWatcher(this, screenDimensions);
  }

  public void reset() {
    fields.clear();
    unprotectedFields.clear();
  }

  // this is called after the pen and screen positions have been modified
  public void buildFields(ScreenPosition[] screenPositions) {
    reset();

    for (List<ScreenPosition> protoField : divide(screenPositions)) {
      addField(new Field(screen, protoField));
      setContexts(protoField);
    }

    // link uprotected fields
    Field previousUnprotectedField = null;

    for (Field field : fields) {
      if (field.isUnprotected()) {
        unprotectedFields.add(field);
        if (previousUnprotectedField != null) {
          previousUnprotectedField.linkToNext(field);
        }
        previousUnprotectedField = field;
      }
    }

    if (unprotectedFields.size() > 0) {
      // link first unprotected field to the last one
      Field firstField = unprotectedFields.get(0);
      Field lastField = unprotectedFields.get(unprotectedFields.size() - 1);
      lastField.linkToNext(firstField);

      // link protected fields to unprotected fields
      Field next = firstField;

      for (Field field : fields) {
        if (field.isProtected()) {
          field.setNext(next);
        } else {
          next = field.getNextUnprotectedField();
        }
      }
    }

    screenWatcher.check();
    fireScreenChanged(screenWatcher);
  }

  private void addField(Field field) {
    fields.add(field);
  }

  // should this be indexed?
  public Optional<Field> getFieldAt(int position) {
    return fields.parallelStream().filter(f -> f.contains(position)).findAny();
  }

  public List<Field> getUnprotectedFields() {
    return unprotectedFields;
  }

  public List<Field> getFields() {
    return fields;
  }

  public int size() {
    return fields.size();
  }

  public Optional<Field> eraseAllUnprotected() {
    unprotectedFields.parallelStream().forEach(f -> f.clearData(true));

    return unprotectedFields.stream().findFirst();
  }

  // ---------------------------------------------------------------------------------//
  // Field utilities
  // ---------------------------------------------------------------------------------//

  public List<Field> getRowFields(int requestedRow) {
    int firstLocation = requestedRow * screenDimensions.columns;
    int lastLocation = firstLocation + screenDimensions.columns - 1;
    return getFieldsInRange(firstLocation, lastLocation);
  }

  public List<Field> getRowFields(int requestedRowFrom, int rows) {
    int firstLocation = requestedRowFrom * screenDimensions.columns;
    int lastLocation = (requestedRowFrom + rows) * screenDimensions.columns - 1;
    return getFieldsInRange(firstLocation, lastLocation);
  }

  private List<Field> getFieldsInRange(int firstLocation, int lastLocation) {
    List<Field> rowFields = new ArrayList<>();
    for (Field field : fields) {
      int location = field.getFirstLocation();
      if (location < firstLocation) {
        continue;
      }
      if (location > lastLocation) {
        break;
      }
      if (field.getDisplayLength() > 0) {
        rowFields.add(field);
      }
    }
    return rowFields;
  }

  public boolean textMatches(int fieldNo, String text) {
    return text.equals(fields.get(fieldNo).getText());
  }

  public boolean textMatches(int fieldNo, String text, int location) {
    Field field = fields.get(fieldNo);
    return field.getFirstLocation() == location && text.equals(field.getText());
  }

  public boolean textMatchesTrim(Field field, String text) {
    return text.equals(field.getText().trim());
  }

  public List<String> getMenus() {
    List<String> menus = new ArrayList<>();

    for (Field field : fields) {
      if (field.getFirstLocation() >= screenDimensions.columns) {
        break;
      }

      if (field.isProtected() && field.isVisible() && field.getDisplayLength() > 1) {
        String text = field.getText().trim();
        if (!text.isEmpty()) {
          menus.add(text);
        }
      }
    }

    return menus;
  }

  // ---------------------------------------------------------------------------------//
  // ScreenChangeListeners
  // ---------------------------------------------------------------------------------//

  private void fireScreenChanged(ScreenWatcher screenWatcher) {
    screenChangeListeners.forEach(listener -> listener.screenChanged(screenWatcher));
  }

  public void addScreenChangeListener(ScreenChangeListener listener) {
    screenChangeListeners.add(listener);
  }

  public void removeScreenChangeListener(ScreenChangeListener listener) {
    screenChangeListeners.remove(listener);
  }

  // ---------------------------------------------------------------------------------//
  // Divide the ScreenPositions into fields
  // ---------------------------------------------------------------------------------//

  private static List<List<ScreenPosition>> divide(ScreenPosition[] screenPositions) {
    List<List<ScreenPosition>> components = new ArrayList<>();
    List<ScreenPosition> positions = new ArrayList<>();

    int start = -1;
    int first = -1;
    int ptr = 0;

    // not wrapped around to the first field yet
    while (ptr != first) {
      ScreenPosition screenPosition = screenPositions[ptr];

      // check for the start of a new field
      if (screenPosition.isStartField()) {
        // if there is a field to add
        if (start >= 0) {
          components.add(new ArrayList<>(positions));
          positions.clear();
        } else {
          first = ptr;                      // this is the first field on the screen
        }

        start = ptr;                        // beginning of the current field
      }

      // if we are in a field...
      if (start >= 0) {
        positions.add(screenPosition);     // collect next field's positions
      }

      // increment ptr and wrap around
      // faster than validate()
      if (++ptr == screenPositions.length) {
        ptr = 0;
        if (first == -1) {
          break;                            // wrapped around and still no fields
        }
      }
    }

    if (start >= 0 && positions.size() > 0) {
      components.add(new ArrayList<>(positions));
    }

    return components;
  }

  // ---------------------------------------------------------------------------------//
  // Process a field's ScreenPositions
  // ---------------------------------------------------------------------------------//

  private void setContexts(List<ScreenPosition> positions) {
    StartFieldAttribute startFieldAttribute = positions.get(0).getStartFieldAttribute();
    ScreenContext defaultContext = startFieldAttribute.process(null, null);

    if (startFieldAttribute.isExtended()) {
      setExtendedContext(defaultContext, positions);
    } else {
      positions.forEach(sp -> sp.setScreenContext(defaultContext));
    }

  }

  private void setExtendedContext(ScreenContext defaultContext,
      List<ScreenPosition> positions) {
    boolean first = true;
    ScreenContext currentContext = defaultContext;

    for (ScreenPosition screenPosition : positions) {
      for (Attribute attribute : screenPosition.getAttributes()) {
        currentContext =
            attribute.process(defaultContext, currentContext);
      }

      if (first) {
        first = false;
        defaultContext = currentContext;
      }
      screenPosition.setScreenContext(currentContext);
    }
  }

}
