package com.bytezone.dm3270.application;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.bytezone.dm3270.application.SiteListStage.Type;

public class PluginsStage extends Stage
{
  private final Preferences prefs;
  private final List<PluginEntry> plugins = new ArrayList<> ();
  private Button cancelButton, saveButton;

  public PluginsStage (Preferences prefs)
  {
    this.prefs = prefs;
    setTitle ("Plugin Manager");

    readPrefs ();

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
    for (PluginEntry plugin : plugins)
    {
      hbox = new HBox ();
      hbox.setSpacing (5);
      hbox.setPadding (new Insets (0, 5, 0, 5));    // trbl
      for (int i = 0; i < headings.length; i++)
      {
        if (fieldTypes[i] == Type.TEXT || fieldTypes[i] == Type.NUMBER)
        {
          TextField textField = new TextField (plugin.name);
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

  private void readPrefs ()
  {
    for (int i = 0; i < 10; i++)
      plugins.add (new PluginEntry ());
  }

  private void savePrefs ()
  {

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

  private class PluginEntry
  {
    String name = "";
    String className = "";
  }
}