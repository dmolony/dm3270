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

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
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

  private final boolean release;

  private ComboBox<String> fileComboBox;
  private ComboBox<String> serverComboBox;
  private ComboBox<String> clientComboBox;

  private Button editServersButton;
  private Button editClientsButton;
  private Button editLocationButton;

  private final SiteListStage serverSitesListStage;
  private final SiteListStage clientSitesListStage;

  private final Button okButton = new Button ("Connect");
  private final Button cancelButton = new Button ("Cancel");

  private final Preferences prefs;
  private String spyFolder;

  private final ToggleGroup functionsGroup = new ToggleGroup ();
  private final ToggleGroup releaseGroup = new ToggleGroup ();

  private final MenuBar menuBar = new MenuBar ();

  public OptionStage (Preferences prefs)
  {
    this.prefs = prefs;

    String optionSelected = prefs.get ("Function", "Terminal");

    String runMode = prefs.get ("Mode", "Release");
    release = runMode.equals ("Release");

    serverSitesListStage = new SiteListStage (prefs, "Server", 5, true);
    clientSitesListStage = new SiteListStage (prefs, "Client", 5, false);

    String[] optionList = { "Spy", "Replay", "Terminal", "Test" };
    Node row1 = options (optionList, functionsGroup, 0, 2);
    Node row2 = options (optionList, functionsGroup, 2, 2);

    VBox panel = buildComboBoxes ();

    if (release)
    {
      panel.getChildren ().addAll (row ("Server", serverComboBox, editServersButton),
                                   row ("", buttons ()));
      setTitle ("Connect to Server");
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
      setTitle ("Choose Function");
    }

    HBox hBox = new HBox (10);
    HBox.setMargin (panel, new Insets (10));
    hBox.getChildren ().addAll (panel);

    functionsGroup.selectedToggleProperty ()
        .addListener ( (ov, oldToggle, newToggle) -> {
          if (newToggle != null)
          {
            String selection = (String) newToggle.getUserData ();
            if (selection.equals (optionList[0]))
              setDisable (false, false, true);          // server, client, file
            else if (selection.equals (optionList[1]))
              setDisable (true, true, false);
            else if (selection.equals (optionList[2]))
              setDisable (false, true, true);
            else if (selection.equals (optionList[3]))
              setDisable (true, false, true);
          }
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

    editLocationButton.setOnAction (e -> editLocation ());

    Menu menuDebug = new Menu ("Mode");
    setMenuItem ("Debug", releaseGroup, menuDebug, runMode, false);
    setMenuItem ("Release", releaseGroup, menuDebug, runMode, false);

    menuBar.getMenus ().addAll (menuDebug);

    BorderPane borderPane = new BorderPane ();
    borderPane.setTop (menuBar);
    borderPane.setCenter (hBox);

    resizableProperty ().setValue (Boolean.FALSE);
    setOnCloseRequest (e -> Platform.exit ());
    setScene (new Scene (borderPane));
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
        files =
            Files
                .list (path)
                .filter (p -> p.getFileName ().toString ()
                             .matches ("[sS][Pp][yY][0-9]{1,4}(\\.[tT][xX][tT])*"))
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

  private void setDisable (boolean server, boolean client, boolean files)
  {
    serverComboBox.setDisable (server);
    editServersButton.setDisable (server);
    clientComboBox.setDisable (client);
    editClientsButton.setDisable (client);
    fileComboBox.setDisable (files);
    editLocationButton.setDisable (files);
  }

  private VBox buildComboBoxes ()
  {
    VBox panel = new VBox (10);

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

    return panel;
  }

  private Node buttons ()
  {
    HBox hbox = new HBox (10);

    okButton.setDefaultButton (true);
    okButton.setPrefWidth (80);
    //    okButton.setOnAction (e -> startSelectedFunction ());

    cancelButton.setCancelButton (true);
    cancelButton.setPrefWidth (80);
    //    cancelButton.setOnAction (e -> primaryStage.hide ());

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
}