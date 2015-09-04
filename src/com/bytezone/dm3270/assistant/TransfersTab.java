package com.bytezone.dm3270.assistant;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenDetails;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class TransfersTab extends AbstractTransferTab implements ScreenChangeListener,
    DatasetSelectionListener, FileSelectionListener, JobSelectionListener
{
  RadioButton btnDatasets = new RadioButton ("Datasets");
  RadioButton btnFiles = new RadioButton ("Files");
  RadioButton btnJobs = new RadioButton ("Jobs");
  RadioButton btnSpecify = new RadioButton ("Specify");

  TextField txtDatasets = new TextField ();
  TextField txtFiles = new TextField ();
  TextField txtJobs = new TextField ();
  TextField txtSpecify = new TextField ();

  Label lblDisposition = new Label ("Disposition");
  Label lblLrecl = new Label ("LRECL");
  Label lblBlksize = new Label ("BLKSIZE");
  Label lblSpace = new Label ("SPACE");

  RadioButton btnCylinders = new RadioButton ("CYL");
  RadioButton btnTracks = new RadioButton ("TRK");
  RadioButton btnBlocks = new RadioButton ("BLK");

  TextField txtLrecl = new TextField ();
  TextField txtBlksize = new TextField ();
  TextField txtSpace = new TextField ();

  RadioButton btnFB = new RadioButton ("FB");
  RadioButton btnPS = new RadioButton ("PS");

  private final ToggleGroup grpFileName = new ToggleGroup ();
  private final ToggleGroup grpSpaceUnits = new ToggleGroup ();
  private final ToggleGroup grpDisposition = new ToggleGroup ();

  public TransfersTab (Screen screen, TextField text, Button execute)
  {
    super ("Transfers", screen, text, execute);

    btnTracks.setSelected (true);
    btnFiles.setSelected (true);
    btnFB.setSelected (true);

    grpFileName.getToggles ().addAll (btnDatasets, btnFiles, btnJobs, btnSpecify);
    grpSpaceUnits.getToggles ().addAll (btnTracks, btnCylinders, btnBlocks);

    grpFileName.selectedToggleProperty ()
        .addListener ( (ov, oldToggle, newToggle) -> toggleSelected (newToggle));

    grpSpaceUnits.selectedToggleProperty ()
        .addListener ( (ov, oldToggle, newToggle) -> toggleSelected (newToggle));

    VBox datasetBlock = new VBox (10);
    datasetBlock.setPadding (new Insets (10, 10, 10, 10));

    HBox line1 = getLine (btnDatasets, txtDatasets);
    HBox line2 = getLine (btnJobs, txtJobs);
    HBox line3 = getLine (btnFiles, txtFiles);
    HBox line4 = getLine (btnSpecify, txtSpecify);

    txtDatasets.setPromptText ("no dataset selected");
    txtJobs.setPromptText ("no batch job selected");
    txtFiles.setPromptText ("no file selected");

    txtSpecify.setEditable (true);

    datasetBlock.getChildren ().addAll (line1, line2, line3, line4);

    VBox spaceBlock = new VBox (10);
    spaceBlock.setPadding (new Insets (10, 10, 10, 10));

    HBox line5 = getLine (lblLrecl, txtLrecl);
    HBox line6 = getLine (lblBlksize, txtBlksize);
    HBox line7 = getLine (btnTracks, btnCylinders, btnBlocks);
    HBox line8 = getLine (lblSpace, txtSpace, line7);
    HBox line9 = getLine (btnFB, btnPS);
    HBox line10 = getLine (lblDisposition, line9);

    spaceBlock.getChildren ().addAll (line10, line5, line6, line8);

    datasetBlock.setStyle ("-fx-border-color: grey; -fx-border-width: 1;"
        + " -fx-border-insets: 10");
    spaceBlock.setStyle ("-fx-border-color: grey; -fx-border-width: 1;"
        + " -fx-border-insets: 10");

    VBox column = new VBox ();
    column.setPadding (new Insets (10, 10, 10, 10));
    column.getChildren ().addAll (datasetBlock, spaceBlock);

    BorderPane borderPane = new BorderPane ();
    borderPane.setLeft (column);

    setContent (borderPane);
  }

  private void toggleSelected (Toggle toggle)
  {
    if (toggle != null)
      setText ();
  }

  private HBox getLine (RadioButton button, TextField text)
  {
    HBox line = new HBox (10);
    line.getChildren ().addAll (button, text);
    button.setPrefWidth (90);
    text.setPrefWidth (300);
    text.setFont (Font.font ("Monospaced", 12));
    text.setEditable (false);
    text.setFocusTraversable (false);
    button.setUserData (text);

    return line;
  }

  private HBox getLine (Label label, TextField text)
  {
    HBox line = new HBox (10);
    line.getChildren ().addAll (label, text);
    label.setPrefWidth (90);
    text.setPrefWidth (100);
    text.setFont (Font.font ("Monospaced", 12));
    text.setEditable (true);
    text.setFocusTraversable (true);

    return line;
  }

  private HBox getLine (Label label, TextField text, HBox hbox)
  {
    HBox line = new HBox (10);

    line.getChildren ().addAll (label, text, hbox);
    label.setPrefWidth (90);
    text.setPrefWidth (100);
    text.setFont (Font.font ("Monospaced", 12));
    text.setEditable (true);
    text.setFocusTraversable (true);

    return line;
  }

  private HBox getLine (Label label, HBox hbox)
  {
    HBox line = new HBox (10);

    line.getChildren ().addAll (label, hbox);
    label.setPrefWidth (90);

    return line;
  }

  private HBox getLine (RadioButton... buttons)
  {
    HBox line = new HBox (10);

    for (RadioButton button : buttons)
      line.getChildren ().add (button);

    return line;
  }

  @Override
      void setText ()
  {
    RadioButton selectedFileButton = (RadioButton) grpFileName.getSelectedToggle ();
    if (selectedFileButton == null)
    {
      eraseCommand ();
      return;
    }

    RadioButton selectedSpaceUnitsButton =
        (RadioButton) grpSpaceUnits.getSelectedToggle ();

    txtCommand.setText (((TextField) selectedFileButton.getUserData ()).getText ());
    ScreenDetails screenDetails = screen.getScreenDetails ();
    btnExecute.setDisable (screenDetails.isKeyboardLocked ()
        || screenDetails.getTSOCommandField () == null);
  }

  @Override
  public void screenChanged ()
  {
    if (isSelected ())
      setText ();
  }

  @Override
  public void datasetSelected (Dataset dataset)
  {
    txtDatasets.setText (dataset.getDatasetName ());
    if (isSelected ())
      setText ();
  }

  @Override
  public void fileSelected (String filename)
  {
    txtFiles.setText (filename);
    if (isSelected ())
      setText ();
  }

  @Override
  public void jobSelected (BatchJob job)
  {
    txtJobs.setText (job.getOutputFile ());
    if (isSelected ())
      setText ();
  }
}