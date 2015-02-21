package com.bytezone.dm3270.application;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
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

public class Console extends Application
{
  private static String CONSOLAS = "Consolas";
  private static String SOURCE_CODE_PRO = "Source Code Pro";
  private static String ANONYMOUS = "Anonymous Pro";
  private static String INCONSOLATA = "Inconsolata";
  private static String MONACO = "Monaco";
  private static String MENLO = "Menlo";
  private static String MPLUS = "M+ 2M";
  private static String PT_MONO = "PT Mono";
  private static String LUCULENT = "Luculent";
  private static String MONOSPACED = "Monospaced";

  private static final int MAINFRAME_EMULATOR_PORT = 5555;

  private ScreenHandler screenHandler;
  private TextField serverName;
  private TextField serverPort;
  private TextField clientPort;
  private TextField filename;
  private CheckBox prevent3270E;

  private Button ok = new Button ("OK");
  private Button cancel = new Button ("Cancel");
  private final ToggleGroup group = new ToggleGroup ();
  private Preferences prefs;
  private final ToggleGroup fontGroup = new ToggleGroup ();
  private final ToggleGroup sizeGroup = new ToggleGroup ();

  private MainframeStage mainframeStage;
  private SpyStage spyStage;
  private ConsoleStage consoleStage;

  @Override
  public void start (Stage dialogStage) throws Exception
  {
    prefs = Preferences.userNodeForPackage (this.getClass ());
    String serverText = prefs.get ("SERVER", "fandezhi.efglobe.com");
    String serverPortText = prefs.get ("SERVER_PORT", "23");
    String clientPortText = prefs.get ("CLIENT_PORT", "2323");
    String fileText = prefs.get ("FILE_NAME", "spy01.txt");
    String optionSelected = prefs.get ("OPTION", "Replay");
    String fontSelected = prefs.get ("FONT", MONOSPACED);
    String sizeSelected = prefs.get ("SIZE", "16");

    //    rw3270.connect ("192.168.0.78", 2300);      // hercules

    String[] optionList = { "Spy", "Replay", "Terminal", "Mainframe" };

    VBox panel = new VBox (10);
    panel.getChildren //
        ().addAll (row ("Mode", options (optionList, group, 0, 2)),
                   row ("", options (optionList, group, 2, 2)),
                   row ("Server", serverName = new TextField (serverText)),
                   row ("Server port", serverPort = new TextField (serverPortText)),
                   row ("Client port", clientPort = new TextField (clientPortText)),
                   row ("Prevent 3270-E", prevent3270E = new CheckBox ()),
                   row ("File name", filename = new TextField (fileText)),
                   row ("", buttons ()));

    HBox hBox = new HBox (10);
    HBox.setMargin (panel, new Insets (10));
    hBox.getChildren ().addAll (panel);

    group.selectedToggleProperty ().addListener (new ChangeListener<Toggle> ()
    {
      @Override
      public void changed (ObservableValue<? extends Toggle> ov, Toggle oldToggle,
          Toggle newToggle)
      {
        if (newToggle != null)
        {
          String button = (String) newToggle.getUserData ();
          switch (button)
          {
            case "Spy":
              serverName.setDisable (false);
              serverPort.setDisable (false);
              clientPort.setDisable (false);
              prevent3270E.setDisable (false);
              filename.setDisable (true);
              break;

            case "Replay":
              serverName.setDisable (true);
              serverPort.setDisable (true);
              clientPort.setDisable (true);
              prevent3270E.setDisable (true);
              filename.setDisable (false);
              break;

            case "Terminal":
              serverName.setDisable (false);
              serverPort.setDisable (false);
              clientPort.setDisable (true);
              prevent3270E.setDisable (true);
              filename.setDisable (true);
              break;

            case "Mainframe":
              serverName.setDisable (true);
              serverPort.setDisable (true);
              clientPort.setDisable (false);
              prevent3270E.setDisable (true);
              filename.setDisable (true);
              break;
          }
        }
      }
    });

    for (int i = 0; i < optionList.length; i++)
    {
      if (optionList[i].equals (optionSelected))
        group.selectToggle (group.getToggles ().get (i));
    }

    ok.setOnAction ( (e) -> {
      // get user values
      String serverTextSave = serverName.getText ();
      String serverPortTextSave = serverPort.getText ();
      String clientPortTextSave = clientPort.getText ();
      String fileTextSave = filename.getText ();
      String optionTextSave = (String) group.getSelectedToggle ().getUserData ();

      // save user values
      prefs.put ("SERVER", serverTextSave);
      prefs.put ("SERVER_PORT", serverPortTextSave);
      prefs.put ("CLIENT_PORT", clientPortTextSave);
      prefs.put ("FILE_NAME", fileTextSave);
      prefs.put ("OPTION", optionTextSave);
      prefs.put ("FONT", ((RadioMenuItem) fontGroup.getSelectedToggle ()).getText ());
      prefs.put ("SIZE", ((RadioMenuItem) sizeGroup.getSelectedToggle ()).getText ());

      dialogStage.hide ();

      screenHandler = createScreenHandler ();       // needs font information
      int serverPortVal = Integer.parseInt (serverPort.getText ());
      int clientPortVal = Integer.parseInt (clientPort.getText ());

      switch (optionTextSave)
      {
        case "Spy":
          spyStage =
              new SpyStage (screenHandler, serverName.getText (), serverPortVal,
                  clientPortVal, prevent3270E.isSelected ());
          spyStage.show ();
          spyStage.startServer ();

          break;

        case "Terminal":
          mainframeStage = new MainframeStage (MAINFRAME_EMULATOR_PORT);
          mainframeStage.show ();
          mainframeStage.startServer ();

          consoleStage =
              new ConsoleStage (screenHandler, "localhost", MAINFRAME_EMULATOR_PORT);
          consoleStage.show ();
          consoleStage.connect ();

          break;

        case "Replay":
          String file =
              System.getProperty ("user.home") + "/Dropbox/Mainframe documentation/"
                  + fileTextSave;
          if (Files.exists (Paths.get (file)))
          {
            new ConsoleStage (screenHandler).show ();
            new ReplayStage (screenHandler, file, this).show ();
          }
          else
            dialogStage.show ();

          break;

        case "Mainframe":
          spyStage =
              new SpyStage (screenHandler, "localhost", MAINFRAME_EMULATOR_PORT,
                  clientPortVal, prevent3270E.isSelected ());
          spyStage.show ();
          spyStage.startServer ();

          mainframeStage = new MainframeStage (MAINFRAME_EMULATOR_PORT);
          mainframeStage.show ();
          mainframeStage.startServer ();

          break;
      }
    });

    cancel.setOnAction ( (e) -> dialogStage.hide ());

    MenuBar menuBar = new MenuBar ();
    Menu menuFile = new Menu ("File");
    Menu menuFont = new Menu ("Fonts");

    MenuItem exit = new MenuItem ("Exit");
    exit.setOnAction (e -> Platform.exit ());

    setFontMenu (CONSOLAS, fontGroup, menuFont, fontSelected);
    setFontMenu (SOURCE_CODE_PRO, fontGroup, menuFont, fontSelected);
    setFontMenu (ANONYMOUS, fontGroup, menuFont, fontSelected);
    setFontMenu (INCONSOLATA, fontGroup, menuFont, fontSelected);
    setFontMenu (LUCULENT, fontGroup, menuFont, fontSelected);
    setFontMenu (MPLUS, fontGroup, menuFont, fontSelected);
    setFontMenu (MENLO, fontGroup, menuFont, fontSelected);
    setFontMenu (PT_MONO, fontGroup, menuFont, fontSelected);
    setFontMenu (MONACO, fontGroup, menuFont, fontSelected);
    setFontMenu (MONOSPACED, fontGroup, menuFont, fontSelected);

    menuFont.getItems ().add (new SeparatorMenuItem ());

    setFontMenu ("14", sizeGroup, menuFont, sizeSelected);
    setFontMenu ("16", sizeGroup, menuFont, sizeSelected);
    setFontMenu ("18", sizeGroup, menuFont, sizeSelected);
    setFontMenu ("20", sizeGroup, menuFont, sizeSelected);
    setFontMenu ("22", sizeGroup, menuFont, sizeSelected);

    menuFile.getItems ().addAll (exit);
    menuBar.getMenus ().addAll (menuFile, menuFont);

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
  }

  private void setFontMenu (String name, ToggleGroup toggleGroup, Menu menu,
      String fontSelected)
  {
    RadioMenuItem item = new RadioMenuItem (name);
    item.setToggleGroup (toggleGroup);
    menu.getItems ().add (item);
    if (fontSelected.equals (name))
      item.setSelected (true);
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
    Label label = new Label (labelText);
    label.setMinWidth (100);
    row.getChildren ().addAll (label, field);
    return row;
  }

  private ScreenHandler createScreenHandler ()
  {
    RadioMenuItem selectedFontName = (RadioMenuItem) fontGroup.getSelectedToggle ();
    RadioMenuItem selectedFontSize = (RadioMenuItem) sizeGroup.getSelectedToggle ();
    Font font =
        Font.font (selectedFontName.getText (),
                   Integer.parseInt (selectedFontSize.getText ()));

    return new ScreenHandler (24, 80, font);
  }

  public static void main (String[] args)
  {
    launch (args);
  }
}