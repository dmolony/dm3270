package com.bytezone.dm3270.application;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Font;
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
  private static final Site DEFAULT_MAINFRAME = new Site ("mainframe", "localhost",
      MAINFRAME_EMULATOR_PORT, true);
  private final static String OS = System.getProperty ("os.name");
  private final static boolean SYSTEM_MENUBAR = OS != null && OS.startsWith ("Mac");

  private Stage primaryStage;

  private Preferences prefs;

  private final ToggleGroup fontGroup = new ToggleGroup ();
  private final ToggleGroup sizeGroup = new ToggleGroup ();

  private OptionStage optionStage;
  private SpyPane spyPane;
  private ConsolePane consolePane;
  private ReplayStage replayStage;
  private MainframeStage mainframeStage;

  private final MenuBar menuBar = new MenuBar ();

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
      if (raw.equalsIgnoreCase ("-reset"))
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
  public void start (Stage primaryStagex) throws Exception
  {
    this.primaryStage = primaryStagex;
    optionStage = new OptionStage (prefs);

    String fontSelected = prefs.get ("FontName", "");
    String sizeSelected = prefs.get ("FontSize", "16");

    Menu menuFont = new Menu ("Fonts");

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
    String[] menuSizes = { "12", "14", "15", "16", "17", "18", "20", "22" };
    for (String menuSize : menuSizes)
      setMenuItem (menuSize, sizeGroup, menuFont, sizeSelected, false);

    optionStage.okButton.setOnAction (e -> startSelectedFunction ());
    optionStage.cancelButton.setOnAction (e -> primaryStage.hide ());
    optionStage.show ();
  }

  private void startSelectedFunction ()
  {
    primaryStage.hide ();
    String errorMessage = "";

    Site serverSite = optionStage.serverSitesListStage.getSelectedSite ();
    Site clientSite = optionStage.clientSitesListStage.getSelectedSite ();

    String optionText =
        (String) optionStage.functionsGroup.getSelectedToggle ().getUserData ();
    switch (optionText)
    {
      case "Replay":
        Path path =
            Paths
                .get (optionStage.spyFolder + "/" + optionStage.fileComboBox.getValue ());
        if (!Files.exists (path))
          errorMessage = path + " does not exist";
        else
          try
          {
            Screen screen = createScreen (Function.REPLAY);
            Session session = new Session (screen, path);     // can throw Exception
            setConsolePane (screen);                          // reassigns primaryStage

            replayStage = new ReplayStage (session, path, prefs);
            replayStage.show ();
          }
          catch (Exception e)
          {
            e.printStackTrace ();
            errorMessage = "Error reading file";
          }

        break;

      case "Terminal":
        if (serverSite == null)
          errorMessage = "No server selected";
        else
        {
          setConsolePane (createScreen (Function.TERMINAL));
          consolePane.connect (serverSite);
        }

        break;

      case "Spy":
        if (serverSite == null)
          errorMessage = "No server selected";
        else if (clientSite == null)
          errorMessage = "No client selected";
        else
          setSpyPane (createScreen (Function.SPY), serverSite, clientSite);

        break;

      case "Test":
        if (clientSite == null)
          errorMessage = "No client selected";
        else
        {
          setSpyPane (createScreen (Function.TEST), DEFAULT_MAINFRAME, clientSite);
          mainframeStage = new MainframeStage (MAINFRAME_EMULATOR_PORT);
          mainframeStage.show ();
          mainframeStage.startServer ();
        }

        break;
    }

    if (!errorMessage.isEmpty () && showAlert (errorMessage))
      primaryStage.show ();
  }

  private void setConsolePane (Screen screen)
  {
    consolePane = new ConsolePane (screen, menuBar);
    Scene scene = new Scene (consolePane);

    primaryStage.setScene (scene);
    primaryStage.sizeToScene ();
    primaryStage.setX (0);
    primaryStage.setY (0);
    primaryStage.setTitle ("dm3270");

    scene.setOnKeyPressed (new ConsoleKeyPress (consolePane, screen));
    scene.setOnKeyTyped (new ConsoleKeyEvent (screen));

    if (SYSTEM_MENUBAR)
      menuBar.useSystemMenuBarProperty ().set (true);

    if (screen.getFunction () == Function.TERMINAL)
      primaryStage.centerOnScreen ();

    primaryStage.show ();
  }

  private void setSpyPane (Screen screen, Site server, Site client)
  {
    spyPane = new SpyPane (screen, server, client);
    Scene scene = new Scene (spyPane);

    primaryStage.setScene (scene);
    primaryStage.sizeToScene ();
    primaryStage.setX (0);
    primaryStage.setY (0);
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

  private void savePreferences ()
  {
    prefs.put ("Function", (String) optionStage.functionsGroup.getSelectedToggle ()
        .getUserData ());
    prefs.put ("FontName", ((RadioMenuItem) fontGroup.getSelectedToggle ()).getText ());
    prefs.put ("FontSize", ((RadioMenuItem) sizeGroup.getSelectedToggle ()).getText ());
    prefs
        .put ("Mode",
              ((RadioMenuItem) optionStage.releaseGroup.getSelectedToggle ()).getText ());

    String filename = optionStage.fileComboBox.getValue ();
    if (filename != null)
      prefs.put ("ReplayFile", filename);

    prefs.put ("SpyFolder", optionStage.spyFolder);
    prefs.put ("ServerName", optionStage.serverComboBox.getSelectionModel ()
        .getSelectedItem ());
    prefs.put ("ClientName", optionStage.clientComboBox.getSelectionModel ()
        .getSelectedItem ());
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

  private Screen createScreen (Function function)
  {
    RadioMenuItem selectedFontName = (RadioMenuItem) fontGroup.getSelectedToggle ();
    RadioMenuItem selectedFontSize = (RadioMenuItem) sizeGroup.getSelectedToggle ();
    Font font = Font.font (selectedFontName.getText (),     //
                           Integer.parseInt (selectedFontSize.getText ()));
    return new Screen (24, 80, font, function);
  }
}