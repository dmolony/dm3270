package com.bytezone.dm3270.application;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

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

class ConsolePane extends BorderPane implements FieldChangeListener, CursorMoveListener,
    KeyboardStatusListener
{
  private final static int MARGIN = 4;
  private final static int GAP = 12;
  private final static String OS = System.getProperty ("os.name");
  private final static boolean SYSTEM_MENUBAR = OS != null && OS.startsWith ("Mac");

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
  private final BorderPane topPane = new BorderPane ();

  private ScreenHistory screenHistory;
  private final Button btnBack = new Button ("<");
  private final Button btnForward = new Button (">");
  private final Button btnCurrent = new Button ("Screens");

  //  private final MenuBar menuBar = new MenuBar ();
  private final Menu menuCommands = new Menu ("Commands");
  private final ToolBar toolbar = new ToolBar ();
  private boolean toolbarVisible;

  public ConsolePane (Screen screen, MenuBar menuBar)
  {
    this.screen = screen;

    screen.getScreenCursor ().addFieldChangeListener (this);
    screen.getScreenCursor ().addCursorMoveListener (this);
    screen.addStatusChangeListener (this);

    setMargin (screen, new Insets (MARGIN, MARGIN, 0, MARGIN));

    setTop (topPane);
    setCenter (screen);

    toolbar.getItems ().addAll (btnBack, btnCurrent, btnForward);
    btnBack.setDisable (true);
    btnForward.setDisable (true);

    //    byte[] buffer = { (byte) 0xF5, (byte) 0xC3 };
    //    Command clearCommand = Command.getCommand (buffer, 0, buffer.length, screen);
    //    btnClear.setOnAction (e -> clearCommand.process ());
    //
    //    byte[] buffer2 = { (byte) 0xF1, (byte) 0xC2 };
    //    Command resetCommand = Command.getCommand (buffer2, 0, buffer2.length, screen);
    //    btnReset.setOnAction (e -> resetCommand.process ());

    MenuItem menuItemToggleToolbar = new MenuItem ("Toggle toolbar");
    menuCommands.getItems ().addAll (menuItemToggleToolbar);
    menuBar.getMenus ().add (menuCommands);
    topPane.setTop (menuBar);

    //    System.out.println (menuBar);
    //    System.out.println (menuBar.getMenus ().size ());
    //    System.out.println (menuBar.isUseSystemMenuBar ());

    //    final String os = System.getProperty ("os.name");
    //    if (os != null && os.startsWith ("Mac"))
    //      menuBar.useSystemMenuBarProperty ().set (true);

    menuItemToggleToolbar.setOnAction ( (e) -> toggleToolbar ());

    Separator[] div = new Separator[4];
    for (int i = 0; i < div.length; i++)
    {
      div[i] = new Separator ();
      div[i].setOrientation (Orientation.VERTICAL);
    }

    HBox left = getHBox (new Insets (2, GAP, 2, MARGIN), Pos.CENTER_LEFT);
    left.getChildren ().addAll (fieldLocation, div[0], insertMode, div[1]);

    HBox center = getHBox (new Insets (2, GAP, 2, GAP), Pos.CENTER);
    center.getChildren ().addAll (status);

    HBox right = getHBox (new Insets (2, MARGIN, 2, GAP), Pos.CENTER_RIGHT);
    right.getChildren ().addAll (div[2], fieldType, div[3], cursorLocation);

    Font font = Font.font ("Monospaced", 14);
    status.setFont (font);
    insertMode.setFont (font);
    cursorLocation.setFont (font);
    fieldType.setFont (font);
    fieldLocation.setFont (font);

    BorderPane statusPane = new BorderPane ();
    statusPane.setLeft (left);
    statusPane.setCenter (center);
    statusPane.setRight (right);
    setBottom (statusPane);

    screen.requestFocus ();

    btnBack.setOnAction (e -> back ());
    btnForward.setOnAction (e -> forward ());
    btnCurrent.setOnAction (e -> toggleHistory ());
  }

  private HBox getHBox (Insets insets, Pos alignment)
  {
    HBox hbox = new HBox ();
    hbox.setPadding (insets);
    hbox.setSpacing (10);
    hbox.setAlignment (alignment);
    return hbox;
  }

  public void toggleToolbar ()
  {
    toolbarVisible = !toolbarVisible;
    topPane.setBottom (toolbarVisible ? toolbar : null);
    ((Stage) getScene ().getWindow ()).sizeToScene ();
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
      setCenter (screen);
      screen.resume ();
    }
    else
    {
      BorderPane.setMargin (imageView, new Insets (MARGIN, MARGIN, 0, MARGIN));
      setCenter (imageView);
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

    // set preferences for this session
    telnetState.setDo3270Extended (server.getExtended ());
    telnetState.setDoTerminalType (true);

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
    insertMode.setText (insert ? "Insert" : "      ");
  }
}