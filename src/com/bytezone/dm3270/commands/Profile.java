package com.bytezone.dm3270.commands;

import java.util.Optional;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Profile
{
  private final Dialog<String> dialog = new Dialog<> ();
  private final GridPane grid = new GridPane ();
  private final Font labelFont = Font.font ("Monospaced", 13);
  private final Font boldFont = Font.font ("Monospaced", FontWeight.BOLD, 13);
  private final ButtonType btnTypeOK = new ButtonType ("OK", ButtonData.OK_DONE);
  private String prefix = "";

  public Profile (String profileMessageText1, String profileMessageText2)
  {
    grid.setPadding (new Insets (10, 40, 10, 30));
    grid.setHgap (10);
    grid.setVgap (5);

    String[] tokens = profileMessageText1.split ("\\s+");

    int row = 1;
    for (String token : tokens)
    {
      Label label = new Label (token);
      label.setFont (labelFont);
      grid.add (label, 1, row++);

      if (token.startsWith ("PREFIX(") && token.endsWith (")"))
      {
        prefix = token.substring (7, token.length () - 1);
        label.setFont (boldFont);
      }
    }

    dialog.setTitle ("Profile");
    dialog.getDialogPane ().getButtonTypes ().addAll (btnTypeOK);
    dialog.getDialogPane ().setContent (grid);
  }

  public Optional<String> showAndWait ()
  {
    return dialog.showAndWait ();
  }

  public String getPrefix ()
  {
    return prefix;
  }
}