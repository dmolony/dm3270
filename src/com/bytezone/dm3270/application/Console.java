package com.bytezone.dm3270.application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.session.Session;

public class Console extends Application
{
  private static String[] preferredFontNames = { //
      "Andale Mono", "Anonymous Pro", "Consolas", "Courier New", "DejaVu Sans Mono",
          "Hermit", "IBM 3270", "IBM 3270 Narrow", "Inconsolata", "Input Mono",
          "Input Mono Narrow", "Luculent", "Menlo", "Monaco", "M+ 2m", "PT Mono",
          "Source Code Pro", "Monospaced" };

  private static final int MAINFRAME_EMULATOR_PORT = 5555;
  private static final int EDIT_BUTTON_WIDTH = 50;
  private static final String EDIT_BUTTON_FONT_SIZE = "-fx-font-size: 10;";

  private Stage primaryStage;
  private ComboBox<String> fileComboBox;
  private ComboBox<String> serverComboBox;
  private ComboBox<String> clientComboBox;

  private Button editServersButton;
  private Button editClientsButton;
  private Button editLocationButton;

  private SiteListStage serverSitesListStage;
  private SiteListStage clientSitesListStage;

  private final Button okButton = new Button ("Connect");
  private final Button cancelButton = new Button ("Cancel");

  private Preferences prefs;
  private String spyFolder;

  private final ToggleGroup functionsGroup = new ToggleGroup ();
  private final ToggleGroup fontGroup = new ToggleGroup ();
  private final ToggleGroup sizeGroup = new ToggleGroup ();
  private final ToggleGroup releaseGroup = new ToggleGroup ();

  private MainframeStage mainframeStage;
  private SpyPane spyPane;
  private ConsolePane consolePane;
  private ReplayStage replayStage;

  private boolean release;
  private MenuBar menuBar = new MenuBar ();

  public enum Function
  {
    SPY, REPLAY, TERMINAL, TEST
  }

  @Override
  public void init () throws Exception
  {
    super.init ();

    prefs = Preferences.userNodeForPackage (this.getClass ());
    for (String raw : getParameters ().getRaw ())
      if (raw.equals ("-reset"))
        prefs.clear ();

    if (false)
    {
      String[] keys = prefs.keys ();
      Arrays.sort (keys);
      for (String key : keys)
        System.out.printf ("%-14s : %s%n", key, prefs.get (key, ""));
    }
  }

  @Override
  public void start (Stage primaryStage) throws Exception
  {
    this.primaryStage = primaryStage;

    String fileText = prefs.get ("ReplayFile", "");
    String optionSelected = prefs.get ("Function", "Terminal");
    String fontSelected = prefs.get ("FontName", "");
    String sizeSelected = prefs.get ("FontSize", "16");
    String runMode = prefs.get ("Mode", "Release");
    spyFolder = prefs.get ("SpyFolder", "");
    String serverSelected = prefs.get ("ServerName", "");
    String clientSelected = prefs.get ("ClientName", "");

    serverSitesListStage = new SiteListStage (prefs, "Server", 5, true);
    clientSitesListStage = new SiteListStage (prefs, "Client", 5, false);

    String[] optionList = { "Spy", "Replay", "Terminal", "Test" };
    Node row1 = options (optionList, functionsGroup, 0, 2);
    Node row2 = options (optionList, functionsGroup, 2, 2);

    VBox panel = new VBox (10);

    ObservableList<String> sessionFiles = getSessionFiles (spyFolder);
    fileComboBox = new ComboBox<> (sessionFiles);
    fileComboBox.setVisibleRowCount (12);
    if (!fileText.isEmpty ())
      fileComboBox.getSelectionModel ().select (fileText);

    serverComboBox = serverSitesListStage.getComboBox ();
    serverComboBox.setVisibleRowCount (5);
    serverComboBox.getSelectionModel ().select (serverSelected);

    editServersButton = serverSitesListStage.getEditButton ();
    editServersButton.setStyle (EDIT_BUTTON_FONT_SIZE);
    editServersButton.setMinWidth (EDIT_BUTTON_WIDTH);

    clientComboBox = clientSitesListStage.getComboBox ();
    clientComboBox.setVisibleRowCount (5);
    clientComboBox.getSelectionModel ().select (clientSelected);

    editClientsButton = clientSitesListStage.getEditButton ();
    editClientsButton.setStyle (EDIT_BUTTON_FONT_SIZE);
    editClientsButton.setMinWidth (EDIT_BUTTON_WIDTH);

    editLocationButton = new Button ("Locate");
    editLocationButton.setStyle (EDIT_BUTTON_FONT_SIZE);
    editLocationButton.setMinWidth (EDIT_BUTTON_WIDTH);

    int width = 150;
    fileComboBox.setPrefWidth (width);
    serverComboBox.setPrefWidth (width);
    clientComboBox.setPrefWidth (width);

    release = runMode.equals ("Release");
    if (release)
    {
      panel.getChildren ().addAll (row ("Server", serverComboBox, editServersButton),
                                   row ("", buttons ()));
      primaryStage.setTitle ("Connect to Server");
      panel.setPadding (new Insets (0, 30, 0, 0));       // trbl
    }
    else
    {
      panel.getChildren //
          ().addAll (row ("Function", row1), row ("", row2),
                     row ("Server", serverComboBox, editServersButton),
                     row ("Client", clientComboBox, editClientsButton),
                     row ("Replay", fileComboBox, editLocationButton),
                     row ("", buttons ()));
      primaryStage.setTitle ("Choose Function");
    }

    HBox hBox = new HBox (10);
    HBox.setMargin (panel, new Insets (10));
    hBox.getChildren ().addAll (panel);

    functionsGroup.selectedToggleProperty ().addListener (new ChangeListener<Toggle> ()
    {
      @Override
      public void changed (ObservableValue<? extends Toggle> ov, Toggle oldToggle,
          Toggle newToggle)
      {
        if (newToggle == null)
          return;

        switch ((String) newToggle.getUserData ())
        {
          case "Spy":
            setDisable (false, false, true);
            break;

          case "Replay":
            setDisable (true, true, false);
            break;

          case "Terminal":
            setDisable (false, true, true);
            break;

          case "Test":
            setDisable (true, false, true);
            break;
        }
      }
    });

    if (release)
      functionsGroup.selectToggle (functionsGroup.getToggles ().get (2));
    else
    {
      boolean found = false;
      for (int i = 0; i < optionList.length; i++)
      {
        if (optionList[i].equals (optionSelected))
        {
          functionsGroup.selectToggle (functionsGroup.getToggles ().get (i));
          found = true;
          break;
        }
      }
      if (!found)
        functionsGroup.selectToggle (functionsGroup.getToggles ().get (2));
    }

    okButton.setDefaultButton (true);
    okButton.setOnAction (e -> startSelectedFunction ());
    cancelButton.setOnAction (e -> primaryStage.hide ());
    editLocationButton.setOnAction (e -> editLocation ());

    Menu menuFont = new Menu ("Fonts");
    Menu menuDebug = new Menu ("Mode");

    List<String> families = Font.getFamilies ();
    for (String fontName : preferredFontNames)
    {
      boolean fontExists = families.contains (fontName);
      if (fontExists && fontSelected.isEmpty ())
        fontSelected = fontName;
      setMenuItem (fontName, fontGroup, menuFont, fontSelected, !fontExists);
    }

    // select Monospaced if there is still no font selected
    if (fontGroup.getSelectedToggle () == null)
    {
      ObservableList<Toggle> toggles = fontGroup.getToggles ();
      fontGroup.selectToggle (toggles.get (toggles.size () - 1));
    }

    menuFont.getItems ().add (new SeparatorMenuItem ());

    setMenuItem ("12", sizeGroup, menuFont, sizeSelected, false);
    setMenuItem ("14", sizeGroup, menuFont, sizeSelected, false);
    setMenuItem ("16", sizeGroup, menuFont, sizeSelected, false);
    setMenuItem ("18", sizeGroup, menuFont, sizeSelected, false);
    setMenuItem ("20", sizeGroup, menuFont, sizeSelected, false);
    setMenuItem ("22", sizeGroup, menuFont, sizeSelected, false);

    setMenuItem ("Debug", releaseGroup, menuDebug, runMode, false);
    setMenuItem ("Release", releaseGroup, menuDebug, runMode, false);

    menuBar.getMenus ().addAll (menuFont, menuDebug);

    final String os = System.getProperty ("os.name");
    if (os != null && os.startsWith ("Mac"))
      menuBar.useSystemMenuBarProperty ().set (true);

    BorderPane borderPane = new BorderPane ();
    borderPane.setTop (menuBar);
    borderPane.setCenter (hBox);

    primaryStage.resizableProperty ().setValue (Boolean.FALSE);
    primaryStage.setOnCloseRequest (e -> Platform.exit ());
    primaryStage.setScene (new Scene (borderPane));
    primaryStage.show ();
  }

  private void startSelectedFunction ()
  {
    primaryStage.hide ();

    Site serverSite = serverSitesListStage.getSelectedSite ();
    Site clientSite = clientSitesListStage.getSelectedSite ();

    String optionText = (String) functionsGroup.getSelectedToggle ().getUserData ();
    switch (optionText)
    {
      case "Replay":
        Path path = Paths.get (spyFolder + "/" + fileComboBox.getValue ());
        if (Files.exists (path))
          try
          {
            Screen screen = createScreen (Function.REPLAY);
            Session session = new Session (screen, path);     // can throw Exception
            setConsole (screen);                              // reassigns primaryStage
            primaryStage.show ();

            replayStage = new ReplayStage (session, path, prefs);
            replayStage.show ();
          }
          catch (Exception e)
          {
            e.printStackTrace ();
            if (showAlert ("Error reading file"))
              primaryStage.show ();
          }
        else if (showAlert (path + " does not exist"))
          primaryStage.show ();

        break;

      case "Terminal":
        if (serverSite != null)
        {
          setConsole (createScreen (Function.TERMINAL));
          primaryStage.centerOnScreen ();
          primaryStage.show ();
          consolePane.connect (serverSite);
        }
        else if (showAlert ("No server selected"))
          primaryStage.show ();

        break;

      case "Spy":
        if (serverSite != null && clientSite != null)
          setSpyPane (createScreen (Function.SPY), serverSite, clientSite);
        else if (showAlert (serverSite == null ? "No server selected"
            : "No client selected"))
          primaryStage.show ();

        break;

      case "Test":
        if (clientSite != null)
        {
          Site mainframe =
              new Site ("mainframe", "localhost", MAINFRAME_EMULATOR_PORT, true);

          setSpyPane (createScreen (Function.TEST), mainframe, clientSite);

          mainframeStage = new MainframeStage (MAINFRAME_EMULATOR_PORT);
          mainframeStage.show ();
          mainframeStage.startServer ();
        }
        else if (showAlert ("No client selected"))
          primaryStage.show ();

        break;
    }
  }

  private void setConsole (Screen screen)
  {
    consolePane = new ConsolePane (screen);

    Scene scene = new Scene (consolePane);
    primaryStage.setScene (scene);

    primaryStage.setX (0);
    primaryStage.setY (0);

    primaryStage.sizeToScene ();
    primaryStage.setTitle ("dm3270");

    scene.setOnKeyPressed (new ConsoleKeyPress (consolePane, screen));
    scene.setOnKeyTyped (new ConsoleKeyEvent (screen));

    menuBar = consolePane.getMenuBar ();
  }

  private void setSpyPane (Screen screen, Site server, Site client)
  {
    spyPane = new SpyPane (screen, server, client);
    Scene scene = new Scene (spyPane);
    primaryStage.setScene (scene);

    primaryStage.setX (1200);
    primaryStage.setY (20);

    primaryStage.sizeToScene ();
    primaryStage.setTitle ("Terminal Spy");

    primaryStage.show ();
    spyPane.startServer ();
  }

  private boolean showAlert (String message)
  {
    Alert alert = new Alert (AlertType.ERROR, message);
    alert.getDialogPane ().setHeaderText (null);
    Optional<ButtonType> result = alert.showAndWait ();
    return (result.isPresent () && result.get () == ButtonType.OK);
  }

  @Override
  public void stop ()
  {
    if (mainframeStage != null)
      mainframeStage.disconnect ();

    if (spyPane != null)
      spyPane.disconnect ();

    if (consolePane != null)
      consolePane.disconnect ();

    if (replayStage != null)
      replayStage.disconnect ();

    savePreferences ();
  }

  private void editLocation ()
  {
    DirectoryChooser chooser = new DirectoryChooser ();
    chooser.setTitle ("Choose Spy Folder");
    File currentLocation = spyFolder.isEmpty () ? null : new File (spyFolder);
    if (currentLocation != null && currentLocation.exists ())
      chooser.setInitialDirectory (currentLocation);

    File selectedDirectory = chooser.showDialog (primaryStage);
    if (selectedDirectory != null)
    {
      spyFolder = selectedDirectory.getAbsolutePath ();
      fileComboBox.getItems ().clear ();
      fileComboBox.setItems (getSessionFiles (spyFolder));
    }
  }

  private ObservableList<String> getSessionFiles (String folderName)
  {
    List<Path> files = null;

    try
    {
      Path path = Paths.get (folderName);
      if (Files.exists (path) && Files.isDirectory (path))
        files =
            Files.list (path)
                .filter (p -> p.getFileName ().toString ().matches ("spy[0-9]{2}\\.txt"))
                .collect (Collectors.toList ());
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }

    if (files == null)
      files = new ArrayList<Path> ();      // empty list

    return FXCollections
        .observableArrayList (files.stream ().map (p -> p.getFileName ().toString ())
            .sorted ().collect (Collectors.toList ()));
  }

  private void savePreferences ()
  {
    prefs.put ("Function", (String) functionsGroup.getSelectedToggle ().getUserData ());
    prefs.put ("FontName", ((RadioMenuItem) fontGroup.getSelectedToggle ()).getText ());
    prefs.put ("FontSize", ((RadioMenuItem) sizeGroup.getSelectedToggle ()).getText ());
    prefs.put ("Mode", ((RadioMenuItem) releaseGroup.getSelectedToggle ()).getText ());

    String filename = fileComboBox.getValue ();
    if (filename != null)
      prefs.put ("ReplayFile", filename);
    prefs.put ("SpyFolder", spyFolder);
    prefs.put ("ServerName", serverComboBox.getSelectionModel ().getSelectedItem ());
    prefs.put ("ClientName", clientComboBox.getSelectionModel ().getSelectedItem ());
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

  private void setMenuItem (String itemName, ToggleGroup toggleGroup, Menu menu,
      String selectedItemName, boolean disable)
  {
    RadioMenuItem item = new RadioMenuItem (itemName);
    item.setToggleGroup (toggleGroup);
    menu.getItems ().add (item);
    if (itemName.equals (selectedItemName))
      item.setSelected (true);
    item.setDisable (disable);
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

  private Node buttons ()
  {
    HBox hbox = new HBox (10);
    okButton.setDefaultButton (true);
    cancelButton.setCancelButton (true);
    okButton.setPrefWidth (80);
    cancelButton.setPrefWidth (80);
    hbox.getChildren ().addAll (cancelButton, okButton);
    return hbox;
  }

  private Node row (String labelText, Node... field)
  {
    HBox hbox = new HBox (10);
    hbox.setAlignment (Pos.CENTER_LEFT);
    Label label = new Label (labelText);
    label.setMinWidth (65);
    label.setAlignment (Pos.CENTER_RIGHT);
    hbox.getChildren ().add (label);
    hbox.getChildren ().addAll (field);
    return hbox;
  }

  private Screen createScreen (Function function)
  {
    RadioMenuItem selectedFontName = (RadioMenuItem) fontGroup.getSelectedToggle ();
    RadioMenuItem selectedFontSize = (RadioMenuItem) sizeGroup.getSelectedToggle ();
    Font font = Font.font (selectedFontName.getText (),     //
                           Integer.parseInt (selectedFontSize.getText ()));
    return new Screen (24, 80, font, function);
  }
}