package com.bytezone.dm3270.application;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SiteListStage extends Stage
{
  private final Preferences prefs;
  private final List<Site> sites = new ArrayList<> ();
  private final ComboBox<String> comboBox = new ComboBox<> ();
  private final Button editListButton = new Button ("Edit...");
  private Button cancelButton, saveButton;

  public enum Type
  {
    TEXT, NUMBER, BOOLEAN
  }

  public SiteListStage (Preferences prefs, String key, int max, boolean show3270e)
  {
    this.prefs = prefs;
    setTitle ("Site Manager");

    readPrefs (key, max);

    String[] headings = { key + " name", "URL", "Port", "3270-E" };
    int[] columnWidths = { 150, 150, 50, 50 };
    Type[] fieldTypes = { Type.TEXT, Type.TEXT, Type.NUMBER, Type.BOOLEAN };

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
      heading.setMinWidth (columnWidths[i]);
    }
    vbox.getChildren ().add (hbox);

    // input fields
    for (Site site : sites)
    {
      hbox = new HBox ();
      hbox.setSpacing (5);
      hbox.setPadding (new Insets (0, 5, 0, 5));    // trbl
      for (int i = 0; i < headings.length; i++)
      {
        if (fieldTypes[i] == Type.TEXT || fieldTypes[i] == Type.NUMBER)
        {
          TextField textField = site.getTextField (i);
          textField.setMaxWidth (columnWidths[i]);
          hbox.getChildren ().add (textField);
        }
        else if (fieldTypes[i] == Type.BOOLEAN)
        {
          HBox box = new HBox ();
          CheckBox checkBox = site.getCheckBoxField (i);
          box.setPrefWidth (columnWidths[i]);
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
      savePrefs (key);
      this.hide ();
    });

    cancelButton.setOnAction (e -> this.hide ());
    editListButton.setOnAction (e -> this.show ());
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

      if (port <= 0)
        port = 23;

      Site site = null;
      if (name.isEmpty () || url.isEmpty ())
        site = new Site ("", "", 23, false);
      else
      {
        site = new Site (name, url, port, extended);
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
      boolean extended = site.getExtended ();

      prefs.put (keyName + "Name", name);
      prefs.put (keyName + "URL", site.url.getText ());
      prefs.put (keyName + "Port", site.port.getText ());
      prefs.putBoolean (keyName + "Extended", extended);

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

  Site getSelectedSite ()
  {
    String key = getSelectedName ();
    if (key == null || key.isEmpty ())
      return null;
    for (Site site : sites)
      if (key.equals (site.getName ()))
        return site;
    return null;
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

  private Node buttons ()
  {
    HBox box = new HBox (10);
    saveButton = new Button ("Save");
    saveButton.setDefaultButton (true);
    cancelButton = new Button ("Cancel");
    cancelButton.setCancelButton (true);
    saveButton.setPrefWidth (80);
    cancelButton.setPrefWidth (80);
    box.getChildren ().addAll (cancelButton, saveButton);
    box.setAlignment (Pos.BASELINE_CENTER);
    box.setPadding (new Insets (10, 10, 10, 10));    // trbl
    return box;
  }
}