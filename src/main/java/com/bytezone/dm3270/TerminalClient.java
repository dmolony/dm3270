package com.bytezone.dm3270;

import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.display.ScreenPosition;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.utilities.Site;
import java.awt.Point;
import java.util.List;
import java.util.Optional;
import javax.net.SocketFactory;

/**
 * Client to connect to TN3270 terminal servers.
 * <p>
 * This class provides a facade to ease usage of dm3270 to java clients.
 */
public class TerminalClient {

  private int model = 2;
  private ScreenDimensions screenDimensions = new ScreenDimensions(24, 80);
  private boolean usesExtended3270;
  private Screen screen;
  private ConsolePane consolePane;
  private SocketFactory socketFactory = SocketFactory.getDefault();
  private ExceptionHandler exceptionHandler;
  private int connectionTimeoutMillis;

  /**
   * Sets the model of terminal to emulate.
   *
   * @param model model of the terminal. Known values are 2,3,4 and 5. When not specified the
   * default model 2 will be used.
   */
  public void setModel(int model) {
    this.model = model;
  }

  /**
   * Sets the screen dimensions of the terminal to emulate.
   *
   * @param screenDimensions dimensions in rows and columns. When not specified default dimensions
   * of 24 rows and 80 columns will be used.
   */
  public void setScreenDimensions(ScreenDimensions screenDimensions) {
    this.screenDimensions = screenDimensions;
  }

  /**
   * Sets whether the emulated terminal supports extended protocol or not.
   *
   * @param usesExtended3270 set true to support extended protocol, and false if not. By default is
   * false.
   */
  public void setUsesExtended3270(boolean usesExtended3270) {
    this.usesExtended3270 = usesExtended3270;
  }

  /**
   * Allows setting the {@link SocketFactory} to be used to create sockets which allows using SSL
   * sockets.
   *
   * @param socketFactory the {@link SocketFactory} to use. If non is specified {@link
   * SocketFactory#getDefault()} will be used.
   */
  public void setSocketFactory(SocketFactory socketFactory) {
    this.socketFactory = socketFactory;
  }

  /**
   * Sets the timeout for the socket connection.
   *
   * @param connectionTimeoutMillis Number of millis to wait for a connection to be established
   * before it fails. If not specified no timeout (same as 0 value) will be applied.
   */
  public void setConnectionTimeoutMillis(int connectionTimeoutMillis) {
    this.connectionTimeoutMillis = connectionTimeoutMillis;
  }

  /**
   * Sets a class to handle general exception handler.
   *
   * @param exceptionHandler a class to handle exceptions. If none is provided then exceptions stack
   * trace will be printed to error output.
   */
  public void setExceptionHandler(ExceptionHandler exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
  }

  /**
   * Connect to a terminal server.
   *
   * @param host host name of the terminal server.
   * @param port port where the terminal server is listening for connections.
   */
  public void connect(String host, int port) {
    TelnetState telnetState = new TelnetState();
    telnetState.setDoDeviceType(model);
    screen = new Screen(screenDimensions, null, telnetState);
    screen.lockKeyboard("connect");
    consolePane = new ConsolePane(screen, new Site(host, port, usesExtended3270), socketFactory);
    consolePane.setConnectionTimeoutMillis(connectionTimeoutMillis);
    consolePane.setExceptionHandler(exceptionHandler);
    consolePane.connect();
  }

  /**
   * Set the text of a field in the screen.
   *
   * @param row row number where to set the field text. First row is 1.
   * @param column column number where to set the field text. First column is 1.
   * @param text the text to set on the field.
   */
  public void setFieldTextByCoord(int row, int column, String text) {
    int linearPosition = (row - 1) * screen.getScreenDimensions().columns + column - 1;
    if (screen.getFieldManager().getFields().isEmpty()) {
      screen.setPositionText(linearPosition, text);
    } else {
      Field field = screen.getFieldManager()
          .getFieldAt(linearPosition)
          .orElseThrow(
              () -> new IllegalArgumentException("Invalid field position " + row + "," + column));
      screen.setFieldText(field, text);
    }
    screen.getScreenCursor().moveTo(linearPosition + text.length());
  }

  public void setFieldTextByLabel(String lbl, String text) {
    Field field = findFieldPositionByLabel(lbl);
    if (field == null) {
      throw new IllegalArgumentException("Invalid field label: " + lbl);
    }
    screen.setFieldText(field, text);
    screen.getScreenCursor().moveTo(field.getFirstLocation() + text.length());
  }

  private Field findFieldPositionByLabel(String label) {
    Field labelField = findLabelField(label);
    return (labelField != null) ? labelField.getNextUnprotectedField() : null;
  }

  private Field findLabelField(String label) {
    String screenText = getScreenText();
    int pos = 0;
    Field fallbackLabelField = null;
    while (pos != -1) {
      pos = screenText.indexOf(label, pos);
      if (pos != -1) {
        Field field = screen.getFieldManager().getFieldAt(pos).orElse(null);
        if (field != null) {
          if (field.isProtected()) {
            return field;
          } else {
            if (fallbackLabelField == null) {
              fallbackLabelField = field;
            }
            pos++;
          }
        } else {
          pos++;
        }
      }
    }
    return fallbackLabelField;
  }

  /**
   * Send an Action ID.
   *
   * This method is usually used to send Enter after setting text fields, or to send some other keys
   * (like F1).
   *
   * @param aid Action ID to send. For example Enter.
   * @param name Name of the action sent.
   */
  public void sendAID(byte aid, String name) {
    consolePane.sendAID(aid, name);
  }

  /**
   * Gets the screen text.
   *
   * @return The screen text with newlines separating each row.
   */
  public String getScreenText() {
    StringBuilder text = new StringBuilder();
    int pos = 0;
    boolean visible = true;
    for (ScreenPosition sp : screen.getPen()) {
      if (sp.isStartField()) {
        visible = sp.getStartFieldAttribute().isVisible();
      }
      text.append(visible ? sp.getCharString() : " ");
      if (++pos % screen.getScreenDimensions().columns == 0) {
        text.append("\n");
      }
    }
    return text.toString();
  }

  /**
   * Gets the list of all fields (protected and unprotected) that compose the screen.
   *
   * @return The list of fields that compose the screen. Fields are not only positions where input
   * is expected, but also parts of the screen which are not meant to be modified or even visible.
   */
  public List<Field> getFields() {
    return screen.getFieldManager().getFields();
  }

  /**
   * Adding a {@link ScreenChangeListener} to the terminal emulator.
   *
   * @param listener The listener to be notified when changes on the screen happen.
   */
  public void addScreenChangeListener(ScreenChangeListener listener) {
    screen.getFieldManager().addScreenChangeListener(listener);
  }

  /**
   * Remove a {@link ScreenChangeListener} from the terminal emulator.
   *
   * @param listener Listener to be removed from notifications.
   */
  public void removeScreenChangeListener(ScreenChangeListener listener) {
    screen.getFieldManager().removeScreenChangeListener(listener);
  }

  /**
   * Allows checking if keyboard has been locked (no input can be sent) by the terminal server.
   *
   * @return True if the keyboard is currently locked, false otherwise.
   */
  public boolean isKeyboardLocked() {
    return screen.isKeyboardLocked();
  }

  /**
   * Add a {@link KeyboardStatusListener} to the terminal emulator.
   *
   * @param listener the listener to be notified when the status (locked/unlocked) of the keyboard
   * has changed.
   */
  public void addKeyboardStatusListener(KeyboardStatusListener listener) {
    screen.addKeyboardStatusChangeListener(listener);
  }

  /**
   * Remove a {@link KeyboardStatusListener} from the terminal emulator.
   *
   * @param listener the listener to be removed from notifications.
   */
  public void removeKeyboardStatusListener(KeyboardStatusListener listener) {
    screen.removeKeyboardStatusChangeListener(listener);
  }

  /**
   * Gets the status of the alarm.
   *
   * Prefer using resetAlarm so it is properly reset when checking value. Use this operation only if
   * you are implementing some tracing or debugging and don't want to change the alarm flag status.
   */
  public boolean isAlarmOn() {
    return screen.isAlarmOn();
  }

  /**
   * Allows resetting and getting the status of the alarm triggered by the terminal server.
   *
   * @return True if the alarm has sounded, false otherwise.
   */
  public boolean resetAlarm() {
    return screen.resetAlarm();
  }

  /**
   * Get the screen dimensions of the terminal emulator screen.
   *
   * @return Allows getting the number of rows and columns used by the terminal emulator.
   */
  public ScreenDimensions getScreenDimensions() {
    return screen.getScreenDimensions();
  }

  /**
   * Get the position of the cursor in the screen.
   *
   * @return The position of the cursor in the screen (x contains the column and y the row). If the
   * cursor is not visible then empty value is returned.
   */
  public Optional<Point> getCursorPosition() {
    Cursor cursor = screen.getScreenCursor();
    int location = cursor.getLocation();
    int columns = screen.getScreenDimensions().columns;
    return cursor.isVisible()
        ? Optional.of(new Point(location % columns + 1, location / columns + 1))
        : Optional.empty();
  }

  /**
   * Add a {@link CursorMoveListener} to the terminal emulator.
   *
   * @param listener listener to be notified when the cursor is moved by terminal server.
   */
  public void addCursorMoveListener(CursorMoveListener listener) {
    screen.getScreenCursor().addCursorMoveListener(listener);
  }

  /**
   * Remove a {@link CursorMoveListener} from the terminal emulator.
   *
   * @param listener listener to be remove from notificaitons.
   */
  public void removeCursorMoveListener(CursorMoveListener listener) {
    screen.getScreenCursor().removeCursorMoveListener(listener);
  }

  /**
   * Disconnect the terminal emulator from the server.
   *
   * @throws InterruptedException thrown when the disconnect is interrupted.
   */
  public void disconnect() throws InterruptedException {
    consolePane.disconnect();
  }

}
