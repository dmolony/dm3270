package com.bytezone.dm3270.filetransfer;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.FieldManager;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.TSOCommandStatusListener;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TransferStage extends Stage implements TSOCommandStatusListener
{
  private static final int BUTTON_WIDTH = 100;
  private static final int LABEL_WIDTH = 100;
  private static final int TEXT_FIELD_WIDTH = 300;

  private final Screen screen;
  private ConsolePane consolePane;

  private final Button btnTransfer = new Button ("Transfer");
  private final Button btnCancel = new Button ("Cancel");

  private final Label lblPCFile = new Label ("PC File");
  private final Label lblMainframeFile = new Label ("Mainframe File");
  private final Label lblDirection = new Label ("Direction");

  private final TextField txtPCFile = new TextField ();
  private final TextField txtMainframeFile = new TextField ();

  private final RadioButton rbtnMFtoPC = new RadioButton ("Mainframe to PC  ");
  private final RadioButton rbtnPCtoMF = new RadioButton ("PC to Mainframe");
  private final ToggleGroup grpDirection = new ToggleGroup ();

  private final List<String> datasetNames = new ArrayList<> ();

  public TransferStage (Screen screen)
  {
    this.screen = screen;
    // this.consolePane = consolePane;

    BorderPane root = new BorderPane ();

    btnTransfer.setPrefWidth (BUTTON_WIDTH);
    btnCancel.setPrefWidth (BUTTON_WIDTH);

    lblMainframeFile.setPrefWidth (LABEL_WIDTH);
    lblMainframeFile.setAlignment (Pos.CENTER_RIGHT);
    lblPCFile.setPrefWidth (LABEL_WIDTH);
    lblPCFile.setAlignment (Pos.CENTER_RIGHT);
    lblDirection.setPrefWidth (LABEL_WIDTH);
    lblDirection.setAlignment (Pos.CENTER_RIGHT);

    txtMainframeFile.setPrefWidth (TEXT_FIELD_WIDTH);
    txtPCFile.setPrefWidth (TEXT_FIELD_WIDTH);

    HBox boxMainframeFile = new HBox (15);
    boxMainframeFile.getChildren ().addAll (lblMainframeFile, txtMainframeFile);
    boxMainframeFile.setAlignment (Pos.CENTER_LEFT);

    HBox boxPCFile = new HBox (15);
    boxPCFile.getChildren ().addAll (lblPCFile, txtPCFile);
    boxPCFile.setAlignment (Pos.CENTER_LEFT);

    HBox boxDirection = new HBox (15);
    boxDirection.getChildren ().addAll (lblDirection, rbtnMFtoPC, rbtnPCtoMF);
    rbtnMFtoPC.setSelected (true);
    rbtnMFtoPC.setToggleGroup (grpDirection);
    rbtnPCtoMF.setToggleGroup (grpDirection);

    VBox vbox1 = new VBox (10);
    vbox1.setPadding (new Insets (10, 10, 10, 10));
    vbox1.getChildren ().addAll (boxMainframeFile, boxPCFile, boxDirection);

    HBox hbox = new HBox (10);
    hbox.setPadding (new Insets (10, 10, 10, 10));
    hbox.setAlignment (Pos.CENTER);
    hbox.getChildren ().addAll (btnCancel, btnTransfer);

    root.setCenter (vbox1);
    root.setBottom (hbox);

    setScene (new Scene (root));

    grpDirection.selectedToggleProperty ().addListener ( (ov, oldToggle, newToggle) -> {
      if (newToggle != null)
        changeLayout ();
    });

    btnTransfer.setOnAction (e -> transferFile ());
    btnCancel.setOnAction (e -> close ());
  }

  public void setConsolePane (ConsolePane consolePane)
  {
    this.consolePane = consolePane;
  }

  private void changeLayout ()
  {

  }

  public void transferFile ()
  {
    FieldManager fieldManager = screen.getFieldManager ();

    // this needs to remove the HLQ or wrap it in apostrophes
    String filename = txtMainframeFile.getText ();
    if (filename != null && !filename.isEmpty ())
    {
      Field input = fieldManager.getTSOCommandField ();
      String tso = fieldManager.isTSOCommandScreen () ? "" : "TSO ";
      String command = String.format ("%sIND$FILE GET %s", tso, filename);
      input.setText (command);

      consolePane.sendAID (AIDCommand.AID_ENTER, "ENTR");
    }
    else
      System.out.println ("filename not specified");
  }

  @Override
  public void screenChanged (FieldManager fieldManager)
  {
    String datasetName = fieldManager.getCurrentDataset ();
    if (!datasetName.isEmpty ())
    {
      txtMainframeFile.setText (datasetName);
      if (!datasetNames.contains (datasetName))
        datasetNames.add (datasetName);
    }
  }
}