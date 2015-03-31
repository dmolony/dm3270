package com.bytezone.dm3270.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import javafx.scene.control.TextField;
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
  private static String[] fontNames = { //
      "Consolas", "Source Code Pro", "Anonymous Pro", "Inconsolata", "Monaco", "Menlo",
          "M+ 2m", "PT Mono", "Luculent", "DejaVu Sans Mono", "Monospaced" };

  private static final int MAINFRAME_EMULATOR_PORT = 5555;

  private Screen screen;
  private TextField serverName;
  private TextField serverPort;
  private TextField clientPort;
  private ComboBox<String> filenameList;
  private CheckBox prevent3270E;

  private Button ok = new Button ("OK");
  private Button cancel = new Button ("Cancel");
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
  public void start (Stage dialogStage) throws Exception
  {
    prefs = Preferences.userNodeForPackage (this.getClass ());
    String serverText = prefs.get ("SERVER", "your.server.com");
    String serverPortText = prefs.get ("SERVER_PORT", "23");
    String clientPortText = prefs.get ("CLIENT_PORT", "2323");
    String fileText = prefs.get ("FILE_NAME", "spy01.txt");
    String optionSelected = prefs.get ("OPTION", "Replay");
    String fontSelected = prefs.get ("FONT", "Monospaced");
    String sizeSelected = prefs.get ("SIZE", "16");
    String runMode = prefs.get ("RUNMODE", "Release");

    String[] optionList = { "Spy", "Replay", "Terminal", "Mainframe" };

    VBox panel = new VBox (10);

    serverName = new TextField (serverText);
    serverPort = new TextField (serverPortText);
    clientPort = new TextField (clientPortText);
    prevent3270E = new CheckBox ();
    filenameList = new ComboBox<> (getSessionFiles ());

    filenameList.setVisibleRowCount (12);
    filenameList.getSelectionModel ().select (fileText);

    int width = 200;
    filenameList.setPrefWidth (width / 1.5);
    serverName.setPrefWidth (width);
    serverPort.setPrefWidth (width / 3);
    clientPort.setPrefWidth (width / 3);

    release = runMode.equals ("Release");
    if (release)
    {
      panel.getChildren ().addAll (row ("Server URL", serverName),
                                   row ("Server port", serverPort), //
                                   row ("", buttons ()));
      dialogStage.setTitle ("Connect to Server");
    }
    else
    {
      panel.getChildren //
          ().addAll (row ("Function", options (optionList, group, 0, 2)),
                     row ("", options (optionList, group, 2, 2)),
                     row ("Server URL", serverName),          //
                     row ("Server port", serverPort),         //
                     row ("Client port", clientPort),
                     row ("Prevent 3270-E", prevent3270E),
                     row ("Session file", filenameList),      //
                     row ("", buttons ()));
      dialogStage.setTitle ("Choose Function");
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
            setDisable (false, false, false, false, true);
            break;

          case "Replay":
            setDisable (true, true, true, true, false);
            break;

          case "Terminal":
            setDisable (false, false, true, true, true);
            break;

          case "Mainframe":
            setDisable (true, true, false, true, true);
            break;
        }
      }
    });

    if (release)
    {
      //      group.selectToggle (group.getToggles ().get (2));
    }
    else
      for (int i = 0; i < optionList.length; i++)
      {
        if (optionList[i].equals (optionSelected))
          group.selectToggle (group.getToggles ().get (i));
      }

    ok.setOnAction ( (e) -> {

      dialogStage.hide ();

      screen = createScreen ();
      int serverPortVal = Integer.parseInt (serverPort.getText ());
      int clientPortVal = Integer.parseInt (clientPort.getText ());

      String optionTextSave = "Terminal";
      if (!release)
        optionTextSave = (String) group.getSelectedToggle ().getUserData ();

      switch (optionTextSave)
      {
        case "Spy":
          spyStage =
              new SpyStage (screen, serverName.getText (), serverPortVal, clientPortVal,
                  prevent3270E.isSelected ());
          spyStage.show ();
          spyStage.startServer ();

          break;

        case "Replay":
          String selectedFileName = filenameList.getValue ();
          String file = userHome + "/Dropbox/Mainframe documentation/" + selectedFileName;
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

        case "Mainframe":
          spyStage =
              new SpyStage (screen, "localhost", MAINFRAME_EMULATOR_PORT, clientPortVal,
                  prevent3270E.isSelected ());
          spyStage.show ();
          spyStage.startServer ();

          mainframeStage = new MainframeStage (MAINFRAME_EMULATOR_PORT);
          mainframeStage.show ();
          mainframeStage.startServer ();

          break;

        case "Terminal":
          consoleStage = new ConsoleStage (screen);
          consoleStage.centerOnScreen ();
          consoleStage.show ();
          consoleStage.connect (serverName.getText (), serverPortVal);

          break;
      }
    });

    cancel.setOnAction ( (e) -> dialogStage.hide ());

    Menu menuFont = new Menu ("Fonts");
    Menu menuDebug = new Menu ("Debug");

    List<String> families = Font.getFamilies ();
    for (String fontName : fontNames)
    {
      boolean disable = !families.contains (fontName);
      setMenuItem (fontName, fontGroup, menuFont, fontSelected, disable);
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
    prefs.put ("SERVER", serverName.getText ());
    prefs.put ("SERVER_PORT", serverPort.getText ());
    prefs.put ("CLIENT_PORT", clientPort.getText ());
    prefs.put ("FILE_NAME", filenameList.getValue ());
    if (!release)
      prefs.put ("OPTION", (String) group.getSelectedToggle ().getUserData ());
    prefs.put ("FONT", ((RadioMenuItem) fontGroup.getSelectedToggle ()).getText ());
    prefs.put ("SIZE", ((RadioMenuItem) sizeGroup.getSelectedToggle ()).getText ());
    prefs.put ("RUNMODE", ((RadioMenuItem) releaseGroup.getSelectedToggle ()).getText ());
  }

  private void setDisable (boolean sn, boolean sp, boolean cp, boolean pr, boolean fn)
  {
    serverName.setDisable (sn);
    serverPort.setDisable (sp);
    clientPort.setDisable (cp);
    prevent3270E.setDisable (pr);
    filenameList.setDisable (fn);
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
    ok = new Button ("OK");
    ok.setDefaultButton (true);
    cancel = new Button ("Cancel");
    cancel.setCancelButton (true);
    ok.setPrefWidth (80);
    cancel.setPrefWidth (80);
    box.getChildren ().addAll (cancel, ok);
    return box;
  }

  private Node row (String labelText, Node field)
  {
    HBox row = new HBox (10);
    row.setAlignment (Pos.CENTER_LEFT);
    Label label = new Label (labelText);
    label.setMinWidth (100);
    row.getChildren ().addAll (label, field);
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

  public static void main (String[] args)
  {
    launch (args);
  }
}