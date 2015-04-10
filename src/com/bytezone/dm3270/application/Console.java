package com.bytezone.dm3270.application;

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
import javafx.scene.control.CheckBox;
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
import javafx.stage.Stage;

import com.bytezone.dm3270.display.Screen;

public class Console extends Application
{
  private static String[] preferredFontNames = { //
      "Andale Mono", "Anonymous Pro", "Arial", "Consolas", "Courier New",
          "DejaVu Sans Mono", "Inconsolata", "Luculent", "Menlo", "Monaco", "M+ 2m",
          "PT Mono", "Source Code Pro", "Zapfino", "Monospaced" };

  private static final int MAINFRAME_EMULATOR_PORT = 5555;

  private Screen screen;

  private ComboBox<String> fileComboBox;
  private ComboBox<String> serverComboBox;
  private ComboBox<String> clientComboBox;
  private CheckBox prevent3270E;

  Button editServersButton;
  Button editClientsButton;

  private Button okButton = new Button ("OK");
  private Button cancelButton = new Button ("Cancel");
  private final ToggleGroup group = new ToggleGroup ();
  private Preferences prefs;

  private final ToggleGroup fontGroup = new ToggleGroup ();
  private final ToggleGroup sizeGroup = new ToggleGroup ();
  private final ToggleGroup releaseGroup = new ToggleGroup ();

  private MainframeStage mainframeStage;
  private SpyStage spyStage;
  private ConsoleStage consoleStage;
  private ReplayStage replayStage;

  private final String userHome = System.getProperty ("user.home");

  private boolean release;

  private final MenuBar menuBar = new MenuBar ();

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
  public void start (Stage dialogStage) throws Exception
  {
    String fileText = prefs.get ("ReplayFile", "spy01.txt");
    String optionSelected = prefs.get ("Function", "Terminal");
    String fontSelected = prefs.get ("FontName", "");
    String sizeSelected = prefs.get ("FontSize", "16");
    String runMode = prefs.get ("Mode", "Release");

    SiteListStage serverSitesListStage =
        new SiteListStage (prefs, "Server", 5, "Server Sites");
    SiteListStage clientSitesListStage =
        new SiteListStage (prefs, "Client", 5, "Client Sites");

    String[] optionList = { "Spy", "Replay", "Terminal", "Test" };
    Node row1 = options (optionList, group, 0, 2);
    Node row2 = options (optionList, group, 2, 2);

    VBox panel = new VBox (10);

    prevent3270E = new CheckBox ();

    ObservableList<String> sessionFiles = getSessionFiles ();
    fileComboBox = new ComboBox<> (sessionFiles);
    fileComboBox.setVisibleRowCount (12);
    fileComboBox.getSelectionModel ().select (fileText);

    serverComboBox = serverSitesListStage.getComboBox ();
    serverComboBox.setVisibleRowCount (6);
    editServersButton = serverSitesListStage.getEditButton ();
    editServersButton.setStyle ("-fx-font-size: 10;");

    clientComboBox = clientSitesListStage.getComboBox ();
    clientComboBox.setVisibleRowCount (4);
    editClientsButton = clientSitesListStage.getEditButton ();
    editClientsButton.setStyle ("-fx-font-size: 10;");

    int width = 200;
    fileComboBox.setPrefWidth (width / 1.5);
    serverComboBox.setPrefWidth (width / 1.5);
    clientComboBox.setPrefWidth (width / 1.5);

    release = runMode.equals ("Release");
    if (release)
    {
      panel.getChildren ().addAll (row ("Server", serverComboBox, editServersButton),
                                   row ("", buttons ()));
      dialogStage.setTitle ("Connect to Server");
    }
    else
    {
      panel.getChildren //
          ().addAll (row ("Function", row1), row ("", row2),
                     row ("Server", serverComboBox, editServersButton),
                     row ("Client", clientComboBox, editClientsButton),
                     row ("Prevent 3270-E", prevent3270E),
                     row ("Session file", fileComboBox),      //
                     row ("", buttons ()));
      dialogStage.setTitle ("Choose Function");
      if (sessionFiles.size () == 0)
        ((RadioButton) group.getToggles ().get (1)).setDisable (true);
    }

    HBox hBox = new HBox (10);
    HBox.setMargin (panel, new Insets (10));
    hBox.getChildren ().addAll (panel);

    group.selectedToggleProperty ().addListener (new ChangeListener<Toggle> ()
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
            setDisable (false, false, false, true);
            break;

          case "Replay":
            setDisable (true, true, true, false);
            break;

          case "Terminal":
            setDisable (false, true, true, true);
            break;

          case "Test":
            setDisable (true, false, true, true);
            break;
        }
      }
    });

    if (release)
      group.selectToggle (group.getToggles ().get (2));
    else
    {
      boolean found = false;
      for (int i = 0; i < optionList.length; i++)
      {
        if (optionList[i].equals (optionSelected))
        {
          group.selectToggle (group.getToggles ().get (i));
          found = true;
          break;
        }
      }
      if (!found)
        group.selectToggle (group.getToggles ().get (2));
    }

    okButton
        .setOnAction ( (e) -> {

          dialogStage.hide ();

          screen = createScreen ();
          Site serverSite = serverSitesListStage.getSelectedSite ();
          Site clientSite = clientSitesListStage.getSelectedSite ();

          String optionText = (String) group.getSelectedToggle ().getUserData ();
          switch (optionText)
          {
            case "Spy":
              if (serverSite == null)
              {
                Alert alert = new Alert (AlertType.ERROR, "No server selected");
                alert.getDialogPane ().setHeaderText (null);
                Optional<ButtonType> result = alert.showAndWait ();
                if (result.isPresent () && result.get () == ButtonType.OK)
                  dialogStage.show ();
              }
              else if (clientSite == null)
              {
                Alert alert = new Alert (AlertType.ERROR, "No client selected");
                alert.getDialogPane ().setHeaderText (null);
                Optional<ButtonType> result = alert.showAndWait ();
                if (result.isPresent () && result.get () == ButtonType.OK)
                  dialogStage.show ();
              }
              else
              {
                spyStage =
                    new SpyStage (screen, serverSite, clientSite, prevent3270E
                        .isSelected ());
                spyStage.show ();
                spyStage.startServer ();
              }

              break;

            case "Replay":
              String selectedFileName = fileComboBox.getValue ();
              String file =
                  userHome + "/Dropbox/Mainframe documentation/" + selectedFileName;
              Path path = Paths.get (file);
              if (!Files.exists (path))
              {
                file = userHome + "/dm3270/" + selectedFileName;
                path = Paths.get (file);
              }

              if (Files.exists (path))
              {
                consoleStage = new ConsoleStage (screen);
                consoleStage.show ();
                replayStage = new ReplayStage (screen, path, prefs);
                replayStage.show ();
              }
              else
              {
                Alert alert = new Alert (AlertType.ERROR, file + " does not exist");
                alert.getDialogPane ().setHeaderText (null);
                Optional<ButtonType> result = alert.showAndWait ();
                if (result.isPresent () && result.get () == ButtonType.OK)
                  dialogStage.show ();
              }

              break;

            case "Terminal":
              if (serverSite == null)
              {
                Alert alert = new Alert (AlertType.ERROR, "No server selected");
                alert.getDialogPane ().setHeaderText (null);
                Optional<ButtonType> result = alert.showAndWait ();
                if (result.isPresent () && result.get () == ButtonType.OK)
                  dialogStage.show ();
              }
              else
              {
                consoleStage = new ConsoleStage (screen);
                consoleStage.centerOnScreen ();
                consoleStage.show ();
                consoleStage.connect (serverSite);
              }

              break;

            case "Test":
              if (clientSite == null)
              {
                Alert alert = new Alert (AlertType.ERROR, "No client selected");
                alert.getDialogPane ().setHeaderText (null);
                Optional<ButtonType> result = alert.showAndWait ();
                if (result.isPresent () && result.get () == ButtonType.OK)
                  dialogStage.show ();
              }
              else
              {
                Site mainframe =
                    new Site ("mainframe", "localhost", MAINFRAME_EMULATOR_PORT);
                spyStage =
                    new SpyStage (screen, mainframe, clientSite, prevent3270E
                        .isSelected ());
                spyStage.show ();
                spyStage.startServer ();

                mainframeStage = new MainframeStage (MAINFRAME_EMULATOR_PORT);
                mainframeStage.show ();
                mainframeStage.startServer ();
              }

              break;
          }
        });

    cancelButton.setOnAction ( (e) -> dialogStage.hide ());

    Menu menuFont = new Menu ("Fonts");
    Menu menuDebug = new Menu ("Mode");

    List<String> families = Font.getFamilies ();
    //    for (String name : families)
    //      System.out.println (name);
    for (String fontName : preferredFontNames)
    {
      boolean fontExists = families.contains (fontName);
      boolean disable = !fontExists;
      if (fontSelected.isEmpty () && fontExists)
        fontSelected = fontName;
      setMenuItem (fontName, fontGroup, menuFont, fontSelected, disable);
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

    dialogStage.setScene (new Scene (borderPane));
    dialogStage.show ();
  }

  @Override
  public void stop ()
  {
    if (mainframeStage != null)
      mainframeStage.disconnect ();

    if (spyStage != null)
      spyStage.disconnect ();

    if (consoleStage != null)
      consoleStage.disconnect ();

    if (replayStage != null)
      replayStage.disconnect ();

    savePreferences ();
  }

  private ObservableList<String> getSessionFiles ()
  {
    String[] locations = new String[2];
    locations[0] = userHome + "/Dropbox/Mainframe documentation/";
    locations[1] = userHome + "/dm3270/";
    List<Path> files = null;

    for (String filename : locations)
    {
      Path path = Paths.get (filename);
      if (!Files.exists (path) || !Files.isDirectory (path))
        continue;

      try
      {
        files =
            Files.list (path)
                .filter (p -> p.getFileName ().toString ().matches ("spy[0-9]{2}\\.txt"))
                .collect (Collectors.toList ());
        break;
      }
      catch (IOException e)
      {
        e.printStackTrace ();
      }
    }

    if (files == null)
      files = new ArrayList<Path> ();      // empty list

    return FXCollections.observableArrayList (files.stream ()
        .map (p -> p.getFileName ().toString ()).collect (Collectors.toList ()));
  }

  private void savePreferences ()
  {
    prefs.put ("Function", (String) group.getSelectedToggle ().getUserData ());
    prefs.put ("FontName", ((RadioMenuItem) fontGroup.getSelectedToggle ()).getText ());
    prefs.put ("FontSize", ((RadioMenuItem) sizeGroup.getSelectedToggle ()).getText ());
    prefs.put ("Mode", ((RadioMenuItem) releaseGroup.getSelectedToggle ()).getText ());
    prefs.put ("ReplayFile", fileComboBox.getValue ());
  }

  private void setDisable (boolean server, boolean client, boolean pr, boolean fn)
  {
    serverComboBox.setDisable (server);
    editServersButton.setDisable (server);
    clientComboBox.setDisable (client);
    editClientsButton.setDisable (client);
    prevent3270E.setDisable (pr);
    fileComboBox.setDisable (fn);
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
    HBox node = new HBox (10);

    for (int i = offset, max = offset + length; i < max; i++)
    {
      String option = options[i];
      RadioButton rb = new RadioButton (option);
      rb.setUserData (option);
      rb.setPrefWidth (100);
      rb.setToggleGroup (group);
      node.getChildren ().add (rb);
    }
    return node;
  }

  private Node buttons ()
  {
    HBox box = new HBox (10);
    okButton = new Button ("OK");
    okButton.setDefaultButton (true);
    cancelButton = new Button ("Cancel");
    cancelButton.setCancelButton (true);
    okButton.setPrefWidth (80);
    cancelButton.setPrefWidth (80);
    box.getChildren ().addAll (cancelButton, okButton);
    return box;
  }

  private Node row (String labelText, Node... field)
  {
    HBox row = new HBox (10);
    row.setAlignment (Pos.CENTER_LEFT);
    Label label = new Label (labelText);
    label.setMinWidth (100);
    row.getChildren ().add (label);
    for (Node node : field)
      row.getChildren ().add (node);
    return row;
  }

  private Screen createScreen ()
  {
    RadioMenuItem selectedFontName = (RadioMenuItem) fontGroup.getSelectedToggle ();
    RadioMenuItem selectedFontSize = (RadioMenuItem) sizeGroup.getSelectedToggle ();
    Font font =
        Font.font (selectedFontName.getText (),
                   Integer.parseInt (selectedFontSize.getText ()));
    return new Screen (24, 80, font);
  }
}