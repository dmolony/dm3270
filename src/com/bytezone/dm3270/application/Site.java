package com.bytezone.dm3270.application;

import javafx.scene.control.TextField;

class Site
{
  public final TextField name = new TextField ();
  public final TextField url = new TextField ();
  public final TextField port = new TextField ();

  public Site (String name, String url, int port)
  {
    this.name.setText (name);
    this.url.setText (url);
    this.port.setText (port + "");
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
    return Integer.parseInt (port.getText ());
  }
}