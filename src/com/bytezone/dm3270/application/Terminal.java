package com.bytezone.dm3270.application;

import javafx.application.Application;

public abstract class Terminal extends Application
{
  //  private static final int COMBO_BOX_WIDTH = 150;
  //  private static final int EDIT_BUTTON_WIDTH = 50;
  //  private static final String EDIT_BUTTON_FONT_SIZE = "-fx-font-size: 10;";
  //
  //  private Preferences prefs;
  //  private ConsolePane consolePane;
  //  private WindowSaver windowSaver;
  //  private SiteListStage serverSitesListStage;
  //  private PluginsStage pluginsStage;
  //  private Screen screen;
  //
  //  private String requestedSite = "";
  //  private TelnetState telnetState;
  //
  //  @Override
  //  public void init () throws Exception
  //  {
  //    super.init ();
  //
  //    prefs = Preferences.userNodeForPackage (this.getClass ());
  //    List<String> parms = new ArrayList<> ();
  //
  //    for (String raw : getParameters ().getRaw ())
  //      if ("-reset".equalsIgnoreCase (raw))
  //        prefs.clear ();
  //      else if (raw.startsWith ("-site="))
  //        requestedSite = raw.substring (6);
  //      else if (!raw.startsWith ("-"))
  //        parms.add (raw);
  //      else
  //        System.out.printf ("Unknown argument: %s%n", raw);
  //
  //    if (parms.size () > 0 && requestedSite.isEmpty ())
  //      requestedSite = parms.get (0);
  //
  //    if (false)
  //    {
  //      String[] keys = prefs.keys ();
  //      Arrays.sort (keys);
  //      for (String key : keys)
  //        if (key.matches ("Server.*Name"))
  //          System.out.printf ("%-18s : %s%n", key, prefs.get (key, ""));
  //    }
  //  }
  //
  //  @Override
  //  public void start (Stage primaryStage) throws Exception
  //  {
  //    serverSitesListStage = new SiteListStage (prefs, "Server", 6, true);
  //    pluginsStage = new PluginsStage (prefs);
  //
  //    Site serverSite = null;
  //
  //    if (!requestedSite.isEmpty ())        // using a command line argument
  //    {
  //      Optional<Site> optionalServerSite =
  //          serverSitesListStage.getSelectedSite (requestedSite);
  //      if (optionalServerSite.isPresent ())
  //        serverSite = optionalServerSite.get ();
  //      else
  //        System.out.printf ("Site not found: %s%n", requestedSite);
  //    }
  //
  //    if (serverSite == null)
  //    {
  //      // show request dialog
  //      Optional<Site> optionalServerSite = getServer ();
  //      if (optionalServerSite.isPresent ())
  //        serverSite = optionalServerSite.get ();
  //    }
  //
  //    if (serverSite != null)
  //    {
  //      ScreenDimensions screenDimensions = new ScreenDimensions (24, 80);
  //      screen = new Screen (screenDimensions, prefs, Function.TERMINAL, pluginsStage,
  //          serverSite, telnetState);
  //
  //      consolePane = new ConsolePane (screen, serverSite, pluginsStage);
  //      consolePane.connect ();
  //      Scene scene = new Scene (consolePane);
  //
  //      windowSaver = new WindowSaver (prefs, primaryStage, "Terminal");
  //      if (!windowSaver.restoreWindow ())
  //        primaryStage.centerOnScreen ();
  //
  //      primaryStage.setScene (scene);
  //      primaryStage.setTitle ("dm3270");
  //
  //      scene.setOnKeyPressed (new ConsoleKeyPress (consolePane, screen));
  //      scene.setOnKeyTyped (new ConsoleKeyEvent (screen));
  //
  //      primaryStage.sizeToScene ();
  //      primaryStage.show ();
  //    }
  //    else
  //      System.out.println ("No valid site specified");
  //  }
  //
  //  private Optional<Site> getServer ()
  //  {
  //    String serverSelected = prefs.get ("ServerName", "");
  //
  //    ComboBox<String> serverComboBox = serverSitesListStage.getComboBox ();
  //    serverComboBox.setPrefWidth (COMBO_BOX_WIDTH);
  //    serverComboBox.setVisibleRowCount (5);
  //    serverComboBox.getSelectionModel ().select (serverSelected);
  //
  //    Button editServersButton = serverSitesListStage.getEditButton ();
  //    editServersButton.setStyle (EDIT_BUTTON_FONT_SIZE);
  //    editServersButton.setMinWidth (EDIT_BUTTON_WIDTH);
  //
  //    Label label = new Label ("Select a server ");
  //
  //    Dialog<Site> dialog = new Dialog<> ();
  //
  //    GridPane grid = new GridPane ();
  //    grid.add (label, 1, 1);
  //    grid.add (serverComboBox, 2, 1);
  //    grid.add (editServersButton, 3, 1);
  //    grid.setHgap (10);
  //    grid.setVgap (10);
  //    dialog.getDialogPane ().setContent (grid);
  //
  //    ButtonType btnTypeOK = new ButtonType ("OK", ButtonData.OK_DONE);
  //    ButtonType btnTypeCancel = new ButtonType ("Cancel", ButtonData.CANCEL_CLOSE);
  //
  //    dialog.getDialogPane ().getButtonTypes ().addAll (btnTypeOK, btnTypeCancel);
  //    dialog.setResultConverter (btnType ->
  //    {
  //      if (btnType == btnTypeOK)
  //        return serverSitesListStage.getSelectedSite ().get ();
  //      return null;
  //    });
  //
  //    return dialog.showAndWait ();
  //  }
  //
  //  @Override
  //  public void stop ()
  //  {
  //    if (consolePane != null)
  //      consolePane.disconnect ();
  //
  //    if (windowSaver != null)
  //      windowSaver.saveWindow ();
  //
  //    if (screen != null)
  //      screen.close ();
  //  }
  //
  //  public static void main (String[] args)
  //  {
  //    launch (args);
  //  }
}