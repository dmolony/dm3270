package com.bytezone.dm3270.filetransfer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.assistant.Dataset;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenWatcher;
import com.bytezone.dm3270.filetransfer.Transfer.TransferType;
import com.bytezone.dm3270.utilities.FileSaver;
import com.bytezone.dm3270.utilities.Site;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;

public class TransferMenu implements ScreenChangeListener
{
  private static final Pattern jclPattern = Pattern.compile (".*\\.(CNTL|JCL)[.(].*\\)");
  private static final Pattern procPattern =
      Pattern.compile (".*\\.(PROC|PARM)LIB[.(].*\\)");
  private static final DateFormat df = new SimpleDateFormat ("dd/MM/yyyy HH:mm:ss");

  private Site server;

  private ScreenWatcher screenWatcher;
  private final TransferManager transferManager;
  private ConsolePane consolePane;

  private final MenuItem menuItemUpload;
  private final MenuItem menuItemDownload;

  public TransferMenu (Site server, TransferManager transferManager)
  {
    this.server = server;
    this.transferManager = transferManager;
    menuItemUpload =
        getMenuItem ("Upload", e -> transfer (TransferType.UPLOAD), KeyCode.U);
    menuItemDownload =
        getMenuItem ("Download", e -> transfer (TransferType.DOWNLOAD), KeyCode.D);
  }

  // called from Screen.setReplayServer()
  public void setReplayServer (Site server)
  {
    this.server = server;
  }

  // called from Screen.setConsolePane()
  public void setConsolePane (ConsolePane consolePane)
  {
    this.consolePane = consolePane;
  }

  public MenuItem getMenuItemUpload ()
  {
    return menuItemUpload;
  }

  public MenuItem getMenuItemDownload ()
  {
    return menuItemDownload;
  }

  // called from the Upload and Download menu items
  private void transfer (TransferType transferType)
  {
    assert consolePane != null;
    assert transferManager != null;

    Path homePath = FileSaver.getHomePath (server);
    if (Files.notExists (homePath))
    {
      showAlert ("Path does not exist: " + homePath);
      return;
    }

    List<String> recentDatasets = screenWatcher.getRecentDatasets ();
    if (recentDatasets.size () == 0)
    {
      showAlert ("No datasets to download");
      return;
    }

    Field tsoCommandField = screenWatcher.getTSOCommandField ();
    if (tsoCommandField == null)
    {
      showAlert ("This screen has no TSO input field");
      return;
    }

    String userHome = System.getProperty ("user.home");
    int baseLength = userHome.length () + 1;
    Optional<IndFileCommand> optCommand = null;

    if (transferType == TransferType.DOWNLOAD)
      optCommand = showDownloadDialog (homePath, baseLength);
    else
      optCommand = showUploadDialog (homePath, baseLength);

    if (!optCommand.isPresent ())
      return;

    IndFileCommand indFileCommand = optCommand.get ();
    String commandText = indFileCommand.getCommand ();

    if (commandText.length () > tsoCommandField.getDisplayLength ())
    {
      showAlert ("Command is too long for the TSO input field");
      System.out.printf ("Field: %d, command: %d%n", tsoCommandField.getDisplayLength (),
                         commandText.length ());
      return;
    }

    System.out.println (indFileCommand);
    System.out.println ();

    transferManager.prepareTransfer (indFileCommand);
    tsoCommandField.setText (commandText);
    consolePane.sendAID (AIDCommand.AID_ENTER, "ENTR");
  }

  private Optional<IndFileCommand> showDownloadDialog (Path homePath, int baseLength)
  {
    Label labelToFolder = new Label ();
    Label labelAction = new Label ();
    Label labelFileDate = new Label ();
    Label labelDatasetDate = new Label ();

    ComboBox<String> datasetList = new ComboBox<> ();
    List<String> recentDatasets = screenWatcher.getRecentDatasets ();
    datasetList.setItems (FXCollections.observableList (recentDatasets));
    datasetList
        .setOnAction (event -> refreshDownload (datasetList, homePath, labelToFolder,
                                                labelAction, labelFileDate,
                                                labelDatasetDate, baseLength));
    datasetList.getSelectionModel ().select (screenWatcher.getSingleDataset ());
    refreshDownload (datasetList, homePath, labelToFolder, labelAction, labelFileDate,
                     labelDatasetDate, baseLength);

    GridPane grid = new GridPane ();
    grid.setPadding (new Insets (10, 35, 10, 20));

    grid.add (new Label ("Download"), 1, 1);
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

    Dialog<IndFileCommand> dialog = new Dialog<> ();
    dialog.getDialogPane ().setContent (grid);

    ButtonType btnTypeOK = new ButtonType ("OK", ButtonData.OK_DONE);
    ButtonType btnTypeCancel = new ButtonType ("Cancel", ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane ().getButtonTypes ().addAll (btnTypeOK, btnTypeCancel);
    dialog.setTitle ("Download dataset");

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

    return dialog.showAndWait ();
  }

  private void refreshDownload (ComboBox<String> datasetList, Path homePath,
      Label labelToFolder, Label labelAction, Label labelFileDate, Label labelDatasetDate,
      int baseLength)
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

  private Optional<IndFileCommand> showUploadDialog (Path homePath, int baseLength)
  {
    Dialog<IndFileCommand> dialog = new Dialog<> ();

    ButtonType btnTypeOK = new ButtonType ("OK", ButtonData.OK_DONE);
    ButtonType btnTypeCancel = new ButtonType ("Cancel", ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane ().getButtonTypes ().addAll (btnTypeOK, btnTypeCancel);
    dialog.setTitle ("Upload dataset");

    Node okButton = dialog.getDialogPane ().lookupButton (btnTypeOK);
    okButton.setDisable (true);

    Label labelFromFolder = new Label ();
    Label labelFileDate = new Label ();
    Label labelDatasetDate = new Label ();

    ComboBox<String> datasetList = new ComboBox<> ();
    List<String> recentDatasets = screenWatcher.getRecentDatasets ();
    datasetList.setItems (FXCollections.observableList (recentDatasets));
    datasetList.setOnAction (event -> refreshUpload (datasetList, homePath,
                                                     labelFromFolder, labelFileDate,
                                                     labelDatasetDate, baseLength));
    datasetList.getSelectionModel ().select (screenWatcher.getSingleDataset ());

    GridPane grid = new GridPane ();

    grid.add (new Label ("Upload"), 1, 1);
    grid.add (datasetList, 2, 1);

    grid.add (new Label ("From folder"), 1, 2);
    grid.add (labelFromFolder, 2, 2);

    grid.add (new Label ("File date"), 1, 3);
    grid.add (labelFileDate, 2, 3);

    grid.add (new Label ("Dataset date"), 1, 4);
    grid.add (labelDatasetDate, 2, 4);

    grid.setHgap (10);
    grid.setVgap (10);

    dialog.getDialogPane ().setContent (grid);

    dialog.setResultConverter (btnType ->
    {
      if (btnType != btnTypeOK)
        return null;

      String datasetName = datasetList.getSelectionModel ().getSelectedItem ();
      IndFileCommand indFileCommand =
          new IndFileCommand (getCommandText ("PUT", datasetName));

      return indFileCommand;
    });

    labelFileDate.textProperty ().addListener ( (observable, oldValue,
        newValue) -> okButton.setDisable (newValue.trim ().isEmpty ()));

    refreshUpload (datasetList, homePath, labelFromFolder, labelFileDate,
                   labelDatasetDate, baseLength);

    return dialog.showAndWait ();
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

  private boolean showAlert (String message)
  {
    Alert alert = new Alert (AlertType.ERROR, message);
    alert.getDialogPane ().setHeaderText (null);
    Optional<ButtonType> result = alert.showAndWait ();
    return (result.isPresent () && result.get () == ButtonType.OK);
  }

  private MenuItem getMenuItem (String text, EventHandler<ActionEvent> eventHandler,
      KeyCode keyCode)
  {
    MenuItem menuItem = new MenuItem (text);
    menuItem.setOnAction (eventHandler);
    menuItem
        .setAccelerator (new KeyCodeCombination (keyCode, KeyCombination.SHORTCUT_DOWN));
    return menuItem;
  }

  @Override
  public void screenChanged (ScreenWatcher screenWatcher)
  {
    this.screenWatcher = screenWatcher;
  }
}