package com.bytezone.dm3270;

import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenPosition;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.utilities.Site;
import javax.net.SocketFactory;

/**
 * Client to connect to TN3270 terminal servers.
 * <p>
 * This class provides a facade to ease usage of dm3270 to java clients.
 */
public class Tn3270Client {

  private Screen screen;
  private ConsolePane consolePane;
  private SocketFactory socketFactory = SocketFactory.getDefault();
  private ExceptionHandler exceptionHandler;
  private int connectionTimeoutMillis;

  public void connect(String host, int port, TerminalType terminalType) {
    TelnetState telnetState = new TelnetState();
    telnetState.setDoDeviceType(terminalType.getModel());
    screen = new Screen(terminalType.getScreenDimensions(), null, telnetState);
    screen.lockKeyboard("connect");
    consolePane = new ConsolePane(screen,
        new Site(host, port, terminalType.usesExtended3270()), socketFactory);
    consolePane.setConnectionTimeoutMillis(connectionTimeoutMillis);
    consolePane.setExceptionHandler(exceptionHandler);
    consolePane.connect();
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

  public boolean isKeyboardLocked() {
    return screen.isKeyboardLocked();
  }

  public void addKeyboardStatusListener(KeyboardStatusListener listener) {
    screen.addKeyboardStatusChangeListener(listener);
  }

  public void removeKeyboardStatusListener(KeyboardStatusListener listener) {
    screen.removeKeyboardStatusChangeListener(listener);
  }

  public void addScreenChangeListener(ScreenChangeListener listener) {
    screen.getFieldManager().addScreenChangeListener(listener);
  }

  public void removeScreenChangeListener(ScreenChangeListener listener) {
    screen.getFieldManager().removeScreenChangeListener(listener);
  }

  public boolean resetAlarm() {
    return screen.resetAlarm();
  }

  public void disconnect() throws InterruptedException {
    consolePane.disconnect();
  }

}
