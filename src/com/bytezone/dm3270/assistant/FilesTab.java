package com.bytezone.dm3270.assistant;

import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.reporter.application.FileNode;
import com.bytezone.reporter.application.NodeSelectionListener;
import com.bytezone.reporter.application.ReporterNode;
import com.bytezone.reporter.file.ReportData;

import javafx.scene.control.MenuBar;

public class FilesTab extends AbstractTransferTab implements NodeSelectionListener
{
  private final ReporterNode reporterNode;
  private FileNode currentFileNode;

  public FilesTab (Screen screen, TSOCommand tsoCommand, Preferences prefs)
  {
    super ("Local Files", screen, tsoCommand);

    reporterNode = new ReporterNode (prefs);
    reporterNode.addNodeSelectionListener (this);
    reporterNode.requestFocus ();

    currentFileNode = reporterNode.getSelectedNode ();

    setContent (reporterNode);
  }

  @Override
  public void nodeSelected (FileNode fileNode)
  {
    currentFileNode = fileNode;
    setText ();
    fireFileSelected (currentFileNode.toString ());
  }

  MenuBar getMenuBar ()
  {
    return reporterNode.getMenuBar ();
  }

  ReporterNode getReporterNode ()
  {
    return reporterNode;
  }

  @Override
  protected void setText ()
  {
    if (currentFileNode == null)
    {
      eraseCommand ();
      return;
    }

    String prefix = screenWatcher == null ? "" : screenWatcher.getPrefix () + ".";
    String fileName = currentFileNode.toString ().toUpperCase ();
    if (fileName.endsWith (".TXT"))
      fileName = fileName.substring (0, fileName.length () - 4);

    if (!prefix.isEmpty () && fileName.startsWith (prefix))
      fileName = fileName.substring (prefix.length ());

    String command = "IND$FILE PUT " + fileName;

    if (currentFileNode.isAscii ())
      command += " ASCII CRLF";

    if (screenWatcher != null && !screenWatcher.isTSOCommandScreen ())
      command = "TSO " + command;

    tsoCommand.txtCommand.setText (command);
    ReportData reportData = currentFileNode.getReportData ();
    tsoCommand.setBuffer (reportData.getBuffer (), currentFileNode.getFile ());
    setButton ();
  }

  // ---------------------------------------------------------------------------------//
  // fileSelected() Listener events
  // ---------------------------------------------------------------------------------//

  private final Set<FileSelectionListener> selectionListeners = new HashSet<> ();

  void fireFileSelected (String filename)
  {
    selectionListeners.forEach (l -> l.fileSelected (filename));
  }

  void addFileSelectionListener (FileSelectionListener listener)
  {
    if (!selectionListeners.contains (listener))
    {
      selectionListeners.add (listener);
      if (currentFileNode != null)
        listener.fileSelected (currentFileNode.toString ());
    }
  }

  void removeFileSelectionListener (FileSelectionListener listener)
  {
    if (selectionListeners.contains (listener))
      selectionListeners.remove (listener);
  }
}