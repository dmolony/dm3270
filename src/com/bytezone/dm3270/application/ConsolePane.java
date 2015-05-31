package com.bytezone.dm3270.application;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.FieldChangeListener;
import com.bytezone.dm3270.display.FontManager;
import com.bytezone.dm3270.display.KeyboardStatusListener;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenHistory;
import com.bytezone.dm3270.extended.CommandHeader;
import com.bytezone.dm3270.extended.TN3270ExtendedCommand;
import com.bytezone.dm3270.filetransfer.FileStage;
import com.bytezone.dm3270.filetransfer.TransferStage;
import com.bytezone.dm3270.jobs.JobStage;
import com.bytezone.dm3270.plugins.PluginsStage;
import com.bytezone.dm3270.streams.TelnetListener;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.streams.TerminalServer;

public class ConsolePane extends BorderPane implements FieldChangeListener,
    CursorMoveListener, KeyboardStatusListener
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
  private TransferStage transferStage;
  private FileStage fileStage;
  private JobStage jobStage;

  private int commandHeaderCount;
  private final BorderPane topPane = new BorderPane ();

  private ScreenHistory screenHistory;
  private final Button btnBack = new Button ("<");
  private final Button btnForward = new Button (">");
  private final Button btnCurrent = new Button ("Screens");

  private HBox historyBox;
  private final Label historyLabel = new Label ();

  private final BorderPane statusPane;

  private final MenuBar menuBar = new MenuBar ();
  private final FontManager fontManager;

  private final ToolBar toolbar = new ToolBar ();
  private boolean toolbarVisible;

  public ConsolePane (Screen screen, Site server, PluginsStage pluginsStage)
  {
    this.screen = screen;
    this.screen.setConsolePane (this);

    screen.getScreenCursor ().addFieldChangeListener (this);
    screen.getScreenCursor ().addCursorMoveListener (this);
    screen.addStatusChangeListener (this);

    setMargin (screen, new Insets (MARGIN, MARGIN, 0, MARGIN));

    toolbar.getItems ().addAll (btnBack, btnCurrent, btnForward);
    btnBack.setDisable (true);
    btnForward.setDisable (true);

    btnBack.setOnAction (e -> back ());
    btnForward.setOnAction (e -> forward ());
    btnCurrent.setOnAction (e -> toggleHistory ());

    //    byte[] buffer = { (byte) 0xF5, (byte) 0xC3 };
    //    Command clearCommand = Command.getCommand (buffer, 0, buffer.length, screen);
    //    btnClear.setOnAction (e -> clearCommand.process ());
    //
    //    byte[] buffer2 = { (byte) 0xF1, (byte) 0xC2 };
    //    Command resetCommand = Command.getCommand (buffer2, 0, buffer2.length, screen);
    //    btnReset.setOnAction (e -> resetCommand.process ());

    fontManager = new FontManager (screen);

    menuBar.getMenus ().addAll (getCommandsMenu (), fontManager.getFontMenu ());
    if (server == null || server.getPlugins ())     // allow null for replay testing
      menuBar.getMenus ().add (pluginsStage.getMenu (server));

    topPane.setTop (menuBar);
    if (SYSTEM_MENUBAR)
      menuBar.useSystemMenuBarProperty ().set (true);

    setTop (topPane);
    setCenter (screen);
    setBottom (statusPane = getStatusBar ());

    setHistoryBar ();

    screen.requestFocus ();
  }

  private Menu getCommandsMenu ()
  {
    Menu menuCommands = new Menu ("Commands");

    MenuItem menuItemToggleToolbar = new MenuItem ("Toolbar");
    menuItemToggleToolbar.setOnAction (e -> toggleToolbar ());
    menuItemToggleToolbar.setAccelerator (new KeyCodeCombination (KeyCode.T,
        KeyCombination.SHORTCUT_DOWN));

    MenuItem menuItemToggleScreens = new MenuItem ("Screen history");
    menuItemToggleScreens.setOnAction (e -> toggleHistory ());
    menuItemToggleScreens.setAccelerator (new KeyCodeCombination (KeyCode.S,
        KeyCombination.SHORTCUT_DOWN));

    MenuItem menuItemFileTransfer = new MenuItem ("File transfer");
    menuItemFileTransfer.setOnAction (e -> fileTransfer ());
    menuItemFileTransfer.setAccelerator (new KeyCodeCombination (KeyCode.F,
        KeyCombination.SHORTCUT_DOWN));

    MenuItem menuItemReportDisplay = new MenuItem ("Reports");
    menuItemReportDisplay.setOnAction (e -> fileDisplay ());
    menuItemReportDisplay.setAccelerator (new KeyCodeCombination (KeyCode.R,
        KeyCombination.SHORTCUT_DOWN));

    MenuItem menuItemJobDisplay = new MenuItem ("Batch jobs");
    menuItemJobDisplay.setOnAction (e -> jobDisplay ());
    menuItemJobDisplay.setAccelerator (new KeyCodeCombination (KeyCode.J,
        KeyCombination.SHORTCUT_DOWN));

    menuCommands.getItems ().addAll (menuItemToggleToolbar, menuItemToggleScreens,
                                     menuItemFileTransfer, menuItemReportDisplay,
                                     menuItemJobDisplay);

    if (!SYSTEM_MENUBAR)
    {
      MenuItem quitMenuItem = new MenuItem ("Quit");
      menuCommands.getItems ().addAll (new SeparatorMenuItem (), quitMenuItem);
      quitMenuItem.setOnAction (e -> Platform.exit ());
      quitMenuItem.setAccelerator (new KeyCodeCombination (KeyCode.Q,
          KeyCombination.SHORTCUT_DOWN));
    }

    return menuCommands;
  }

  private BorderPane getStatusBar ()
  {
    Separator[] div = new Separator[6];
    for (int i = 0; i < div.length; i++)
    {
      div[i] = new Separator ();
      div[i].setOrientation (Orientation.VERTICAL);
    }

    HBox left = getHBox (new Insets (2, GAP, 2, 3), Pos.CENTER_LEFT);
    left.getChildren ().addAll (div[0], fieldLocation, div[1], insertMode, div[2]);

    HBox center = getHBox (new Insets (2, GAP, 2, GAP), Pos.CENTER);
    center.getChildren ().addAll (status);

    HBox right = getHBox (new Insets (2, 0, 2, GAP), Pos.CENTER_RIGHT);
    right.getChildren ().addAll (div[3], fieldType, div[4], cursorLocation, div[5]);

    Font statusBarFont = fontManager.getDefaultFont ();
    status.setFont (statusBarFont);
    insertMode.setFont (statusBarFont);
    cursorLocation.setFont (statusBarFont);
    fieldType.setFont (statusBarFont);
    fieldLocation.setFont (statusBarFont);

    BorderPane statusPane = new BorderPane ();
    statusPane.setLeft (left);
    statusPane.setCenter (center);
    statusPane.setRight (right);

    return statusPane;
  }

  private void setHistoryBar ()
  {
    historyBox = getHBox (new Insets (2, GAP, 2, GAP), Pos.CENTER);
    historyBox.getChildren ().add (historyLabel);
    Font statusBarFont = fontManager.getDefaultFont ();
    historyLabel.setFont (statusBarFont);
  }

  private HBox getHBox (Insets insets, Pos alignment)
  {
    HBox hbox = new HBox ();
    hbox.setPadding (insets);
    hbox.setSpacing (10);
    hbox.setAlignment (alignment);
    return hbox;
  }

  private void toggleToolbar ()
  {
    toolbarVisible = !toolbarVisible;
    topPane.setBottom (toolbarVisible ? toolbar : null);
    ((Stage) getScene ().getWindow ()).sizeToScene ();
  }

  private void toggleHistory ()
  {
    if (screenHistory == null)
    {
      screenHistory = screen.pause ();
      if (screenHistory == null)          // no history to show
        return;
      setView (screenHistory.current ());
      btnBack.setDisable (false);
      btnForward.setDisable (false);
      setBottom (historyBox);
      historyLabel.setText (String.format ("Screen %02d of %02d",
                                           screenHistory.getCurrentIndex () + 1,
                                           screenHistory.size ()));
    }
    else
    {
      setView (null);
      btnBack.setDisable (true);
      btnForward.setDisable (true);
      setBottom (statusPane);
    }
  }

  private void fileTransfer ()
  {
    if (transferStage == null)
      transferStage = new TransferStage (screen, this);

    transferStage.show ();
  }

  private void jobDisplay ()
  {
    if (jobStage == null)
      jobStage = new JobStage ();

    jobStage.show ();
  }

  private void fileDisplay ()
  {
    if (fileStage == null)
      fileStage = screen.getFileStage ();
    if (fileStage != null)
      fileStage.show ();
  }

  void back ()
  {
    if (screenHistory != null && screenHistory.hasPrevious ())
    {
      setView (screenHistory.previous ());
      historyLabel.setText (String.format ("Screen %02d of %02d",
                                           screenHistory.getCurrentIndex () + 1,
                                           screenHistory.size ()));
    }
  }

  void forward ()
  {
    if (screenHistory != null && screenHistory.hasNext ())
    {
      setView (screenHistory.next ());
      historyLabel.setText (String.format ("Screen %02d of %02d",
                                           screenHistory.getCurrentIndex () + 1,
                                           screenHistory.size ()));
    }
  }

  private void setView (ImageView imageView)
  {
    if (imageView == null)
    {
      screenHistory = null;
      setCenter (screen);
      screen.resume ();
      setStyle (null);
    }
    else
    {
      setCenter (imageView);
      setMargin (imageView, new Insets (MARGIN, MARGIN, 0, MARGIN));
      setStyle ("-fx-background-color: navajowhite;");
    }
  }

  // called from ConsoleKeyPress.handle (KeyEvent e)
  // called from TransferStage.doStuff()
  public void sendAID (byte aid, String name)
  {
    if (screen.isInsertMode ())
      screen.toggleInsertMode ();

    screen.lockKeyboard (name);
    screen.setAID (aid);

    AIDCommand command = screen.readModifiedFields ();

    assert telnetState != null;

    if (telnetState.does3270Extended ())
    {
      byte[] buffer = new byte[5];
      Utility.packUnsignedShort (commandHeaderCount++, buffer, 3);
      CommandHeader header = new CommandHeader (buffer);
      TN3270ExtendedCommand extendedCommand = new TN3270ExtendedCommand (header, command);
      telnetState.write (extendedCommand.getTelnetData ());
    }
    else
      telnetState.write (command.getTelnetData ());
  }

  // called from Screen.processPluginRequest (Plugin plugin)
  public void sendAID (AIDCommand command)
  {
    assert telnetState != null;

    if (telnetState.does3270Extended ())
    {
      byte[] buffer = new byte[5];
      Utility.packUnsignedShort (commandHeaderCount++, buffer, 3);
      CommandHeader header = new CommandHeader (buffer);
      TN3270ExtendedCommand extendedCommand = new TN3270ExtendedCommand (header, command);
      telnetState.write (extendedCommand.getTelnetData ());
    }
    else
      telnetState.write (command.getTelnetData ());
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
  public void keyboardStatusChanged (boolean keyboardLocked, String keyName,
      boolean insert)
  {
    status.setText (keyboardLocked ? keyName : "       ");
    insertMode.setText (insert ? "Insert" : "      ");
  }
}