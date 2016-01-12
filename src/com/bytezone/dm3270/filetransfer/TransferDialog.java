package com.bytezone.dm3270.filetransfer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.display.ScreenWatcher;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;

public class TransferDialog
{
  protected static final DateFormat df = new SimpleDateFormat ("dd/MM/yyyy HH:mm:ss");
  protected static final Pattern jclPattern =
      Pattern.compile (".*\\.(CNTL|JCL)[.(].*\\)");
  protected static final Pattern procPattern =
      Pattern.compile (".*\\.(PROC|PARM)LIB[.(].*\\)");

  final Dialog<IndFileCommand> dialog = new Dialog<> ();

  protected final ScreenWatcher screenWatcher;
  protected final Path homePath;
  protected final int baseLength;

  protected final ComboBox<String> datasetComboBox = new ComboBox<> ();

  public TransferDialog (ScreenWatcher screenWatcher, Path homePath, int baseLength)
  {
    this.screenWatcher = screenWatcher;
    this.homePath = homePath;
    this.baseLength = baseLength;

    refresh ();
  }

  protected void refresh ()
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