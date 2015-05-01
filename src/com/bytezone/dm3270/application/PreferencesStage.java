package com.bytezone.dm3270.application;

import java.util.prefs.Preferences;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public abstract class PreferencesStage extends Stage
{
  protected final Preferences prefs;
  protected Button cancelButton, saveButton;

  public enum Type
  {
    TEXT, NUMBER, BOOLEAN
  }

  public PreferencesStage (Preferences prefs)
  {
    this.prefs = prefs;
  }

  protected Node buttons ()
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