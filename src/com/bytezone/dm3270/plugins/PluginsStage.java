package com.bytezone.dm3270.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
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
import com.bytezone.dm3270.application.Site;
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
  private final MenuItem editMenuItem = new MenuItem ("Plugin Manager...");
  private int requestMenus;
  private int baseMenuSize;
  private Screen screen;

  public PluginsStage (Preferences prefs)
  {
    super (prefs);
    setTitle ("Plugin Manager");

    readPrefs ();

    editMenuItem.setOnAction (e -> this.show ());

    fields.add (new PreferenceField ("Menu entry", 130, Type.TEXT));
    fields.add (new PreferenceField ("Class name", 300, Type.TEXT));
    fields.add (new PreferenceField ("Active", 60, Type.BOOLEAN));

    VBox vbox = getHeadings ();

    // input fields
    for (PluginEntry pluginEntry : plugins)
    {
      HBox hbox = new HBox ();
      hbox.setSpacing (5);
      hbox.setPadding (new Insets (0, 5, 0, 5));    // trbl

      for (int i = 0; i < fields.size (); i++)
      {
        PreferenceField field = fields.get (i);
        if (field.type == Type.TEXT || field.type == Type.NUMBER)
        {
          TextField textField = pluginEntry.getTextField (i);
          textField.setPrefWidth (field.width);
          hbox.getChildren ().add (textField);
        }
        else if (field.type == Type.BOOLEAN)
        {
          HBox box = new HBox ();
          CheckBox checkBox = pluginEntry.getCheckBox (i);
          box.setPrefWidth (field.width);
          box.setAlignment (Pos.CENTER);
          box.getChildren ().add (checkBox);
          hbox.getChildren ().add (box);
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
            //     if (result == null)
            //       result = pluginResult;      // only the first result is processed
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

  public Menu getMenu (Site site)
  {
    setMenu (site);
    return menu;
  }

  public MenuItem getEditMenuItem ()
  {
    return editMenuItem;
  }

  public boolean allowsPlugins ()
  {
    return true;
  }

  private void setMenu (Site site)
  {
    menu = new Menu ("Plugins");

    int activeCount = 0;
    for (PluginEntry pluginEntry : plugins)
    {
      String text = pluginEntry.name.getText ();
      if (text.isEmpty ())
        continue;

      CheckMenuItem menuItem = new CheckMenuItem (text);
      menu.getItems ().add (menuItem);
      pluginEntry.instantiate ();

      if (pluginEntry.plugin == null)
        menuItem.setDisable (true);
      else
      {
        menuItem.setUserData (pluginEntry);
        menuItem.setOnAction (e -> itemSelected (e));
        if (pluginEntry.isAutoActivate ())
        {
          activeCount++;
          menuItem.setSelected (true);
          pluginEntry.select (true);
        }
      }
    }

    baseMenuSize = menu.getItems ().size ();

    if (activeCount > 0)
    {
      menu.getItems ().add (new SeparatorMenuItem ());
      for (PluginEntry pluginEntry : plugins)
        if (pluginEntry.isAutoActivate () && pluginEntry.plugin.doesRequest ())
          menu.getItems ().add (pluginEntry.requestMenuItem);
    }
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
    System.out.println ("rebuilding");
    ObservableList<MenuItem> items = menu.getItems ();
    while (items.size () > baseMenuSize)
      items.remove (menu.getItems ().size () - 1);
    menu.getItems ().add (new SeparatorMenuItem ());

    for (PluginEntry pluginEntry : plugins)
      if (pluginEntry.isActivated && pluginEntry.requestMenuItem != null)
        items.add (pluginEntry.requestMenuItem);
  }

  private void readPrefs ()
  {
    for (int i = 0; i < MAX_PLUGINS; i++)
    {
      String pluginName = prefs.get (String.format ("PluginName-%02d", i), "");
      String pluginClass = prefs.get (String.format ("PluginClass-%02d", i), "");
      boolean pluginActivate =
          prefs.getBoolean (String.format ("PluginActivate-%02d", i), false);
      plugins.add (new PluginEntry (pluginName, pluginClass, pluginActivate));
    }
  }

  private void savePrefs ()
  {
    for (int i = 0; i < MAX_PLUGINS; i++)
    {
      PluginEntry plugin = plugins.get (i);
      prefs.put (String.format ("PluginName-%02d", i), plugin.name.getText ());
      prefs.put (String.format ("PluginClass-%02d", i), plugin.className.getText ());
      prefs.putBoolean (String.format ("PluginActivate-%02d", i),
                        plugin.activate.isSelected ());
    }
  }

  private class PluginEntry
  {
    private final TextField name = new TextField ();
    private final TextField className = new TextField ();
    private final CheckBox activate = new CheckBox ();

    private final TextField[] textFieldList = { name, className, null };
    private final CheckBox[] checkBoxList = { null, null, activate };

    private Plugin plugin;
    private boolean isActivated;
    private MenuItem requestMenuItem;          // used to trigger a Request

    public PluginEntry (String name, String className, boolean activate)
    {
      this.name.setText (name);
      this.className.setText (className);
      this.activate.setSelected (activate);
    }

    public TextField getTextField (int index)
    {
      return textFieldList[index];
    }

    public CheckBox getCheckBox (int index)
    {
      return checkBoxList[index];
    }

    public boolean isAutoActivate ()
    {
      return activate.isSelected ();
    }

    public void select (boolean activate)
    {
      if (activate)
        plugin.activate ();
      else
        plugin.deactivate ();

      if (requestMenuItem == null && plugin.doesRequest ())
      {
        requestMenuItem = new MenuItem (name.getText ());
        requestMenuItem.setOnAction (e -> screen.processPluginRequest (plugin));
        requestMenuItem.setAccelerator (new KeyCodeCombination (keyCodes[requestMenus++],
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