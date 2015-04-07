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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SiteListStage extends BasicStage
{
  Preferences prefs;
  List<Site> sites = new ArrayList<> ();
  ComboBox<String> comboBox;
  Button cancelButton, saveButton;

  public SiteListStage (Preferences prefs, String key, int max, String windowTitle)
  {
    this.prefs = prefs;
    setTitle (windowTitle);

    readPrefs (key, max);

    VBox vbox = new VBox ();
    vbox.setSpacing (5);
    vbox.setPadding (new Insets (5, 5, 5, 5));    // trbl

    HBox titleBox = new HBox ();
    titleBox.getChildren ().addAll (new Label ("Site name"), new Label ("URL"),
                                    new Label ("Port"));
    vbox.getChildren ().add (titleBox);

    for (Site site : sites)
    {
      HBox hbox = new HBox ();
      hbox.setSpacing (15);
      hbox.setPadding (new Insets (0, 5, 0, 5));    // trbl
      hbox.getChildren ().addAll (site.name, site.url, site.port);
      vbox.getChildren ().add (hbox);
    }

    List<String> list = new ArrayList<> ();
    for (Site site : sites)
      list.add (site.getName ());
    ObservableList<String> observableList = FXCollections.observableList (list);
    comboBox = new ComboBox<> (observableList);

    //set previous selection

    BorderPane borderPane = new BorderPane ();
    borderPane.setCenter (vbox);
    borderPane.setBottom (buttons ());

    Scene scene = new Scene (borderPane);
    setScene (scene);

    saveButton.setOnAction ( (e) -> {
      savePrefs (key);
      this.hide ();
    });

    cancelButton.setOnAction ( (e) -> this.hide ());
  }

  private void readPrefs (String key, int max)
  {
    int count = 0;
    while (count < max)
    {
      String keyName = String.format ("%s%02d", key, count++);

      String name = prefs.get (keyName + "Name", "");
      String url = prefs.get (keyName + "URL", "");
      int port = prefs.getInt (keyName + "Port", 23);

      if (port <= 0)
        port = 23;

      if (name.isEmpty () || url.isEmpty ())
        sites.add (new Site ("", "", 23));
      else
        sites.add (new Site (name, url, port));
    }
  }

  private void savePrefs (String key)
  {
    for (int i = 0; i < sites.size (); i++)
    {
      Site site = sites.get (i);
      String keyName = String.format ("%s%02d", key, i);
      prefs.put (keyName + "Name", site.name.getText ());
      prefs.put (keyName + "URL", site.url.getText ());
      prefs.put (keyName + "Port", site.port.getText ());
    }
  }

  Site getSelected ()
  {
    return null;
  }

  ComboBox<String> getComboBox ()
  {
    return comboBox;
  }

  private Node buttons ()
  {
    HBox box = new HBox (10);
    saveButton = new Button ("OK");
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