package com.bytezone.dm3270.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.bytezone.dm3270.application.PreferencesStage;
import com.bytezone.dm3270.display.Screen;

public class PluginsStage extends PreferencesStage
{
  private static final int MAX_PLUGINS = 10;
  private static KeyCode[] keyCodes = { KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3,
                                       KeyCode.DIGIT4, KeyCode.DIGIT5, KeyCode.DIGIT6,
                                       KeyCode.DIGIT7, KeyCode.DIGIT8, KeyCode.DIGIT9,
                                       KeyCode.DIGIT0 };

  private final List<PluginEntry> plugins = new ArrayList<> (MAX_PLUGINS);
  private Menu menu;
  private int requestMenus;
  private int baseMenuSize;
  private Screen screen;

  public PluginsStage (Preferences prefs)
  {
    super (prefs);
    setTitle ("Plugin Manager");

    readPrefs ();
    setMenu ();

    String[] headings = { "Menu entry", "Class name" };
    int[] columnWidths = { 130, 300 };
    Type[] fieldTypes = { Type.TEXT, Type.TEXT };

    VBox vbox = new VBox ();
    vbox.setSpacing (5);
    vbox.setPadding (new Insets (0, 15, 0, 15));    // trbl

    // headings
    HBox hbox = new HBox ();
    hbox.setSpacing (5);
    hbox.setPadding (new Insets (10, 5, 0, 5));    // trbl

    for (int i = 0; i < headings.length; i++)
    {
      Label heading = new Label (headings[i]);
      hbox.getChildren ().add (heading);
      heading.setPrefWidth (columnWidths[i]);
    }

    vbox.getChildren ().add (hbox);

    // input fields
    for (PluginEntry pluginEntry : plugins)
    {
      hbox = new HBox ();
      hbox.setSpacing (5);
      hbox.setPadding (new Insets (0, 5, 0, 5));    // trbl

      for (int i = 0; i < headings.length; i++)
      {
        if (fieldTypes[i] == Type.TEXT || fieldTypes[i] == Type.NUMBER)
        {
          TextField textField = pluginEntry.getTextField (i);
          textField.setPrefWidth (columnWidths[i]);
          hbox.getChildren ().add (textField);
        }
      }
      vbox.getChildren ().add (hbox);
    }

    BorderPane borderPane = new BorderPane ();
    borderPane.setCenter (vbox);
    borderPane.setBottom (buttons ());

    Scene scene = new Scene (borderPane);
    setScene (scene);

    saveButton.setOnAction (e -> {
      savePrefs ();
      this.hide ();
    });

    cancelButton.setOnAction (e -> this.hide ());
  }

  public void setScreen (Screen screen)
  {
    this.screen = screen;
  }

  public void processAll (PluginData pluginScreen)
  {
    for (PluginEntry pluginEntry : plugins)
      if (pluginEntry.isActivated)
      {
        Plugin plugin = pluginEntry.plugin;
        if (plugin != null && plugin.doesAuto ())
          try
          {
            plugin.processAuto (pluginScreen);
            //            if (result == null)
            //              result = pluginResult;      // only the first result is processed
          }
          catch (Exception e)
          {
            e.printStackTrace ();
          }
      }
  }

  public int activePlugins ()
  {
    int activePlugins = 0;
    for (PluginEntry pluginEntry : plugins)
    {
      Plugin plugin = pluginEntry.plugin;
      if (plugin != null && pluginEntry.isActivated)
        ++activePlugins;
    }
    return activePlugins;
  }

  public Menu getMenu ()
  {
    return menu;
  }

  private void setMenu ()
  {
    menu = new Menu ("Plugins");

    MenuItem itemEditPlugins = new MenuItem ("Edit plugins");
    itemEditPlugins.setOnAction (e -> show ());
    menu.getItems ().addAll (itemEditPlugins, new SeparatorMenuItem ());

    for (PluginEntry pluginEntry : plugins)
    {
      String text = pluginEntry.name.getText ();
      if (!text.isEmpty ())
      {
        CheckMenuItem menuItem = new CheckMenuItem (text);
        menu.getItems ().add (menuItem);
        pluginEntry.instantiate ();
        menuItem.setDisable (pluginEntry.plugin == null);
        menuItem.setUserData (pluginEntry);
        menuItem.setOnAction (e -> itemSelected (e));
      }
    }
    baseMenuSize = menu.getItems ().size ();
  }

  private void itemSelected (ActionEvent e)
  {
    CheckMenuItem menuItem = ((CheckMenuItem) e.getSource ());
    PluginEntry pluginEntry = (PluginEntry) menuItem.getUserData ();
    pluginEntry.select (menuItem.isSelected ());
    rebuildMenu ();
  }

  private void rebuildMenu ()
  {
    ObservableList<MenuItem> items = menu.getItems ();
    while (items.size () > baseMenuSize)
      items.remove (menu.getItems ().size () - 1);
    menu.getItems ().add (new SeparatorMenuItem ());

    for (PluginEntry pluginEntry : plugins)
      if (pluginEntry.isActivated && pluginEntry.menuItem != null)
        items.add (pluginEntry.menuItem);
  }

  private void readPrefs ()
  {
    for (int i = 0; i < MAX_PLUGINS; i++)
    {
      String pluginName = prefs.get (String.format ("PluginName-%02d", i), "");
      String pluginClass = prefs.get (String.format ("PluginClass-%02d", i), "");
      plugins.add (new PluginEntry (pluginName, pluginClass));
    }
  }

  private void savePrefs ()
  {
    for (int i = 0; i < MAX_PLUGINS; i++)
    {
      PluginEntry plugin = plugins.get (i);
      prefs.put (String.format ("PluginName-%02d", i), plugin.name.getText ());
      prefs.put (String.format ("PluginClass-%02d", i), plugin.className.getText ());
    }
  }

  private class PluginEntry
  {
    private final TextField name = new TextField ();
    private final TextField className = new TextField ();
    private final TextField[] textFieldList = { name, className };
    private Plugin plugin;
    private boolean isActivated;
    private MenuItem menuItem;          // used to trigger a Request

    public PluginEntry (String name, String className)
    {
      this.name.setText (name);
      this.className.setText (className);
    }

    public TextField getTextField (int index)
    {
      return textFieldList[index];
    }

    public void select (boolean activate)
    {
      if (activate)
        plugin.activate ();
      else
        plugin.deactivate ();

      if (menuItem == null && plugin.doesRequest ())
      {
        menuItem = new MenuItem (name.getText ());
        menuItem.setOnAction (e -> screen.processPluginRequest (plugin));
        menuItem.setAccelerator (new KeyCodeCombination (keyCodes[requestMenus++],
            KeyCombination.SHORTCUT_DOWN));
      }

      isActivated = activate;
    }

    public Plugin instantiate ()
    {
      try
      {
        plugin = null;
        String classNameText = className.getText ();
        if (!classNameText.isEmpty ())
        {
          Class<?> c = Class.forName (classNameText);
          if (c != null)
            plugin = (Plugin) c.newInstance ();
        }
      }
      catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
      {
      }
      return plugin;
    }
  }
}