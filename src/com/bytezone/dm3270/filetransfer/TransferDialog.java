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
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

public class TransferDialog
{
  protected static final DateFormat df = new SimpleDateFormat ("yyyy/MM/dd HH:mm:ss");
  protected static final Pattern jclPattern =
      Pattern.compile (".*\\.(CNTL|JCL)[.(].*\\)");
  protected static final Pattern procPattern =
      Pattern.compile (".*\\.(PROC|PARM)LIB[.(].*\\)");

  protected final Dialog<IndFileCommand> dialog = new Dialog<> ();

  protected final GridPane grid = new GridPane ();
  protected final Font labelFont = Font.font ("Monospaced", 14);
  protected final ComboBox<String> datasetComboBox = new ComboBox<> ();

  protected final ButtonType btnTypeOK = new ButtonType ("OK", ButtonData.OK_DONE);
  protected final ButtonType btnTypeCancel =
      new ButtonType ("Cancel", ButtonData.CANCEL_CLOSE);

  protected final ScreenWatcher screenWatcher;
  protected final Path homePath;
  protected final int baseLength;
  protected final String commandDirection;

  public TransferDialog (ScreenWatcher screenWatcher, Path homePath, int baseLength,
      String title, String commandDirection)
  {
    this.screenWatcher = screenWatcher;
    this.homePath = homePath;
    this.baseLength = baseLength;
    this.commandDirection = commandDirection;

    grid.setPadding (new Insets (10, 35, 10, 20));
    grid.setHgap (10);
    grid.setVgap (10);

    dialog.setTitle (title);
    dialog.getDialogPane ().getButtonTypes ().addAll (btnTypeOK, btnTypeCancel);
    dialog.getDialogPane ().setContent (grid);

    dialog.setResultConverter (btnType ->
    {
      if (btnType != btnTypeOK)
        return null;

      String datasetName = datasetComboBox.getSelectionModel ().getSelectedItem ();
      IndFileCommand indFileCommand =
          new IndFileCommand (getCommandText (commandDirection, datasetName));

      String saveFolderName = FileSaver.getSaveFolderName (homePath, datasetName);
      Path saveFile = Paths.get (saveFolderName, datasetName);
      indFileCommand.setLocalFile (saveFile.toFile ());

      return indFileCommand;
    });

    datasetComboBox.setStyle ("-fx-font-size: 13; -fx-font-family: Monospaced");

    buildDatasetList ();
  }

  public Optional<IndFileCommand> showAndWait ()
  {
    buildDatasetList ();
    return dialog.showAndWait ();
  }

  private void buildDatasetList ()
  {
    datasetComboBox
        .setItems (FXCollections.observableList (screenWatcher.getRecentDatasets ()));
    datasetComboBox.getSelectionModel ().select (screenWatcher.getSingleDataset ());
  }

  protected String getCommandText (String direction, String datasetName)
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
        System.out.println ("Dataset name matches prefix - do not transfer");
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

  protected String formatDate (Dataset dataset)
  {
    String date = dataset.getReferredDate ();
    if (date == null || date.isEmpty ())
      return "";
    else
    {
      String reformattedDate =
          date.substring (0, 4) + "/" + date.substring (5, 7) + "/" + date.substring (8);
      return reformattedDate + " " + dataset.getReferredTime ();
    }
  }

  protected String formatDate (Path saveFile)
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