package com.bytezone.dm3270.assistant;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenDetails;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class TransfersTab extends AbstractTransferTab implements ScreenChangeListener,
    DatasetSelectionListener, FileSelectionListener, JobSelectionListener
{
  Label lblDatasets = new Label ("Datasets");
  Label lblFiles = new Label ("Files");
  Label lblJobs = new Label ("Batch Jobs");
  Label lblSpecify = new Label ("Specify");

  RadioButton btnDatasets = new RadioButton ();
  RadioButton btnFiles = new RadioButton ();
  RadioButton btnJobs = new RadioButton ();
  RadioButton btnSpecify = new RadioButton ();

  TextField txtDatasets = new TextField ();
  TextField txtFiles = new TextField ();
  TextField txtJobs = new TextField ();
  TextField txtSpecify = new TextField ();

  Label lblLrecl = new Label ("LRECL");
  Label lblBlksize = new Label ("BLKSIZE");
  Label lblSpace = new Label ("SPACE");

  TextField txtLrecl = new TextField ();
  TextField txtBlksize = new TextField ();
  TextField txtSpace = new TextField ();

  private final ToggleGroup grpFileName = new ToggleGroup ();

  public TransfersTab (Screen screen, TextField text, Button execute)
  {
    super ("Transfers", screen, text, execute);

    grpFileName.getToggles ().addAll (btnDatasets, btnFiles, btnJobs, btnSpecify);

    grpFileName.selectedToggleProperty ().addListener ( (ov, oldToggle, newToggle) -> {
      if (newToggle != null)
        selectDataset ((TextField) newToggle.getUserData ());
    });

    VBox datasetBlock = new VBox (10);
    datasetBlock.setPadding (new Insets (10, 10, 10, 10));

    HBox line1 = getLine (btnDatasets, lblDatasets, txtDatasets);
    HBox line2 = getLine (btnJobs, lblJobs, txtJobs);
    HBox line3 = getLine (btnFiles, lblFiles, txtFiles);
    HBox line4 = getLine (btnSpecify, lblSpecify, txtSpecify);

    txtSpecify.setEditable (true);

    datasetBlock.getChildren ().addAll (line1, line2, line3, line4);

    VBox spaceBlock = new VBox (10);
    spaceBlock.setPadding (new Insets (10, 10, 10, 10));

    HBox line5 = getLine (lblLrecl, txtLrecl);
    HBox line6 = getLine (lblBlksize, txtBlksize);
    HBox line7 = getLine (lblSpace, txtSpace);

    spaceBlock.getChildren ().addAll (line5, line6, line7);

    datasetBlock.setStyle ("-fx-border-color: grey; -fx-border-width: 1;"
        + " -fx-border-insets: 10");
    spaceBlock.setStyle ("-fx-border-color: grey; -fx-border-width: 1;"
        + " -fx-border-insets: 10");

    VBox column = new VBox (10);
    column.getChildren ().addAll (datasetBlock, spaceBlock);

    BorderPane borderPane = new BorderPane ();
    borderPane.setLeft (column);

    setContent (borderPane);
  }

  private void selectDataset (TextField text)
  {
    setText ();
  }

  private HBox getLine (RadioButton button, Label label, TextField text)
  {
    HBox line = new HBox (10);
    line.getChildren ().addAll (button, label, text);
    label.setPrefWidth (70);
    text.setPrefWidth (300);
    text.setFont (Font.font ("Monospaced", 12));
    text.setPromptText ("nothing yet");
    text.setEditable (false);
    text.setFocusTraversable (false);
    button.setUserData (text);

    return line;
  }

  private HBox getLine (Label label, TextField text)
  {
    HBox line = new HBox (10);
    line.getChildren ().addAll (label, text);
    label.setPrefWidth (70);
    text.setPrefWidth (100);
    text.setFont (Font.font ("Monospaced", 12));
    text.setEditable (true);
    text.setFocusTraversable (true);

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