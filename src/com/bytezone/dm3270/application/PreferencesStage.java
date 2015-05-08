package com.bytezone.dm3270.application;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public abstract class PreferencesStage extends Stage
{
  protected final Preferences prefs;
  protected Button cancelButton, saveButton;
  protected List<PreferenceField> fields = new ArrayList<> ();

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

  public VBox getHeadings ()
  {
    HBox hbox = new HBox ();
    hbox.setSpacing (5);
    hbox.setPadding (new Insets (10, 5, 0, 5));    // trbl

    for (int i = 0; i < fields.size (); i++)
    {
      PreferenceField field = fields.get (i);
      Label heading = new Label (field.heading);
      hbox.getChildren ().add (heading);
      heading.setPrefWidth (field.width);
      if (field.type == Type.BOOLEAN)
        heading.setAlignment (Pos.CENTER);
    }

    VBox vbox = new VBox ();
    vbox.setSpacing (5);
    vbox.setPadding (new Insets (0, 15, 0, 15));    // trbl
    vbox.getChildren ().add (hbox);

    return vbox;
  }

  public class PreferenceField
  {
    public final String heading;
    public final int width;
    public final Type type;

    public PreferenceField (String heading, int width, Type type)
    {
      this.heading = heading;
      this.width = width;
      this.type = type;
    }
  }
}