package com.bytezone.dm3270.assistant;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
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

  private final ToggleGroup grpFileName = new ToggleGroup ();

  public TransfersTab (Screen screen, TextField text, Button execute)
  {
    super ("Transfers", screen, text, execute);

    grpFileName.getToggles ().addAll (btnDatasets, btnFiles, btnJobs, btnSpecify);

    VBox vbox = new VBox (10);
    vbox.setPadding (new Insets (10, 10, 10, 10));

    HBox line1 = getLine (btnDatasets, lblDatasets, txtDatasets);
    HBox line2 = getLine (btnJobs, lblJobs, txtJobs);
    HBox line3 = getLine (btnFiles, lblFiles, txtFiles);
    HBox line4 = getLine (btnSpecify, lblSpecify, txtSpecify);

    txtSpecify.setEditable (true);

    vbox.getChildren ().addAll (line1, line2, line3, line4);

    setContent (vbox);
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

    return line;
  }

  @Override
      void setText ()
  {
    Toggle toggle = grpFileName.getSelectedToggle ();
    if (toggle == null)
    {
      eraseCommand ();
      return;
    }
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