package com.bytezone.dm3270.application;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.FieldChangeListener;
import com.bytezone.dm3270.display.KeyboardStatusListener;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenHistory;
import com.bytezone.dm3270.extended.CommandHeader;
import com.bytezone.dm3270.extended.TN3270ExtendedCommand;
import com.bytezone.dm3270.streams.TelnetListener;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.streams.TerminalServer;

public class ConsoleStage extends Stage implements FieldChangeListener,
    CursorMoveListener, KeyboardStatusListener
{
  private static int margin = 4;

  private final Screen screen;
  private final Label status = new Label ();
  private final Label insertMode = new Label ();
  private final Label cursorLocation = new Label ();
  private final Label fieldType = new Label ();
  private final Label fieldLocation = new Label ();

  private TelnetListener telnetListener;
  private final TelnetState telnetState = new TelnetState ();
  private TerminalServer terminalServer;
  private Thread terminalServerThread;

  private int commandHeaderCount;
  private final BorderPane borderPane = new BorderPane ();

  private ScreenHistory screenHistory;
  private final Button btnBack = new Button ("<");
  private final Button btnForward = new Button (">");
  private final Button btnCurrent = new Button ("History");

  public ConsoleStage (Screen screen)
  {
    this.screen = screen;

    screen.getScreenCursor ().addFieldChangeListener (this);
    screen.getScreenCursor ().addCursorMoveListener (this);
    screen.addStatusChangeListener (this);

    setTitle ("dm3270");

    BorderPane.setMargin (screen, new Insets (margin, margin, 0, margin));

    borderPane.setCenter (screen);

    ToolBar toolbar = new ToolBar ();
    toolbar.getItems ().addAll (btnBack, btnCurrent, btnForward);
    btnBack.setDisable (true);
    btnForward.setDisable (true);

    //      byte[] buffer = { (byte) 0xF5, (byte) 0xC3 };
    //      Command clearCommand = Command.getCommand (buffer, 0, buffer.length, screen);
    //      btnClear.setOnAction (e -> clearCommand.process ());
    //
    //      byte[] buffer2 = { (byte) 0xF1, (byte) 0xC2 };
    //      Command resetCommand = Command.getCommand (buffer2, 0, buffer2.length, screen);
    //      btnReset.setOnAction (e -> resetCommand.process ());

    borderPane.setTop (toolbar);

    HBox hbox0 = new HBox ();
    hbox0.setPadding (new Insets (2, 12, 2, 4));       // trbl
    hbox0.setSpacing (10);
    hbox0.setAlignment (Pos.CENTER_LEFT);
    hbox0.getChildren ().add (fieldLocation);

    HBox hbox1 = new HBox ();
    hbox1.setPadding (new Insets (2, 12, 2, 12));       // trbl
    hbox1.setSpacing (10);
    hbox1.setAlignment (Pos.CENTER);
    hbox1.getChildren ().add (status);

    HBox hbox2 = new HBox ();
    hbox2.setPadding (new Insets (2, 4, 2, 12));        // trbl
    hbox2.setSpacing (20);
    hbox2.setAlignment (Pos.CENTER_RIGHT);
    hbox2.getChildren ().addAll (insertMode, fieldType, cursorLocation);

    Font font = Font.font ("Monospaced", 14);
    status.setFont (font);
    insertMode.setFont (font);
    cursorLocation.setFont (font);
    fieldType.setFont (font);
    fieldLocation.setFont (font);

    BorderPane statusPane = new BorderPane ();
    statusPane.setLeft (hbox0);
    statusPane.setCenter (hbox1);
    statusPane.setRight (hbox2);
    borderPane.setBottom (statusPane);

    setScene (new Scene (borderPane));
    resizableProperty ().setValue (Boolean.FALSE);
    setX (0);
    setY (0);

    getScene ().setOnKeyPressed (new ConsoleKeyPress (this, screen));
    getScene ().setOnKeyTyped (new ConsoleKeyEvent (screen));

    screen.requestFocus ();

    setOnCloseRequest (new EventHandler<WindowEvent> ()
    {
      @Override
      public void handle (WindowEvent we)
      {
        Platform.exit ();
      }
    });

    btnBack.setOnAction ( (e) -> {
      back ();
    });

    btnForward.setOnAction ( (e) -> {
      forward ();
    });

    btnCurrent.setOnAction ( (e) -> {
      toggleHistory ();
    });
  }

  public void toggleHistory ()
  {
    if (screenHistory == null)
    {
      screenHistory = screen.pause ();
      if (screenHistory == null)          // no history to show
        return;
      setView (screenHistory.current ());
      btnBack.setDisable (false);
      btnForward.setDisable (false);
    }
    else
    {
      setView (null);
      btnBack.setDisable (true);
      btnForward.setDisable (true);
    }
  }

  public void back ()
  {
    if (screenHistory != null && screenHistory.hasPrevious ())
      setView (screenHistory.previous ());
  }

  public void forward ()
  {
    if (screenHistory != null && screenHistory.hasNext ())
      setView (screenHistory.next ());
  }

  private void setView (ImageView imageView)
  {
    if (imageView == null)
    {
      screenHistory = null;
      borderPane.setCenter (screen);
      screen.resume ();
    }
    else
    {
      BorderPane.setMargin (imageView, new Insets (margin, margin, 0, margin));
      borderPane.setCenter (imageView);
    }
  }

  public void sendAID (AIDCommand command)
  {
    if (telnetState != null && telnetState.does3270Extended ())
    {
      byte[] buffer = new byte[5];
      Utility.packUnsignedShort (commandHeaderCount++, buffer, 3);
      CommandHeader header = new CommandHeader (buffer);
      TN3270ExtendedCommand extendedCommand = new TN3270ExtendedCommand (header, command);
      sendData (extendedCommand.getTelnetData ());
    }
    else
      sendData (command.getTelnetData ());
  }

  public void sendData (byte[] buffer)
  {
    if (buffer == null)
    {
      System.out.println ("Sending null!");
      return;
    }

    if (telnetState != null)
      telnetState.write (buffer);
  }

  public void connect (Site server)
  {
    if (server == null)
      throw new IllegalArgumentException ("Server must not be null");

    telnetState.setDo3270Extended (true);    // set preferences for this session
    telnetState.setDoTerminalType (true);    // set preferences for this session

    telnetListener = new TelnetListener (screen, telnetState);
    terminalServer =
        new TerminalServer (server.getURL (), server.getPort (), telnetListener);
    telnetState.setTerminalServer (terminalServer);

    terminalServerThread = new Thread (terminalServer);
    terminalServerThread.start ();
  }

  public void disconnect ()
  {
    if (terminalServer != null)
      terminalServer.close ();

    telnetState.close ();

    if (terminalServerThread != null)
    {
      terminalServerThread.interrupt ();
      try
      {
        terminalServerThread.join ();
      }
      catch (InterruptedException e)
      {
        e.printStackTrace ();
      }
    }
  }

  @Override
  public void fieldChanged (Field oldField, Field newField)
  {
    if (newField == null)
    {
      fieldType.setText ("      ");
      fieldLocation.setText ("0000/0000");
    }
    else
    {
      StartFieldAttribute sfa = newField.getStartFieldAttribute ();
      fieldType.setText (String.format ("%6.6s", sfa.getAcronym ()));
      fieldLocation.setText (String.format ("%04d/%04d", newField.getCursorOffset (),
                                            newField.getDisplayLength ()));
    }
  }

  @Override
  public void cursorMoved (int oldLocation, int newLocation, Field currentField)
  {
    int row = newLocation / screen.columns;
    int col = newLocation % screen.columns;
    cursorLocation.setText (String.format ("%03d/%03d", row, col));
    fieldChanged (currentField, currentField);    // update the acronym
  }

  @Override
  public void keyboardStatusChanged (boolean keyboardLocked, boolean insert)
  {
    status.setText (keyboardLocked ? "Inhibit" : "       ");
    insertMode.setText (insert ? "Insert " : "       ");
  }
}