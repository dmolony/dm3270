package com.bytezone.dm3270.application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import com.bytezone.dm3270.plugins.PluginsStage;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class OptionStage extends Stage
{
  private static final int COMBO_BOX_WIDTH = 150;
  private static final int EDIT_BUTTON_WIDTH = 50;
  private static final String EDIT_BUTTON_FONT_SIZE = "-fx-font-size: 10;";
  private final static String OS = System.getProperty ("os.name");
  private final static boolean SYSTEM_MENUBAR = OS != null && OS.startsWith ("Mac");

  private final boolean release;

  ComboBox<String> fileComboBox;
  ComboBox<String> serverComboBox;
  ComboBox<String> clientComboBox;

  private Button editServersButton;
  private Button editClientsButton;
  private Button editLocationButton;

  final SiteListStage serverSitesListStage;
  final SiteListStage clientSitesListStage;

  final Button okButton = new Button ("Connect");
  final Button cancelButton = new Button ("Cancel");

  private final Preferences prefs;
  String spyFolder;

  final ToggleGroup functionsGroup = new ToggleGroup ();
  private final String[] optionList = { "Spy", "Replay", "Terminal", "Test" };

  private final BorderPane innerPane = new BorderPane ();
  private final BorderPane outerPane = new BorderPane ();
  private final VBox functionsBox = new VBox (10);
  private final HBox serverBox;
  private final VBox clientSpyBox = new VBox (10);
  private HBox buttonsBox = new HBox (10);

  CheckMenuItem toggleModeMenuItem;
  private final MenuBar menuBar = new MenuBar ();

  private enum Mode
  {
    DEBUG, RELEASE
  }

  public OptionStage (Preferences prefs, PluginsStage pluginsStage)
  {
    this.prefs = prefs;

    String optionSelected = prefs.get ("Function", "Terminal");

    String runMode = prefs.get ("Mode", "Release");
    release = runMode.equals ("Release");

    serverSitesListStage = new SiteListStage (prefs, "Server", 10, true);
    clientSitesListStage = new SiteListStage (prefs, "Client", 6, false);

    Node row1 = options (optionList, functionsGroup, 0, 2);
    Node row2 = options (optionList, functionsGroup, 2, 2);

    buildComboBoxes ();

    HBox functionsBox1 = row ("Function", row1);
    HBox functionsBox2 = row ("", row2);
    functionsBox.getChildren ().addAll (functionsBox1, functionsBox2);
    functionsBox.setPadding (new Insets (10, 10, 0, 10));

    serverBox = row ("Server", serverComboBox, editServersButton);
    serverBox.setPadding (new Insets (10, 10, 10, 10));// trbl

    HBox clientBox = row ("Client", clientComboBox, editClientsButton);
    HBox spyBox = row ("Replay", fileComboBox, editLocationButton);
    clientSpyBox.getChildren ().addAll (clientBox, spyBox);
    clientSpyBox.setPadding (new Insets (0, 10, 10, 10));

    buttonsBox = buttons ();

    innerPane.setTop (functionsBox);
    innerPane.setCenter (serverBox);
    outerPane.setCenter (innerPane);
    outerPane.setBottom (buttonsBox);
    setMode (release ? Mode.RELEASE : Mode.DEBUG);

    functionsGroup.selectedToggleProperty ().addListener ( (ov, oldToggle, newToggle) ->
    {
      if (newToggle != null)
        disableButtons ((String) newToggle.getUserData ());
    });

    if (release)
      optionSelected = "Terminal";

    boolean found = false;
    for (int i = 0; i < optionList.length; i++)
      if (optionList[i].equals (optionSelected))
      {
        functionsGroup.selectToggle (functionsGroup.getToggles ().get (i));
        found = true;
        break;
      }

    if (!found)
      functionsGroup.selectToggle (functionsGroup.getToggles ().get (2));   // Terminal
    disableButtons (optionSelected);

    editLocationButton.setOnAction (e -> editLocation ());

    Menu menuCommands = new Menu ("Commands");
    menuBar.getMenus ().add (menuCommands);
    outerPane.setTop (menuBar);

    toggleModeMenuItem = new CheckMenuItem ("Release mode");
    menuCommands.getItems ().addAll (toggleModeMenuItem, pluginsStage.getEditMenuItem ());

    toggleModeMenuItem.setSelected (runMode.equals ("Release"));
    toggleModeMenuItem.setOnAction (e -> switchMode (e));

    toggleModeMenuItem.setAccelerator (new KeyCodeCombination (KeyCode.M,
        KeyCombination.SHORTCUT_DOWN));

    menuBar.setUseSystemMenuBar (SYSTEM_MENUBAR);
    if (!SYSTEM_MENUBAR)
    {
      MenuItem quitMenuItem = new MenuItem ("Quit");
      menuCommands.getItems ().addAll (new SeparatorMenuItem (), quitMenuItem);
      quitMenuItem.setOnAction (e -> Platform.exit ());
      quitMenuItem.setAccelerator (new KeyCodeCombination (KeyCode.Q,
          KeyCombination.SHORTCUT_DOWN));
    }

    setResizable (false);
    setOnCloseRequest (e -> Platform.exit ());
    setScene (new Scene (outerPane));
    okButton.requestFocus ();
  }

  private void switchMode (ActionEvent e)
  {
    CheckMenuItem menuItem = (CheckMenuItem) e.getSource ();
    setMode (menuItem.isSelected () ? Mode.RELEASE : Mode.DEBUG);
  }

  private void setMode (Mode mode)
  {
    if (mode == Mode.DEBUG)
    {
      innerPane.setTop (functionsBox);
      innerPane.setBottom (clientSpyBox);
      setTitle ("Choose Function");
    }
    else
    {
      innerPane.setTop (null);
      innerPane.setBottom (null);
      setTitle ("Connect to Server");
      functionsGroup.selectToggle (functionsGroup.getToggles ().get (2));// Terminal
    }

    okButton.requestFocus ();
    sizeToScene ();
  }

  private void editLocation ()
  {
    DirectoryChooser chooser = new DirectoryChooser ();
    chooser.setTitle ("Choose Spy Folder");
    File currentLocation = spyFolder.isEmpty () ? null : new File (spyFolder);
    if (currentLocation != null && currentLocation.exists ())
      chooser.setInitialDirectory (currentLocation);

    File selectedDirectory = chooser.showDialog (this);
    if (selectedDirectory != null)
    {
      spyFolder = selectedDirectory.getAbsolutePath ();
      fileComboBox.getItems ().clear ();
      ObservableList<String> files = getSessionFiles (spyFolder);
      fileComboBox.setItems (files);
      if (files.size () > 0)
        fileComboBox.getSelectionModel ().select (0);
    }
  }

  private ObservableList<String> getSessionFiles (String folderName)
  {
    List<Path> files = null;

    try
    {
      Path path = Paths.get (folderName);
      if (Files.exists (path) && Files.isDirectory (path))
        files = Files.list (path)
            .filter (p -> p.getFileName ().toString ()
                .matches ("[sS][Pp][yY][0-9]{1,4}(\\.[tT][xX][tT])*"))
            .collect (Collectors.toList ());
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }

    if (files == null)
      files = new ArrayList<Path> ();// empty list

    return FXCollections
        .observableArrayList (files.stream ().map (p -> p.getFileName ().toString ())
            .sorted ().collect (Collectors.toList ()));
  }

  private Node options (String[] options, ToggleGroup group, int offset, int length)
  {
    HBox hbox = new HBox (10);

    for (int i = offset, max = offset + length; i < max; i++)
    {
      String option = options[i];
      RadioButton rb = new RadioButton (option);
      rb.setUserData (option);
      rb.setPrefWidth (100);
      rb.setToggleGroup (group);
      hbox.getChildren ().add (rb);
    }

    return hbox;
  }

  private void disableButtons (String selection)
  {
    if (selection.equals (optionList[0]))
      setDisable (false, false, true);                  // server, client, files
    else if (selection.equals (optionList[1]))
      setDisable (true, true, false);
    else if (selection.equals (optionList[2]))
      setDisable (false, true, true);
    else if (selection.equals (optionList[3]))
      setDisable (true, false, true);
  }

  private void setDisable (boolean server, boolean client, boolean files)
  {
    serverComboBox.setDisable (server);
    editServersButton.setDisable (server);
    clientComboBox.setDisable (client);
    editClientsButton.setDisable (client);
    fileComboBox.setDisable (files);
    editLocationButton.setDisable (files);
  }

  private void buildComboBoxes ()
  {
    spyFolder = prefs.get ("SpyFolder", "");
    String fileText = prefs.get ("ReplayFile", "");

    fileComboBox = new ComboBox<> (getSessionFiles (spyFolder));
    fileComboBox.setPrefWidth (COMBO_BOX_WIDTH);
    fileComboBox.setVisibleRowCount (15);
    fileComboBox.getSelectionModel ().select (fileText);

    String serverSelected = prefs.get ("ServerName", "");

    serverComboBox = serverSitesListStage.getComboBox ();
    serverComboBox.setPrefWidth (COMBO_BOX_WIDTH);
    serverComboBox.setVisibleRowCount (5);
    serverComboBox.getSelectionModel ().select (serverSelected);

    String clientSelected = prefs.get ("ClientName", "");

    clientComboBox = clientSitesListStage.getComboBox ();
    clientComboBox.setPrefWidth (COMBO_BOX_WIDTH);
    clientComboBox.setVisibleRowCount (5);
    clientComboBox.getSelectionModel ().select (clientSelected);

    editServersButton = serverSitesListStage.getEditButton ();
    editServersButton.setStyle (EDIT_BUTTON_FONT_SIZE);
    editServersButton.setMinWidth (EDIT_BUTTON_WIDTH);

    editClientsButton = clientSitesListStage.getEditButton ();
    editClientsButton.setStyle (EDIT_BUTTON_FONT_SIZE);
    editClientsButton.setMinWidth (EDIT_BUTTON_WIDTH);

    editLocationButton = new Button ("Folder...");
    editLocationButton.setStyle (EDIT_BUTTON_FONT_SIZE);
    editLocationButton.setMinWidth (EDIT_BUTTON_WIDTH);
  }

  private HBox buttons ()
  {
    HBox hbox = new HBox (10);
    hbox.setAlignment (Pos.CENTER);

    okButton.setDefaultButton (true);
    okButton.setPrefWidth (80);

    cancelButton.setCancelButton (true);
    cancelButton.setPrefWidth (80);

    hbox.getChildren ().addAll (cancelButton, okButton);
    hbox.setPadding (new Insets (10, 10, 10, 10));

    return hbox;
  }

  private HBox row (String labelText, Node... field)
  {
    HBox hbox = new HBox (10);
    hbox.setAlignment (Pos.CENTER_LEFT);

    Label label = new Label (labelText);
    label.setMinWidth (50);
    label.setAlignment (Pos.CENTER_RIGHT);

    hbox.getChildren ().add (label);
    hbox.getChildren ().addAll (field);

    return hbox;
  }
}