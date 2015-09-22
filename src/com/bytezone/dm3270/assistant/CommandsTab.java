package com.bytezone.dm3270.assistant;

import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenDetails;
import com.bytezone.dm3270.display.TSOCommandListener;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class CommandsTab extends AbstractTransferTab
    implements TSOCommandListener, ScreenChangeListener
{
  ObservableList<String> commands = FXCollections.observableArrayList ();
  ListView<String> commandList = new ListView<> (commands);

  public CommandsTab (Screen screen, TextField text, Button execute)
  {
    super ("Commands", screen, text, execute);

    commandList.setStyle ("-fx-font-size: 12; -fx-font-family: Monospaced");
    setContent (commandList);
    commandList.getSelectionModel ().selectedItemProperty ()
        .addListener ( (obs, oldSelection, newSelection) -> setText ());
  }

  @Override
      void setText ()
  {
    String selectedCommand = commandList.getSelectionModel ().getSelectedItem ();
    ScreenDetails screenDetails = screen.getScreenDetails ();
    if (selectedCommand == null)
    {
      eraseCommand ();
      return;
    }

    txtCommand.setText (selectedCommand);
    btnExecute.setDisable (screen.isKeyboardLocked ()
        || screenDetails.getTSOCommandField () == null);
  }

  @Override
  public void tsoCommand (String command)
  {
    if (command.startsWith ("="))
      return;

    ScreenDetails screenDetails = screen.getScreenDetails ();

    if (screenDetails.isTSOCommandScreen () || command.toUpperCase ().startsWith ("TSO "))
      if (!commands.contains (command))
        commands.add (command);

    if (isSelected ())
      setText ();
  }

  @Override
  public void screenChanged ()
  {
    if (isSelected ())
      setText ();
  }
}