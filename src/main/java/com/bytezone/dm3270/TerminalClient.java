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

  public void setModel(int model) {
    this.model = model;
  }

  public void setScreenDimensions(ScreenDimensions screenDimensions) {
    this.screenDimensions = screenDimensions;
  }

  public void setUsesExtended3270(boolean usesExtended3270) {
    this.usesExtended3270 = usesExtended3270;
  }

  public void setSocketFactory(SocketFactory socketFactory) {
    this.socketFactory = socketFactory;
  }

  public void setConnectionTimeoutMillis(int connectionTimeoutMillis) {
    this.connectionTimeoutMillis = connectionTimeoutMillis;
  }

  public void setExceptionHandler(ExceptionHandler exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
  }

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

  public void setFieldText(int row, int column, String text) {
    int fieldLinearPosition = (row - 1) * screen.getScreenDimensions().columns + column - 1;
    Field field = screen.getFieldManager()
        .getFieldAt(fieldLinearPosition)
        .orElseThrow(
            () -> new IllegalArgumentException("Invalid field position " + row + "," + column));
    screen.setFieldText(field, text);
    screen.getScreenCursor().moveTo(fieldLinearPosition + text.length());
  }

  public void sendAID(byte aid, String name) {
    consolePane.sendAID(aid, name);
  }

  public String getScreenText() {
    StringBuilder text = new StringBuilder();
    int pos = 0;
    for (ScreenPosition sp : screen.getPen()) {
      text.append(sp.getCharString());
      if (++pos % screen.getScreenDimensions().columns == 0) {
        text.append("\n");
      }
    }
    return text.toString();
  }

  public void addScreenChangeListener(ScreenChangeListener listener) {
    screen.getFieldManager().addScreenChangeListener(listener);
  }

  public void removeScreenChangeListener(ScreenChangeListener listener) {
    screen.getFieldManager().removeScreenChangeListener(listener);
  }

  public boolean isKeyboardLocked() {
    return screen.isKeyboardLocked();
  }

  public void addKeyboardStatusListener(KeyboardStatusListener listener) {
    screen.addKeyboardStatusChangeListener(listener);
  }

  public void removeKeyboardStatusListener(KeyboardStatusListener listener) {
    screen.removeKeyboardStatusChangeListener(listener);
  }

  public boolean resetAlarm() {
    return screen.resetAlarm();
  }

  public ScreenDimensions getScreenDimensions() {
    return screen.getScreenDimensions();
  }

  public Point getCursorPosition() {
    Cursor cursor = screen.getScreenCursor();
    int location = cursor.getLocation();
    int columns = screen.getScreenDimensions().columns;
    return cursor.isVisible() ? new Point(location % columns + 1, location / columns + 1) : null;
  }

  public void addCursorMoveListener(CursorMoveListener listener) {
    screen.getScreenCursor().addCursorMoveListener(listener);
  }

  public void removeCursorMoveListener(CursorMoveListener listener) {
    screen.getScreenCursor().removeCursorMoveListener(listener);
  }

  public void disconnect() throws InterruptedException {
    consolePane.disconnect();
  }

}
