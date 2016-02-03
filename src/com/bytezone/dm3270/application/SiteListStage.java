package com.bytezone.dm3270.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

import com.bytezone.dm3270.utilities.PreferencesStage;
import com.bytezone.dm3270.utilities.Site;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SiteListStage extends PreferencesStage
{
  private final List<Site> sites = new ArrayList<> ();
  private final ComboBox<String> comboBox = new ComboBox<> ();
  private final Button editListButton = new Button ("Edit...");

  public SiteListStage (Preferences prefs, String key, int max, boolean show3270e)
  {
    super (prefs);

    setTitle ("Site Manager");

    readPrefs (key, max);

    fields.add (new PreferenceField (key + " name", 150, Type.TEXT));
    fields.add (new PreferenceField ("URL", 150, Type.TEXT));
    fields.add (new PreferenceField ("Port", 50, Type.NUMBER));
    fields.add (new PreferenceField ("Ext", 50, Type.BOOLEAN));
    fields.add (new PreferenceField ("Model", 40, Type.NUMBER));
    fields.add (new PreferenceField ("Plugins", 50, Type.BOOLEAN));
    fields.add (new PreferenceField ("Save folder", 80, Type.TEXT));

    VBox vbox = getHeadings ();

    // input fields
    for (Site site : sites)
    {
      HBox hbox = new HBox ();
      hbox.setSpacing (5);
      hbox.setPadding (new Insets (0, 5, 0, 5));    // trbl

      for (int i = 0; i < fields.size (); i++)
      {
        PreferenceField field = fields.get (i);
        if (field.type == Type.TEXT || field.type == Type.NUMBER)
        {
          TextField textField = site.getTextField (i);
          textField.setMaxWidth (field.width);
          hbox.getChildren ().add (textField);
        }
        else if (field.type == Type.BOOLEAN)
        {
          HBox box = new HBox ();
          CheckBox checkBox = site.getCheckBoxField (i);
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

    saveButton.setOnAction (e -> save (key));
    cancelButton.setOnAction (e -> this.hide ());
    editListButton.setOnAction (e -> this.show ());
  }

  private void save (String key)
  {
    savePrefs (key);
    this.hide ();
  }

  private void readPrefs (String key, int max)
  {
    List<String> siteNames = new ArrayList<> ();
    int count = 0;

    while (count < max)
    {
      String keyName = String.format ("%s%02d", key, count++);

      String name = prefs.get (keyName + "Name", "");
      String url = prefs.get (keyName + "URL", "");
      int port = prefs.getInt (keyName + "Port", 23);
      boolean extended = prefs.getBoolean (keyName + "Extended", true);
      int model = prefs.getInt (keyName + "Model", 2);
      boolean plugins = prefs.getBoolean (keyName + "Plugins", false);
      String folder = prefs.get (keyName + "Folder", "");

      if (port <= 0)
        port = 23;
      if (model < 2 || model > 5)
        model = 2;

      Site site = null;
      if (name.isEmpty () || url.isEmpty ())
        site = new Site ("", "", 23, false, 2, false, "");
      else
      {
        site = new Site (name, url, port, extended, model, plugins, folder);
        siteNames.add (name);
      }
      sites.add (site);
    }

    updateComboBox (siteNames, 0);
  }

  private void savePrefs (String key)
  {
    int selectedIndex = getSelectedIndex ();
    List<String> siteNames = new ArrayList<> ();

    for (int i = 0; i < sites.size (); i++)
    {
      Site site = sites.get (i);
      String keyName = String.format ("%s%02d", key, i);
      String name = site.name.getText ();
      String folder = site.folder.getText ();
      boolean extended = site.getExtended ();
      boolean plugins = site.getPlugins ();

      prefs.put (keyName + "Name", name);
      prefs.put (keyName + "URL", site.url.getText ());
      prefs.put (keyName + "Port", site.port.getText ());
      prefs.putBoolean (keyName + "Extended", extended);
      prefs.put (keyName + "Model", site.model.getText ());
      prefs.putBoolean (keyName + "Plugins", plugins);
      prefs.put (keyName + "Folder", folder);

      if (name != null && !name.isEmpty ())
        siteNames.add (name);
    }

    if (selectedIndex >= 0 && selectedIndex < siteNames.size ())
      updateComboBox (siteNames, selectedIndex);
    else if (sites.size () > 0)
      updateComboBox (siteNames, 0);
  }

  private void updateComboBox (List<String> names, int selectedIndex)
  {
    ObservableList<String> ol = FXCollections.observableArrayList (names);
    if (ol != null)
    {
      comboBox.setItems (ol);
      comboBox.getSelectionModel ().select (selectedIndex);
    }
  }

  Optional<Site> getSelectedSite ()
  {
    String key = getSelectedName ();
    if (key == null || key.isEmpty ())
      return Optional.empty ();
    for (Site site : sites)
      if (key.equals (site.getName ()))
        return Optional.of (site);
    return Optional.empty ();
  }

  Optional<Site> getSelectedSite (String siteName)
  {
    for (Site site : sites)
      if (siteName.equals (site.getName ()))
        return Optional.of (site);

    return Optional.empty ();
  }

  List<Site> getSites ()
  {
    return sites;
  }

  String getSelectedName ()
  {
    return comboBox.getSelectionModel ().getSelectedItem ();
  }

  int getSelectedIndex ()
  {
    return comboBox.getSelectionModel ().getSelectedIndex ();
  }

  ComboBox<String> getComboBox ()
  {
    return comboBox;
  }

  Button getEditButton ()
  {
    return editListButton;
  }
}