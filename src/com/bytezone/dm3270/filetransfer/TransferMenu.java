package com.bytezone.dm3270.filetransfer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenWatcher;
import com.bytezone.dm3270.filetransfer.Transfer.TransferType;
import com.bytezone.dm3270.utilities.Dm3270Utility;
import com.bytezone.dm3270.utilities.FileSaver;
import com.bytezone.dm3270.utilities.Site;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class TransferMenu implements ScreenChangeListener
{
  private Site server;

  private ScreenWatcher screenWatcher;
  private final TransferManager transferManager;
  private ConsolePane consolePane;

  private final MenuItem menuItemUpload;
  private final MenuItem menuItemDownload;

  private UploadDialog uploadDialog;
  private DownloadDialog downloadDialog;

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

    if (server == null)
    {
      Dm3270Utility.showAlert ("Null server");
      return;
    }

    Path homePath = FileSaver.getHomePath (server);
    if (Files.notExists (homePath))
    {
      Dm3270Utility.showAlert ("Path does not exist: " + homePath);
      return;
    }

    List<String> recentDatasets = screenWatcher.getRecentDatasets ();
    if (recentDatasets.size () == 0)
    {
      Dm3270Utility.showAlert ("No datasets to download");
      return;
    }

    Field tsoCommandField = screenWatcher.getTSOCommandField ();
    if (tsoCommandField == null)
    {
      Dm3270Utility.showAlert ("This screen has no TSO input field");
      return;
    }

    String userHome = System.getProperty ("user.home");
    int baseLength = userHome.length () + 1;
    TransferDialog transferDialog = null;

    if (transferType == TransferType.DOWNLOAD)
    {
      if (downloadDialog == null)
        downloadDialog = new DownloadDialog (screenWatcher, homePath, baseLength);
      transferDialog = downloadDialog;
    }
    else
    {
      if (uploadDialog == null)
        uploadDialog = new UploadDialog (screenWatcher, homePath, baseLength);
      transferDialog = uploadDialog;
    }

    Optional<IndFileCommand> optCommand = transferDialog.showAndWait ();
    if (!optCommand.isPresent ())
      return;

    IndFileCommand indFileCommand = optCommand.get ();
    String commandText = indFileCommand.getCommand ();

    if (commandText.length () > tsoCommandField.getDisplayLength ())
    {
      Dm3270Utility.showAlert ("Command is too long for the TSO input field");
      System.out.printf ("Field: %d, command: %d%n", tsoCommandField.getDisplayLength (),
                         commandText.length ());
      return;
    }

    // this should be a single call to transferManager
    transferManager.prepareTransfer (indFileCommand);
    tsoCommandField.setText (commandText);
    consolePane.sendAID (AIDCommand.AID_ENTER, "ENTR");
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