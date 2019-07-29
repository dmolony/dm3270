package com.bytezone.dm3270.application;

import com.bytezone.dm3270.ConnectionListener;
import com.bytezone.dm3270.buffers.Buffer;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.FieldChangeListener;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.extended.CommandHeader;
import com.bytezone.dm3270.extended.TN3270ExtendedCommand;
import com.bytezone.dm3270.streams.TelnetListener;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.streams.TerminalServer;
import javax.net.SocketFactory;

public class ConsolePane implements FieldChangeListener, CursorMoveListener,
    KeyboardStatusListener {

  private final Screen screen;

  private final TelnetState telnetState;
  private final Site server;
  private final SocketFactory socketFactory;

  private TerminalServer terminalServer;
  private Thread terminalServerThread;
  private int connectionTimeoutMillis;
  private ConnectionListener connectionListener;

  public ConsolePane(Screen screen, Site server, SocketFactory socketFactory) {
    this.screen = screen;
    this.telnetState = screen.getTelnetState();
    this.server = server;
    this.socketFactory = socketFactory;

    screen.setConsolePane(this);
    screen.getScreenCursor().addFieldChangeListener(this);
    screen.getScreenCursor().addCursorMoveListener(this);
  }

  public void setConnectionTimeoutMillis(int connectionTimeoutMillis) {
    this.connectionTimeoutMillis = connectionTimeoutMillis;
  }

  public void setConnectionListener(ConnectionListener connectionListener) {
    this.connectionListener = connectionListener;
  }

  public void sendAID(byte aid, String name) {
    if (screen.isInsertMode()) {
      screen.toggleInsertMode();
    }

    screen.lockKeyboard(name);
    screen.setAID(aid);

    Command command = screen.readModifiedFields();
    sendAID(command);
  }

  private void sendAID(Command command) {
    assert telnetState != null;

    if (telnetState.does3270Extended()) {
      byte[] buffer = new byte[5];
      if (screen.isSscpLuData()) {
        buffer[0] = 0x07;
      }
      Buffer.packUnsignedShort(telnetState.nextCommandHeaderSeq(), buffer, 3);
      CommandHeader header = new CommandHeader(buffer, screen.getCharset());
      TN3270ExtendedCommand extendedCommand = new TN3270ExtendedCommand(header, command,
          telnetState, screen.getCharset());
      telnetState.write(extendedCommand.getTelnetData());
    } else {
      telnetState.write(command.getTelnetData());
    }
  }

  public void connect() {
    if (server == null) {
      throw new IllegalArgumentException("Server must not be null");
    }

    // set preferences for this session
    telnetState.setDo3270Extended(server.getExtended());
    telnetState.setDoTerminalType(true);

    TelnetListener telnetListener = new TelnetListener(screen, telnetState);
    terminalServer =
        new TerminalServer(server.getURL(), server.getPort(), socketFactory, telnetListener);
    terminalServer.setConnectionTimeoutMillis(connectionTimeoutMillis);
    terminalServer.setConnectionListener(connectionListener);
    telnetState.setTerminalServer(terminalServer);

    terminalServerThread = new Thread(terminalServer);
    terminalServerThread.start();
  }

  public void disconnect() throws InterruptedException {
    telnetState.close();

    if (terminalServer != null) {
      terminalServer.close();
    }

    if (terminalServerThread != null) {
      terminalServerThread.interrupt();
      terminalServerThread.join();
    }
  }

  @Override
  public void fieldChanged(Field oldField, Field newField) {
  }

  @Override
  public void cursorMoved(int oldLocation, int newLocation, Field currentField) {
    fieldChanged(currentField, currentField);            // update the acronym
  }

  @Override
  public void keyboardStatusChanged(KeyboardStatusChangedEvent evt) {
  }

}
