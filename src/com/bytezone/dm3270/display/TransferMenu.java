package com.bytezone.dm3270.display;

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
import com.bytezone.dm3270.application.Site;
import com.bytezone.dm3270.assistant.Dataset;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.filetransfer.IndFileCommand;
import com.bytezone.dm3270.filetransfer.Transfer.TransferType;
import com.bytezone.dm3270.filetransfer.TransferManager;
import com.bytezone.dm3270.utilities.FileSaver;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class TransferMenu implements ScreenChangeListener
{
  private static final Pattern jclPattern = Pattern.compile (".*\\.(CNTL|JCL)[.(].*\\)");
  private static final Pattern procPattern =
      Pattern.compile (".*\\.(PROC|PARM)LIB[.(].*\\)");
  private static final DateFormat df = new SimpleDateFormat ("dd/MM/yyyy HH:mm:ss");

  private ScreenWatcher screenWatcher;
  private final Site server;
  private Site replaySite;
  private TransferManager transferManager;
  private ConsolePane consolePane;

  public TransferMenu (Site site)
  {
    this.server = site;
  }

  // called from Screen.setReplayServer()
  public void setReplaySite (Site serverSite)
  {
    replaySite = serverSite;
  }

  public void setTransferManager (TransferManager transferManager)
  {
    this.transferManager = transferManager;
  }

  public void setConsolePane (ConsolePane consolePane)
  {
    this.consolePane = consolePane;
  }

  public void transfer (TransferType transferType)
  {
    Site site = server != null ? server : replaySite != null ? replaySite : null;
    String folderName = site == null ? "" : site.getFolder ();

    // change this to getHomePath (site) - move Site to dm3270Utilities
    Path homePath = FileSaver.getHomePath (folderName);
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

    String userHome = System.getProperty ("user.home");
    int baseLength = userHome.length () + 1;

    switch (transferType)
    {
      case DOWNLOAD:
        Optional<IndFileCommand> command = showDownloadDialog (homePath, baseLength);
        if (command.isPresent ())
          createTransfer (command.get ());
        break;

      case UPLOAD:
        command = showUploadDialog (homePath, baseLength);
        //        if ("OK".equals (cmd))
        //          System.out.println ("upload " + menuItemUpload.getUserData ());
        break;
    }
  }

  private void createTransfer (IndFileCommand indFileCommand)
  {
    assert consolePane != null;
    assert transferManager != null;

    Field tsoCommandField = screenWatcher.getTSOCommandField ();
    if (tsoCommandField == null)
    {
      showAlert ("This screen has no TSO input field");
      return;
    }

    String command = indFileCommand.getCommand ();
    if (command.length () > tsoCommandField.getDisplayLength ())
    {
      showAlert ("Command is too long for the TSO input field");
      System.out.printf ("Field: %d, command: %d%n", tsoCommandField.getDisplayLength (),
                         command.length ());
      return;
    }

    System.out.println (indFileCommand);
    System.out.println ();

    transferManager.prepareTransfer (indFileCommand);
    tsoCommandField.setText (indFileCommand.getCommand ());
    consolePane.sendAID (AIDCommand.AID_ENTER, "ENTR");
  }

  private boolean showAlert (String message)
  {
    Alert alert = new Alert (AlertType.ERROR, message);
    alert.getDialogPane ().setHeaderText (null);
    Optional<ButtonType> result = alert.showAndWait ();
    return (result.isPresent () && result.get () == ButtonType.OK);
  }

  private Optional<IndFileCommand> showDownloadDialog (Path homePath, int baseLength)
  {
    Label label1 = new Label ("Download");
    Label label3 = new Label ("To folder");
    Label saveFolder = new Label ();
    Label label5 = new Label ("Action");
    Label actionLabel = new Label ();
    Label label7 = new Label ("File date");
    Label fileDateLabel = new Label ();
    Label label9 = new Label ("Dataset date");
    Label datasetDateLabel = new Label ();

    ComboBox<String> box = new ComboBox<> ();
    List<String> recentDatasets = screenWatcher.getRecentDatasets ();
    box.setItems (FXCollections.observableList (recentDatasets));
    box.setOnAction (event -> refresh (box, homePath, saveFolder, actionLabel,
                                       fileDateLabel, datasetDateLabel, baseLength));
    box.getSelectionModel ().select (screenWatcher.getSingleDataset ());
    refresh (box, homePath, saveFolder, actionLabel, fileDateLabel, datasetDateLabel,
             baseLength);

    Dialog<IndFileCommand> dialog = new Dialog<> ();

    GridPane grid = new GridPane ();
    grid.setPadding (new Insets (10, 35, 10, 20));
    grid.add (label1, 1, 1);
    grid.add (box, 2, 1);
    grid.add (label3, 1, 2);
    grid.add (saveFolder, 2, 2);
    grid.add (label5, 1, 3);
    grid.add (actionLabel, 2, 3);
    grid.add (label7, 1, 4);
    grid.add (fileDateLabel, 2, 4);
    grid.add (label9, 1, 5);
    grid.add (datasetDateLabel, 2, 5);
    grid.setHgap (10);
    grid.setVgap (10);
    dialog.getDialogPane ().setContent (grid);

    ButtonType btnTypeOK = new ButtonType ("OK", ButtonData.OK_DONE);
    ButtonType btnTypeCancel = new ButtonType ("Cancel", ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane ().getButtonTypes ().addAll (btnTypeOK, btnTypeCancel);

    dialog.setResultConverter (btnType ->
    {
      if (btnType != btnTypeOK)
        return null;

      String datasetName = box.getSelectionModel ().getSelectedItem ();
      IndFileCommand indFileCommand =
          new IndFileCommand (getCommandText ("GET", datasetName));

      String saveFolderName = FileSaver.getSaveFolderName (homePath, datasetName);
      Path saveFile = Paths.get (saveFolderName, datasetName);
      //      indFileCommand.setDatasetName (datasetName);
      indFileCommand.setLocalFile (saveFile.toFile ());

      return indFileCommand;
    });

    return dialog.showAndWait ();
  }

  private void refresh (ComboBox<String> box, Path homePath, Label saveFolder,
      Label actionLabel, Label dateLabel, Label dateLabel2, int baseLength)
  {
    String datasetSelected = box.getSelectionModel ().getSelectedItem ();
    //    System.out.printf ("Dataset selected: %s%n", datasetSelected);
    String saveFolderName = FileSaver.getSaveFolderName (homePath, datasetSelected);
    //    System.out.printf ("Home path: %s%n", homePath);
    //    System.out.printf ("Save folder name: %s%n", saveFolderName);
    Path saveFile = Paths.get (saveFolderName, datasetSelected);
    //    System.out.printf ("Save folder path: %s%n", saveFile);

    saveFolder.setText (saveFolderName.substring (baseLength));
    //    Dataset dataset = siteDatasets.get (datasetSelected);
    Optional<Dataset> dataset = screenWatcher.getDataset (datasetSelected);
    //    if (dataset != null)
    if (dataset.isPresent ())
    {
      String date = dataset.get ().getReferredDate ();
      if (date.isEmpty ())
        dateLabel2.setText ("<no date>");
      else
      {
        String reformattedDate = date.substring (8) + "/" + date.substring (5, 7) + "/"
            + date.substring (0, 4);
        dateLabel2.setText (reformattedDate + " " + dataset.get ().getReferredTime ());
      }
    }
    else
      System.out.println ("not found");

    if (Files.exists (saveFile))
    {
      try
      {
        BasicFileAttributes attr =
            Files.readAttributes (saveFile, BasicFileAttributes.class);
        dateLabel.setText (df.format (attr.lastModifiedTime ().toMillis ()));
      }
      catch (IOException e)
      {
        dateLabel.setText ("IOException");
      }
      actionLabel.setText ("Overwrite existing file");
    }
    else
    {
      actionLabel.setText ("Create new file");
      dateLabel.setText ("");
    }
  }

  private Optional<IndFileCommand> showUploadDialog (Path homePath, int baseLength)
  {
    Label label1 = new Label ("Upload: ");
    //    Label label2 = new Label (fileName);
    Label label3 = new Label ("From folder: ");
    Label saveFolder = new Label ("??");
    //    Label label5 = new Label ("Exists: ");
    //    Label label6 = new Label (Files.exists (path) ? "Yes" : "No");

    ComboBox<String> box = new ComboBox<> ();
    List<String> recentDatasets = screenWatcher.getRecentDatasets ();
    box.setItems (FXCollections.observableList (recentDatasets));
    //    box.setOnAction (event -> refresh (box, homePath, saveFolder, actionLabel,
    //                                   fileDateLabel, datasetDateLabel, baseLength));
    //    box.getSelectionModel ().select (singleDataset);
    //    refresh (box, homePath, saveFolder, actionLabel, fileDateLabel,
    // datasetDateLabel,
    //             baseLength);

    Dialog<IndFileCommand> dialog = new Dialog<> ();

    GridPane grid = new GridPane ();
    grid.add (label1, 1, 1);
    grid.add (box, 2, 1);
    grid.add (label3, 1, 2);
    grid.add (saveFolder, 2, 2);
    //    grid.add (label5, 1, 3);
    //    grid.add (label6, 2, 3);
    grid.setHgap (10);
    grid.setVgap (10);
    dialog.getDialogPane ().setContent (grid);

    ButtonType btnTypeOK = new ButtonType ("OK", ButtonData.OK_DONE);
    ButtonType btnTypeCancel = new ButtonType ("Cancel", ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane ().getButtonTypes ().addAll (btnTypeOK, btnTypeCancel);

    dialog.setResultConverter (btnType ->
    {
      if (btnType != btnTypeOK)
        return null;

      String datasetName = box.getSelectionModel ().getSelectedItem ();
      IndFileCommand indFileCommand =
          new IndFileCommand (getCommandText ("PUT", datasetName));

      return indFileCommand;
    });

    return dialog.showAndWait ();
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

  @Override
  public void screenChanged (ScreenWatcher screenWatcher)
  {
    this.screenWatcher = screenWatcher;
  }
}