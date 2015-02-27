package com.bytezone.dm3270.application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.session.Session;
import com.bytezone.dm3270.session.Session.SessionMode;
import com.bytezone.dm3270.streams.TelnetListener;
import com.bytezone.dm3270.streams.TelnetSocket.Source;
import com.bytezone.dm3270.streams.TerminalServer;

public class ConsoleStage extends Stage
{
  private final ScreenHandler screenHandler;
  private final Screen screen;
  private final Label status = new Label ();
  private final Label cursorLocation = new Label ();
  private final Label fieldType = new Label ();

  private String mainframeURL;
  private int mainframePort;

  private TelnetListener telnetListener;
  private TelnetState telnetState;
  private Session session;
  private TerminalServer terminalServer;

  private ScreenField currentField;

  public ConsoleStage (ScreenHandler screenHandler, Screen screen, String mainframeURL,
      int mainframePort)
  {
    this (screenHandler, screen);
    this.mainframeURL = mainframeURL;
    this.mainframePort = mainframePort;
  }

  public ConsoleStage (ScreenHandler screenHandler, Screen screen)
  {
    this.screenHandler = screenHandler;
    this.screen = screen;
    screenHandler.setConsoleStage (this);

    setTitle ("dm3270");

    ScreenCanvas canvas = screenHandler.getScreenCanvas ();
    canvas.setCursor (Cursor.CROSSHAIR);
    int margin = 4;
    BorderPane.setMargin (canvas, new Insets (margin, margin, 0, margin));

    BorderPane borderPane = new BorderPane ();
    borderPane.setCenter (canvas);

    HBox hbox1 = new HBox ();
    hbox1.setPadding (new Insets (2, 12, 2, 12));       // trbl
    hbox1.setSpacing (10);
    hbox1.setAlignment (Pos.CENTER_LEFT);
    hbox1.getChildren ().add (status);

    HBox hbox2 = new HBox ();
    hbox2.setPadding (new Insets (2, 4, 2, 12));        // trbl
    hbox2.setSpacing (20);
    hbox2.setAlignment (Pos.CENTER_RIGHT);
    hbox2.getChildren ().addAll (fieldType, cursorLocation);

    Font font = Font.font ("Monospaced", 14);
    status.setFont (font);
    cursorLocation.setFont (font);
    fieldType.setFont (font);

    BorderPane statusPane = new BorderPane ();
    statusPane.setLeft (hbox1);
    statusPane.setRight (hbox2);
    borderPane.setBottom (statusPane);

    setScene (new Scene (borderPane));
    resizableProperty ().setValue (Boolean.FALSE);
    setX (0);
    setY (0);

    getScene ().setOnKeyPressed (new ConsoleKeyPress (this, screenHandler, screen));
    getScene ().setOnKeyTyped (new ConsoleKeyEvent (screenHandler, screen));
  }

  public void sendData (byte[] buffer)
  {
    if (buffer == null)
    {
      System.out.println ("Sending null!");
      return;
    }

    if (buffer[0] != (byte) 0x88)
      screenHandler.lockKeyboard ();

    if (telnetState != null)
      telnetState.write (buffer);
  }

  public void setStatus (String text)
  {
    status.setText (text);
  }

  public void setCursorLocation (int row, int column)
  {
    cursorLocation.setText (String.format ("%03d/%03d", row, column));
  }

  public void setCurrentField (ScreenField screenField)
  {
    if (screenField != currentField)
    {
      if (screenField == null)
        fieldType.setText ("");
      else
        setFieldType (screenField);
      currentField = screenField;
    }
  }

  public void setFieldType (ScreenField screenField)
  {
    StartFieldAttribute sfa = screenField.getStartFieldAttribute ();
    String sfaText = sfa == null ? "" : sfa.getAcronym ();
    fieldType.setText (String.format ("%4d  %6s", screenField.getLength (), sfaText));
  }

  public void connect ()
  {
    telnetState = new TelnetState ();
    session = new Session (screenHandler, screen, telnetState, SessionMode.TERMINAL);
    telnetListener = new TelnetListener (Source.SERVER, session);
    terminalServer = new TerminalServer (mainframeURL, mainframePort, telnetListener);
    telnetState.setTerminalServer (terminalServer);

    new Thread (terminalServer).start ();
  }

  public void disconnect ()
  {
    if (terminalServer != null)
      terminalServer.close ();
  }
}