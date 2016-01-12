package com.bytezone.dm3270.filetransfer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.assistant.Dataset;
import com.bytezone.dm3270.display.ScreenWatcher;
import com.bytezone.dm3270.utilities.FileSaver;

import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class UploadDialog
{
  private static final DateFormat df = new SimpleDateFormat ("dd/MM/yyyy HH:mm:ss");
  private static final Pattern jclPattern = Pattern.compile (".*\\.(CNTL|JCL)[.(].*\\)");
  private static final Pattern procPattern =
      Pattern.compile (".*\\.(PROC|PARM)LIB[.(].*\\)");

  final Dialog<IndFileCommand> dialog = new Dialog<> ();
  private final ScreenWatcher screenWatcher;

  public UploadDialog (ScreenWatcher screenWatcher, Path homePath, int baseLength)
  {
    this.screenWatcher = screenWatcher;

    Label labelFromFolder = new Label ();
    Label labelFileDate = new Label ();
    Label labelDatasetDate = new Label ();

    ComboBox<String> datasetList = new ComboBox<> ();
    datasetList
        .setItems (FXCollections.observableList (screenWatcher.getRecentDatasets ()));
    datasetList.setOnAction (event -> refreshUpload (datasetList, homePath,
                                                     labelFromFolder, labelFileDate,
                                                     labelDatasetDate, baseLength));
    datasetList.getSelectionModel ().select (screenWatcher.getSingleDataset ());

    GridPane grid = new GridPane ();

    grid.add (new Label ("Dataset"), 1, 1);
    grid.add (datasetList, 2, 1);

    grid.add (new Label ("From folder"), 1, 2);
    grid.add (labelFromFolder, 2, 2);

    grid.add (new Label ("File date"), 1, 3);
    grid.add (labelFileDate, 2, 3);

    grid.add (new Label ("Dataset date"), 1, 4);
    grid.add (labelDatasetDate, 2, 4);

    grid.setHgap (10);
    grid.setVgap (10);

    dialog.setTitle ("Upload dataset");

    ButtonType btnTypeOK = new ButtonType ("OK", ButtonData.OK_DONE);
    ButtonType btnTypeCancel = new ButtonType ("Cancel", ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane ().getButtonTypes ().addAll (btnTypeOK, btnTypeCancel);

    Node okButton = dialog.getDialogPane ().lookupButton (btnTypeOK);
    okButton.setDisable (true);

    dialog.getDialogPane ().setContent (grid);

    dialog.setResultConverter (btnType ->
    {
      if (btnType != btnTypeOK)
        return null;

      String datasetName = datasetList.getSelectionModel ().getSelectedItem ();
      IndFileCommand indFileCommand =
          new IndFileCommand (getCommandText ("PUT", datasetName));

      String saveFolderName = FileSaver.getSaveFolderName (homePath, datasetName);
      Path saveFile = Paths.get (saveFolderName, datasetName);
      indFileCommand.setLocalFile (saveFile.toFile ());

      return indFileCommand;
    });

    labelFileDate.textProperty ().addListener ( (observable, oldValue,
        newValue) -> okButton.setDisable (newValue.trim ().isEmpty ()));

    refreshUpload (datasetList, homePath, labelFromFolder, labelFileDate,
                   labelDatasetDate, baseLength);
  }

  private void refreshUpload (ComboBox<String> datasetList, Path homePath,
      Label labelFromFolder, Label labelFileDate, Label labelDatasetDate, int baseLength)
  {
    String datasetSelected = datasetList.getSelectionModel ().getSelectedItem ();
    String saveFolderName = FileSaver.getSaveFolderName (homePath, datasetSelected);
    Path saveFile = Paths.get (saveFolderName, datasetSelected);

    labelFromFolder.setText (saveFolderName.substring (baseLength));
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
      labelFileDate.setText (formatDate (saveFile));
    else
      labelFileDate.setText ("");
  }

  private String getCommandText (String direction, String datasetName)
  {
    Matcher matcher1 = jclPattern.matcher (datasetName);
    Matcher matcher2 = procPattern.matcher (datasetName);
    boolean useCrlf = matcher1.matches () || matcher2.matches ();

    // remove prefix to save space on the command line
    String prefix = screenWatcher.getPrefix ();
    if (!prefix.isEmpty () && datasetName.startsWith (prefix))
    {
      if (datasetName.length () == prefix.length ())
      {
        System.out.println ("Dataset name matches prefix - do not download");
        return "";
      }
      datasetName = datasetName.substring (prefix.length () + 1);
    }
    else
      datasetName = "'" + datasetName + "'";

    String tsoPrefix = screenWatcher.isTSOCommandScreen () ? "" : "TSO ";
    String options = useCrlf ? " ASCII CRLF" : "";

    return String.format ("%sIND$FILE %s %s%s", tsoPrefix, direction, datasetName,
                          options);
  }

  private String formatDate (Path saveFile)
  {
    try
    {
      BasicFileAttributes attr =
          Files.readAttributes (saveFile, BasicFileAttributes.class);
      return df.format (attr.lastModifiedTime ().toMillis ());
    }
    catch (IOException e)
    {
      return "IOException";
    }
  }
}