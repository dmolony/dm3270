package com.bytezone.dm3270.filetransfer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.bytezone.dm3270.assistant.Dataset;
import com.bytezone.dm3270.display.ScreenWatcher;
import com.bytezone.dm3270.utilities.FileSaver;

import javafx.scene.control.Label;

public class DownloadDialog extends TransferDialog
{
  private final Label labelToFolder = new Label ();
  private final Label labelAction = new Label ();
  private final Label labelFileDate = new Label ();
  private final Label labelDatasetDate = new Label ();

  public DownloadDialog (ScreenWatcher screenWatcher, Path homePath, int baseLength)
  {
    super (screenWatcher, homePath, baseLength, "Download dataset", "GET");

    labelToFolder.setFont (labelFont);
    labelAction.setFont (labelFont);
    labelFileDate.setFont (labelFont);
    labelDatasetDate.setFont (labelFont);

    grid.add (new Label ("Dataset"), 1, 1);
    grid.add (datasetComboBox, 2, 1);

    grid.add (new Label ("Folder"), 1, 2);
    grid.add (labelToFolder, 2, 2);

    grid.add (new Label ("Action"), 1, 3);
    grid.add (labelAction, 2, 3);

    grid.add (new Label ("File date"), 1, 4);
    grid.add (labelFileDate, 2, 4);

    grid.add (new Label ("Dataset date"), 1, 5);
    grid.add (labelDatasetDate, 2, 5);

    refreshDownload ();
    datasetComboBox.setOnAction (event -> refreshDownload ());
  }

  private void refreshDownload ()
  {
    String datasetSelected = datasetComboBox.getSelectionModel ().getSelectedItem ();
    if (datasetSelected == null)    // could be rebuilding the list
      return;

    String saveFolderName = FileSaver.getSaveFolderName (homePath, datasetSelected);
    Path saveFile = Paths.get (saveFolderName, datasetSelected);

    labelToFolder.setText (saveFolderName.substring (baseLength));

    Optional<Dataset> dataset = screenWatcher.getDataset (datasetSelected);
    if (dataset.isPresent ())
      labelDatasetDate.setText (formatDate (dataset.get ()));
    else
      labelDatasetDate.setText ("");

    if (Files.exists (saveFile))
    {
      labelFileDate.setText (formatDate (saveFile));
      labelAction.setText ("Overwrite existing file");
    }
    else
    {
      labelFileDate.setText ("");
      labelAction.setText ("Create new file");
    }
  }
}