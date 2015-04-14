package com.bytezone.dm3270.application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class BasicStage extends Stage
{
  protected static final boolean DONT_PROCESS = false;
  protected static final boolean DO_PROCESS = true;

  protected HBox getHBox ()
  {
    HBox hbox = new HBox ();
    hbox.setSpacing (15);
    hbox.setPadding (new Insets (10, 10, 10, 10));    // trbl
    hbox.setAlignment (Pos.CENTER_LEFT);
    return hbox;
  }

  protected VBox getVBox ()
  {
    VBox vbox = new VBox ();
    vbox.setSpacing (15);
    vbox.setPadding (new Insets (10, 10, 10, 10));    // trbl
    return vbox;
  }

  protected Button getButton (String name, VBox vbox, int width)
  {
    Button button = new Button (name);
    button.setPrefWidth (width);
    vbox.getChildren ().add (button);
    button.setDisable (true);
    return button;
  }

  protected Button getButton (String name, HBox hbox, int width)
  {
    Button button = new Button (name);
    button.setPrefWidth (width);
    hbox.getChildren ().add (button);
    button.setDisable (true);
    return button;
  }

  protected TextArea getTextArea (int width)
  {
    TextArea textArea = new TextArea ();
    textArea.setEditable (false);
    textArea.setFont (Font.font ("Monospaced", 12));
    textArea.setPrefWidth (width);
    return textArea;
  }

  protected Tab getTab (String name, TextArea textArea)
  {
    Tab tab = new Tab ();
    tab.setText (name);
    tab.setContent (textArea);
    return tab;
  }

  protected RadioButton getRadioButton (String text, HBox hbox, ToggleGroup group)
  {
    RadioButton button = new RadioButton (text);
    hbox.getChildren ().add (button);
    button.setToggleGroup (group);
    return button;
  }
}