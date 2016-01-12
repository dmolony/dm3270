package com.bytezone.dm3270.filetransfer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.bytezone.dm3270.assistant.Dataset;
import com.bytezone.dm3270.display.ScreenWatcher;
import com.bytezone.dm3270.utilities.FileSaver;

import javafx.scene.Node;
import javafx.scene.control.Label;

public class UploadDialog extends TransferDialog
{
  private final Label labelFromFolder = new Label ();
  private final Label labelFileDate = new Label ();
  private final Label labelDatasetDate = new Label ();

  public UploadDialog (ScreenWatcher screenWatcher, Path homePath, int baseLength)
  {
    super (screenWatcher, homePath, baseLength, "Upload dataset", "PUT");

    labelFromFolder.setFont (labelFont);
    labelFileDate.setFont (labelFont);
    labelDatasetDate.setFont (labelFont);

    grid.add (new Label ("Dataset"), 1, 1);
    grid.add (datasetComboBox, 2, 1);

    grid.add (new Label ("Folder"), 1, 2);
    grid.add (labelFromFolder, 2, 2);

    grid.add (new Label ("File date"), 1, 3);
    grid.add (labelFileDate, 2, 3);

    grid.add (new Label ("Dataset date"), 1, 4);
    grid.add (labelDatasetDate, 2, 4);

    Node okButton = dialog.getDialogPane ().lookupButton (btnTypeOK);
    okButton.setDisable (true);

    labelFileDate.textProperty ().addListener ( (observable, oldValue,
        newValue) -> okButton.setDisable (newValue.trim ().isEmpty ()));

    refreshUpload ();
    datasetComboBox.setOnAction (event -> refreshUpload ());
  }

  private void refreshUpload ()
  {
    String datasetSelected = datasetComboBox.getSelectionModel ().getSelectedItem ();
    if (datasetSelected == null)    // could be rebuilding the list
      return;

    String saveFolderName = FileSaver.getSaveFolderName (homePath, datasetSelected);
    Path saveFile = Paths.get (saveFolderName, datasetSelected);

    labelFromFolder.setText (saveFolderName.substring (baseLength));
    Optional<Dataset> dataset = screenWatcher.getDataset (datasetSelected);
    if (dataset.isPresent ())
    {
      String date = dataset.get ().getReferredDate ();
      if (date == null || date.isEmpty ())
        labelDatasetDate.setText ("");
      else
      {
        String reformattedDate = date.substring (0, 4) + "/" + date.substring (5, 7) + "/"
            + date.substring (8);
        labelDatasetDate
            .setText (reformattedDate + " " + dataset.get ().getReferredTime ());
      }
    }
    else
    {
      System.out.println ("not found");
      labelDatasetDate.setText ("");
    }

    if (Files.exists (saveFile))
      labelFileDate.setText (formatDate (saveFile));
    else
      labelFileDate.setText ("");
  }
}