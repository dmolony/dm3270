package com.bytezone.dm3270.application;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenDetails;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.extended.CommandHeader;
import com.bytezone.dm3270.extended.TN3270ExtendedCommand;
import com.bytezone.dm3270.plugins.PluginsStage;
import com.bytezone.dm3270.streams.TelnetListener;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.streams.TerminalServer;
import com.bytezone.dm3270.utilities.Utility;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

public class ConsolePane extends BorderPane implements FieldChangeListener,
    CursorMoveListener, KeyboardStatusListener, ScreenChangeListener
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
  private final TelnetState telnetState = new TelnetState ();
  private int commandHeaderCount;
  private final Site server;
  private Site replaySite;

  private TerminalServer terminalServer;
  private Thread terminalServerThread;

  private HistoryManager screenHistory;             // null unless showing screen history 

  private HBox historyBox;                          // status display area
  private final Label historyLabel = new Label ();  // status text
  private final BorderPane statusPane;

  private final MenuBar menuBar = new MenuBar ();
  private MenuItem menuItemUpload;
  private MenuItem menuItemDownload;

  private final FontManager fontManager;
  private final ScreenDimensions screenDimensions;

  public ConsolePane (Screen screen, Site server, PluginsStage pluginsStage)
  {
    this.screen = screen;
    this.screenDimensions = screen.getScreenDimensions ();
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
          System.out.printf ("Time offset: %s %d%n", direction, value);
          ZonedDateTime now = ZonedDateTime.now (ZoneOffset.UTC);
          System.out.println ("UTC : " + now);
          if (direction == '+')
            now = now.plusHours (value);
          else
            now = now.minusHours (value);
          System.out.println ("Site: " + now);
        }
      }
    }

    screen.requestFocus ();
  }

  public void setStatusText (String text)
  {
    status.setText (text);
  }

  void setReplayServer (Site serverSite)
  {
    replaySite = serverSite;
  }

  private Menu getCommandsMenu ()
  {
    Menu menuCommands = new Menu ("Commands");

    MenuItem menuItemToggleScreens =
        getMenuItem ("Screen history", e -> toggleHistory (), KeyCode.S);

    MenuItem menuItemAssistant =
        getMenuItem ("Transfers", e -> screen.getAssistantStage ().show (), KeyCode.T);
    menuItemUpload = getMenuItem ("Upload", e -> upload (), KeyCode.U);
    menuItemDownload = getMenuItem ("Download", e -> download (), KeyCode.D);

    menuCommands.getItems ().addAll (menuItemToggleScreens, menuItemAssistant,
                                     new SeparatorMenuItem (), menuItemUpload,
                                     menuItemDownload);

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

  private void upload ()
  {
    System.out.println ("upload " + menuItemUpload.getUserData ());
  }

  private void download ()
  {
    String fileName = (String) menuItemDownload.getUserData ();
    Site site = server != null ? server : replaySite != null ? replaySite : null;
    String folderName = site != null ? site.getFolder () : "";

    String userHome = System.getProperty ("user.home");
    Path filePath = Paths.get (userHome, "dm3270", "files", folderName);
    if (Files.notExists (filePath))
    {
      // show dialog
      System.out.println ("Path does not exist: " + filePath);
      return;
    }

    String buildPath = filePath.toString ();
    int baseLength = userHome.length () + 1;

    String[] segments = fileName.split ("\\.");         // split into segments
    int last = segments.length - 1;
    if (last >= 0 && segments[last].endsWith (")"))     // is last segment a pds member?
    {
      int pos = segments[last].indexOf ('(');
      segments[last] = segments[last].substring (0, pos);     // remove '(member name)'
    }

    int nextSegment = 0;

    while (Files.notExists (Paths.get (buildPath, fileName)))
    {
      if (nextSegment >= segments.length)
        break;
      Path nextPath = Paths.get (buildPath, segments[nextSegment++]);
      if (Files.notExists (nextPath))
        break;
      buildPath = nextPath.toString ();
    }

    String cmd = showDialog (buildPath, baseLength, fileName);
    if ("OK".equals (cmd))
      System.out.println ("Download: " + fileName);
  }

  private String showDialog (String buildPath, int baseLength, String fileName)
  {
    Path path = Paths.get (buildPath, fileName);

    Label label1 = new Label ("Download: ");
    Label label2 = new Label (fileName);
    Label label3 = new Label ("To folder: ");
    Label label4 = new Label (buildPath.substring (baseLength));
    Label label5 = new Label ("Exists: ");
    Label label6 = new Label (Files.exists (path) ? "Yes" : "No");

    Dialog<String> dialog = new Dialog<> ();

    GridPane grid = new GridPane ();
    grid.add (label1, 1, 1);
    grid.add (label2, 2, 1);
    grid.add (label3, 1, 2);
    grid.add (label4, 2, 2);
    grid.add (label5, 1, 3);
    grid.add (label6, 2, 3);
    grid.setHgap (10);
    grid.setVgap (10);
    dialog.getDialogPane ().setContent (grid);

    ButtonType btnTypeOK = new ButtonType ("OK", ButtonData.OK_DONE);
    ButtonType btnTypeCancel = new ButtonType ("Cancel", ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane ().getButtonTypes ().addAll (btnTypeOK, btnTypeCancel);
    dialog.setResultConverter (btnType ->
    {
      if (btnType == btnTypeOK)
        return "OK";
      return "";
    });

    return dialog.showAndWait ().get ();
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
  // called from TransferStage.doStuff()
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
      Utility.packUnsignedShort (commandHeaderCount++, buffer, 3);
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

  @Override
  public void screenChanged (ScreenDetails screenDetails)
  {
    String datasetName = screenDetails.getSingleDataset ();
    if (datasetName.isEmpty ())
    {
      menuItemDownload.setUserData (null);
      menuItemUpload.setUserData (null);
      menuItemDownload.setDisable (true);
      menuItemUpload.setDisable (true);
    }
    else
    {
      menuItemDownload.setUserData (datasetName);
      menuItemUpload.setUserData (datasetName);
      menuItemDownload.setDisable (false);
      menuItemUpload.setDisable (false);
    }
  }
}