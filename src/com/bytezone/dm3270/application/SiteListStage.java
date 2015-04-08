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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SiteListStage extends BasicStage
{
  private final Preferences prefs;
  private final List<Site> sites = new ArrayList<> ();
  private final ComboBox<String> comboBox = new ComboBox<> ();
  private final Button editListButton = new Button ("Edit...");
  private Button cancelButton, saveButton;

  public SiteListStage (Preferences prefs, String key, int max, String windowTitle)
  {
    this.prefs = prefs;
    setTitle (windowTitle);

    readPrefs (key, max);

    String[] headings = { "Site name", "URL", "Port" };
    int[] columnWidths = { 100, 150, 50, 100 };

    HBox hbox = new HBox ();
    hbox.setSpacing (5);
    hbox.setPadding (new Insets (0, 15, 0, 15));    // trbl

    for (int i = 0; i < headings.length; i++)
    {
      VBox vbox = new VBox ();
      vbox.setSpacing (5);
      vbox.setPadding (new Insets (5, 5, 5, 5));    // trbl

      Label heading = new Label (headings[i]);
      vbox.getChildren ().add (heading);

      for (Site site : sites)
      {
        TextField textField = site.getTextField (i);
        textField.setMaxWidth (columnWidths[i]);
        vbox.getChildren ().add (textField);
      }

      hbox.getChildren ().add (vbox);
    }

    BorderPane borderPane = new BorderPane ();
    borderPane.setCenter (hbox);
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
    int selectedIndex = prefs.getInt (String.format ("%sSelected", key), 0);
    int count = 0;
    while (count < max)
    {
      String keyName = String.format ("%s%02d", key, count++);

      String name = prefs.get (keyName + "Name", "");
      String url = prefs.get (keyName + "URL", "");
      int port = prefs.getInt (keyName + "Port", 23);

      if (port <= 0)
        port = 23;

      Site site = null;
      if (name.isEmpty () || url.isEmpty ())
        site = new Site ("", "", 23);
      else
      {
        site = new Site (name, url, port);
        siteNames.add (name);
      }
      sites.add (site);
    }

    updateComboBox (siteNames, selectedIndex);
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
      prefs.put (keyName + "Name", name);
      prefs.put (keyName + "URL", site.url.getText ());
      prefs.put (keyName + "Port", site.port.getText ());
      if (name != null && !name.isEmpty ())
        siteNames.add (name);
    }

    if (selectedIndex >= 0 && selectedIndex < sites.size ())
      updateComboBox (siteNames, selectedIndex);
    else if (sites.size () > 0)
      updateComboBox (siteNames, 0);

    prefs.putInt (String.format ("%sSelected", key), getSelectedIndex ());
  }

  private void updateComboBox (List<String> names, int selectedIndex)
  {
    ObservableList<String> ol = FXCollections.observableArrayList (names);
    if (ol != null)
      comboBox.setItems (ol);
    comboBox.getSelectionModel ().select (selectedIndex);
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