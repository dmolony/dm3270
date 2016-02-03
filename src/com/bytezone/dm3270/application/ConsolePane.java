package com.bytezone.dm3270.application;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import com.bytezone.dm3270.application.Parameters.SiteParameters;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.FieldChangeListener;
import com.bytezone.dm3270.display.FontManager;
import com.bytezone.dm3270.display.HistoryManager;
import com.bytezone.dm3270.display.HistoryScreen;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.extended.CommandHeader;
import com.bytezone.dm3270.extended.TN3270ExtendedCommand;
import com.bytezone.dm3270.plugins.PluginsStage;
import com.bytezone.dm3270.streams.TelnetListener;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.streams.TerminalServer;
import com.bytezone.dm3270.utilities.Dm3270Utility;
import com.bytezone.dm3270.utilities.Site;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

public class ConsolePane extends BorderPane
    implements FieldChangeListener, CursorMoveListener, KeyboardStatusListener
{
  private final static int MARGIN = 4;
  private final static int GAP = 12;
  private final static String OS = System.getProperty ("os.name");
  private final static boolean SYSTEM_MENUBAR = OS != null && OS.startsWith ("Mac");
  private final Parameters parameters = new Parameters ();

  private final Screen screen;
  private final Label status = new Label ();
  private final Label insertMode = new Label ();
  private final Label cursorLocation = new Label ();
  private final Label fieldType = new Label ();
  private final Label fieldLocation = new Label ();

  private TelnetListener telnetListener;
  private final TelnetState telnetState;
  private int commandHeaderCount;
  private final Site server;

  private TerminalServer terminalServer;
  private Thread terminalServerThread;

  private HistoryManager screenHistory;             // null unless showing screen history 

  private HBox historyBox;                          // status display area
  private final Label historyLabel = new Label ();  // status text
  private final BorderPane statusPane;

  private final MenuBar menuBar = new MenuBar ();
  private MenuItem menuItemAssistant;
  private MenuItem menuItemConsoleLog;

  private final FontManager fontManager;

  public ConsolePane (Screen screen, Site server, PluginsStage pluginsStage)
  {
    this.screen = screen;
    this.telnetState = screen.getTelnetState ();
    this.server = server;

    this.fontManager = screen.getFontManager ();
    pluginsStage.setConsolePane (this);

    screen.setConsolePane (this);
    screen.getScreenCursor ().addFieldChangeListener (this);
    screen.getScreenCursor ().addCursorMoveListener (this);

    setMargin (screen, new Insets (MARGIN, MARGIN, 0, MARGIN));

    menuBar.getMenus ().addAll (getCommandsMenu (), fontManager.getFontMenu ());

    // allow null for replay testing
    if (server == null || server.getPlugins ())
      menuBar.getMenus ().add (pluginsStage.getMenu (server));

    setTop (menuBar);
    setCenter (screen);
    setBottom (statusPane = getStatusBar ());
    menuBar.setUseSystemMenuBar (SYSTEM_MENUBAR);

    setHistoryBar ();

    if (server != null)
    {
      Optional<SiteParameters> sp = parameters.getSiteParameters (server.getName ());
      if (sp.isPresent ())
      {
        String offset = sp.get ().getParameter ("offset");
        if (!offset.isEmpty () && offset.length () > 4)
        {
          char direction = offset.charAt (3);
          int value = Integer.parseInt (offset.substring (4));
          System.out.printf ("Time offset : %s %d%n", direction, value);
          ZonedDateTime now = ZonedDateTime.now (ZoneOffset.UTC);
          System.out.println ("UTC         : " + now);
          if (direction == '+')
            now = now.plusHours (value);
          else
            now = now.minusHours (value);
          System.out.println ("Site        : " + now);
          System.out.println ();
        }
      }
    }

    screen.requestFocus ();
  }

  public void setStatusText (String text)
  {
    status.setText (text);
  }

  private Menu getCommandsMenu ()
  {
    Menu menuCommands = new Menu ("Commands");

    MenuItem menuItemToggleScreens =
        getMenuItem ("Screen history", e -> toggleHistory (), KeyCode.S);

    menuItemAssistant =
        getMenuItem ("Transfers", e -> screen.getAssistantStage ().show (), KeyCode.T);

    menuItemConsoleLog =
        getMenuItem ("Console log", e -> screen.getConsoleLogStage ().show (), KeyCode.L);
    setIsConsole (false);

    menuCommands.getItems ().addAll (menuItemToggleScreens, menuItemAssistant,
                                     menuItemConsoleLog, new SeparatorMenuItem (),
                                     screen.getMenuItemUpload (),
                                     screen.getMenuItemDownload ());

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

  private MenuItem getMenuItem (String text, EventHandler<ActionEvent> eventHandler,
      KeyCode keyCode)
  {
    MenuItem menuItem = new MenuItem (text);
    menuItem.setOnAction (eventHandler);
    menuItem
        .setAccelerator (new KeyCodeCombination (keyCode, KeyCombination.SHORTCUT_DOWN));
    return menuItem;
  }

  private BorderPane getStatusBar ()
  {
    Separator[] div = new Separator[6];
    for (int i = 0; i < div.length; i++)
    {
      div[i] = new Separator ();
      div[i].setOrientation (Orientation.VERTICAL);
    }

    HBox leftBox = getHBox (new Insets (2, GAP, 2, 3), Pos.CENTER_LEFT);
    leftBox.getChildren ().addAll (div[0], fieldLocation, div[1], insertMode, div[2]);

    HBox centerBox = getHBox (new Insets (2, GAP, 2, GAP), Pos.CENTER);
    centerBox.getChildren ().add (status);

    HBox rightBox = getHBox (new Insets (2, 0, 2, GAP), Pos.CENTER_RIGHT);
    rightBox.getChildren ().addAll (div[3], fieldType, div[4], cursorLocation, div[5]);

    setStatusFont ();

    BorderPane statusPane = new BorderPane ();
    statusPane.setLeft (leftBox);
    statusPane.setCenter (centerBox);
    statusPane.setRight (rightBox);

    return statusPane;
  }

  public void setIsConsole (boolean value)
  {
    menuItemConsoleLog.setDisable (!value);
    menuItemAssistant.setDisable (value);
  }

  // called from this.getStatusBar()
  // called from Screen.fontChanged()
  public void setStatusFont ()
  {
    Font font = fontManager.getStatusBarFont ();
    status.setFont (font);
    insertMode.setFont (font);
    cursorLocation.setFont (font);
    fieldType.setFont (font);
    fieldLocation.setFont (font);
  }

  private void setHistoryBar ()
  {
    historyBox = getHBox (new Insets (2, GAP, 2, GAP), Pos.CENTER);
    historyBox.getChildren ().add (historyLabel);
    Font statusBarFont = fontManager.getStatusBarFont ();
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

  private void toggleHistory ()
  {
    if (screenHistory == null)                  // in normal screen mode
    {
      Optional<HistoryManager> opt = screen.pause ();
      if (opt.isPresent ())
      {
        screenHistory = opt.get ();
        changeScreen (screenHistory.current ());
        setBottom (historyBox);
      }
    }
    else                                        // in screen history mode
    {
      screenHistory = null;
      setCenter (screen);
      setBottom (statusPane);
      screen.resume ();
      setStyle (null);
    }
  }

  void back ()
  {
    if (screenHistory != null && screenHistory.hasPrevious ())
      changeScreen (screenHistory.previous ());
  }

  void forward ()
  {
    if (screenHistory != null && screenHistory.hasNext ())
      changeScreen (screenHistory.next ());
  }

  private void changeScreen (HistoryScreen historyScreen)
  {
    historyScreen.drawScreen (screen.getFontManager ().getFontDetails ());
    setCenter (historyScreen);
    setMargin (historyScreen, new Insets (MARGIN, MARGIN, 0, MARGIN));
    setStyle ("-fx-background-color: navajowhite;");
    historyLabel.setText (String.format ("Screen %02d of %02d",
                                         screenHistory.getCurrentIndex () + 1,
                                         screenHistory.size ()));
  }

  // called from ConsoleKeyPress.handle (KeyEvent e)
  // called from TSOCommand.execute()
  public void sendAID (byte aid, String name)
  {
    if (screen.isInsertMode ())
      screen.toggleInsertMode ();

    screen.lockKeyboard (name);
    screen.setAID (aid);

    AIDCommand command = screen.readModifiedFields ();
    sendAID (command);
  }

  // called from PluginsStage.processPluginRequest (Plugin plugin)
  public void sendAID (AIDCommand command)
  {
    assert telnetState != null;

    if (telnetState.does3270Extended ())
    {
      byte[] buffer = new byte[5];
      Dm3270Utility.packUnsignedShort (commandHeaderCount++, buffer, 3);
      CommandHeader header = new CommandHeader (buffer);
      TN3270ExtendedCommand extendedCommand = new TN3270ExtendedCommand (header, command);
      telnetState.write (extendedCommand.getTelnetData ());
    }
    else
      telnetState.write (command.getTelnetData ());
  }

  // called from Console.startSelectedFunction()
  // called from Terminal.start()
  void connect ()
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
    ScreenDimensions screenDimensions = screen.getScreenDimensions ();
    int row = newLocation / screenDimensions.columns;
    int col = newLocation % screenDimensions.columns;
    cursorLocation.setText (String.format ("%03d/%03d", row, col));
    fieldChanged (currentField, currentField);            // update the acronym
  }

  @Override
  public void keyboardStatusChanged (KeyboardStatusChangedEvent evt)
  {
    setStatusText (evt.keyboardLocked ? evt.keyName : "       ");
    insertMode.setText (evt.insertMode ? "Insert" : "      ");
  }
}