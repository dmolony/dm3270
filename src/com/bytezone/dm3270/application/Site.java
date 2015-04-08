package com.bytezone.dm3270.application;

import javafx.scene.control.TextField;

class Site
{
  public final TextField name = new TextField ();
  public final TextField url = new TextField ();
  public final TextField port = new TextField ();
  TextField[] fieldList = { name, url, port };

  public Site (String name, String url, int port)
  {
    this.name.setText (name);
    this.url.setText (url);
    this.port.setText (port == 23 && name.isEmpty () ? "" : port + "");
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

  public TextField getTextField (int index)
  {
    return fieldList[index];
  }

  @Override
  public String toString ()
  {
    return String.format ("Site [name=%s, url=%s, port=%d]", getName (), getURL (),
                          getPort ());
  }
}