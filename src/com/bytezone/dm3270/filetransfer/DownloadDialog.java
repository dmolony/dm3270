package com.bytezone.dm3270.filetransfer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.bytezone.dm3270.assistant.Dataset;
import com.bytezone.dm3270.display.ScreenWatcher;
import com.bytezone.dm3270.utilities.FileSaver;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class DownloadDialog extends TransferDialog
{
  Label labelToFolder = new Label ();
  Label labelAction = new Label ();
  Label labelFileDate = new Label ();
  Label labelDatasetDate = new Label ();

  public DownloadDialog (ScreenWatcher screenWatcher, Path homePath, int baseLength)
  {
    super (screenWatcher, homePath, baseLength);

    datasetList.setOnAction (event -> refreshDownload ());

    GridPane grid = new GridPane ();
    grid.setPadding (new Insets (10, 35, 10, 20));

    grid.add (new Label ("Dataset"), 1, 1);
    grid.add (datasetList, 2, 1);

    grid.add (new Label ("To folder"), 1, 2);
    grid.add (labelToFolder, 2, 2);

    grid.add (new Label ("Action"), 1, 3);
    grid.add (labelAction, 2, 3);

    grid.add (new Label ("File date"), 1, 4);
    grid.add (labelFileDate, 2, 4);

    grid.add (new Label ("Dataset date"), 1, 5);
    grid.add (labelDatasetDate, 2, 5);

    grid.setHgap (10);
    grid.setVgap (10);

    dialog.setTitle ("Download dataset");
    dialog.getDialogPane ().setContent (grid);

    ButtonType btnTypeOK = new ButtonType ("OK", ButtonData.OK_DONE);
    ButtonType btnTypeCancel = new ButtonType ("Cancel", ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane ().getButtonTypes ().addAll (btnTypeOK, btnTypeCancel);

    dialog.setResultConverter (btnType ->
    {
      if (btnType != btnTypeOK)
        return null;

      String datasetName = datasetList.getSelectionModel ().getSelectedItem ();
      IndFileCommand indFileCommand =
          new IndFileCommand (getCommandText ("GET", datasetName));

      String saveFolderName = FileSaver.getSaveFolderName (homePath, datasetName);
      Path saveFile = Paths.get (saveFolderName, datasetName);
      indFileCommand.setLocalFile (saveFile.toFile ());

      return indFileCommand;
    });

    refreshDownload ();
  }

  private void refreshDownload ()
  {
    String datasetSelected = datasetList.getSelectionModel ().getSelectedItem ();
    String saveFolderName = FileSaver.getSaveFolderName (homePath, datasetSelected);
    Path saveFile = Paths.get (saveFolderName, datasetSelected);

    labelToFolder.setText (saveFolderName.substring (baseLength));
    Optional<Dataset> dataset = screenWatcher.getDataset (datasetSelected);
    if (dataset.isPresent ())
    {
      String date = dataset.get ().getReferredDate ();
      if (date.isEmpty ())
        labelDatasetDate.setText ("<no date>");
      else
      {
        String reformattedDate = date.substring (8) + "/" + date.substring (5, 7) + "/"
            + date.substring (0, 4);
        labelDatasetDate
            .setText (reformattedDate + " " + dataset.get ().getReferredTime ());
      }
    }
    else
      System.out.println ("not found");

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