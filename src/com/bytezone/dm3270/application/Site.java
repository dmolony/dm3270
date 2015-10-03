package com.bytezone.dm3270.application;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class Site
{
  public final TextField name = new TextField ();
  public final TextField url = new TextField ();
  public final TextField port = new TextField ();
  public final CheckBox extended = new CheckBox ();
  public final CheckBox plugins = new CheckBox ();
  public final TextField folder = new TextField ();

  private final TextField[] textFieldList = { name, url, port, null, null, folder };
  private final CheckBox[] checkBoxFieldList =
      { null, null, null, extended, plugins, null };

  public Site (String name, String url, int port, boolean extended, boolean plugins,
      String folder)
  {
    this.name.setText (name);
    this.url.setText (url);
    this.port.setText (port == 23 && name.isEmpty () ? "" : port + "");
    this.extended.setSelected (extended);
    this.plugins.setSelected (plugins);
    this.folder.setText (folder);
  }

  public String getName ()
  {
    return name.getText ();
  }

  public String getURL ()
  {
    return url.getText ();
  }

  public int getPort ()
  {
    try
    {
      int portValue = Integer.parseInt (port.getText ());
      if (portValue <= 0)
      {
        System.out.println ("Invalid port value: " + port.getText ());
        port.setText ("23");
        portValue = 23;
      }
      return portValue;
    }
    catch (NumberFormatException e)
    {
      System.out.println ("Invalid port value: " + port.getText ());
      port.setText ("23");
      return 23;
    }
  }

  public boolean getExtended ()
  {
    return extended.isSelected ();
  }

  public boolean getPlugins ()
  {
    return plugins.isSelected ();
  }

  public String getFolder ()
  {
    return folder.getText ();
  }

  public TextField getTextField (int index)
  {
    return textFieldList[index];
  }

  public CheckBox getCheckBoxField (int index)
  {
    return checkBoxFieldList[index];
  }

  @Override
  public String toString ()
  {
    return String.format ("Site [name=%s, url=%s, port=%d, folder=%s]", getName (),
                          getURL (), getPort (), getFolder ());
  }
}