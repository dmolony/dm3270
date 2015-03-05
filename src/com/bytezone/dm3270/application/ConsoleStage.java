package com.bytezone.dm3270.application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.FieldChangeListener;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.session.Session;
import com.bytezone.dm3270.session.Session.SessionMode;
import com.bytezone.dm3270.streams.TelnetListener;
import com.bytezone.dm3270.streams.TelnetSocket.Source;
import com.bytezone.dm3270.streams.TerminalServer;

public class ConsoleStage extends Stage implements FieldChangeListener,
    CursorMoveListener
{
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

  private Field currentField;

  public ConsoleStage (Screen screen, String mainframeURL, int mainframePort)
  {
    this (screen);
    this.mainframeURL = mainframeURL;
    this.mainframePort = mainframePort;
  }

  public ConsoleStage (Screen screen)
  {
    this.screen = screen;

    screen.getScreenCursor ().addFieldChangeListener (this);
    screen.getScreenCursor ().addCursorMoveListener (this);

    setTitle ("dm3270");

    int margin = 4;
    BorderPane.setMargin (screen, new Insets (margin, margin, 0, margin));

    ToolBar toolbar = new ToolBar ();
    toolbar.getItems ().add (new Button ("Home"));
    toolbar.getItems ().add (new Button ("Options"));
    toolbar.getItems ().add (new Button ("Help"));

    //    final String os = System.getProperty ("os.name");
    //    if (os != null && os.startsWith ("Mac"))
    //      menuBar.useSystemMenuBarProperty ().set (true);

    BorderPane borderPane = new BorderPane ();
    borderPane.setCenter (screen);
    borderPane.setTop (toolbar);
    //    borderPane.setTop (menuBar);

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

    getScene ().setOnKeyPressed (new ConsoleKeyPress (this, screen));
    getScene ().setOnKeyTyped (new ConsoleKeyEvent (screen));
  }

  public void sendData (byte[] buffer)
  {
    if (buffer == null)
    {
      System.out.println ("Sending null!");
      return;
    }

    if (buffer[0] != (byte) 0x88)
      screen.lockKeyboard ();

    if (telnetState != null)
      telnetState.write (buffer);
  }

  // Inhibit message
  public void setStatus (String text)
  {
    status.setText (text);
  }

  public void connect ()
  {
    telnetState = new TelnetState ();
    session = new Session (screen, telnetState, SessionMode.TERMINAL);
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

  @Override
  public void fieldChanged (Field oldField, Field newField)
  {
    if (newField == null)
      fieldType.setText ("");
    else
    {
      StartFieldAttribute sfa = newField.getStartFieldAttribute ();
      fieldType.setText (String.format ("%4d  %6s", newField.getDisplayLength (),
                                        sfa.getAcronym ()));
    }
    currentField = newField;
  }

  @Override
  public void cursorMoved (int oldLocation, int newLocation)
  {
    int row = newLocation / screen.columns;
    int col = newLocation % screen.columns;
    cursorLocation.setText (String.format ("%03d/%03d", row, col));
    fieldChanged (currentField, currentField);    // update the acronym
  }
}