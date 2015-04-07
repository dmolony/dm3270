package com.bytezone.dm3270.application;

import javafx.scene.control.TextArea;

class Site
{
  public final TextArea name = new TextArea ();
  public final TextArea url = new TextArea ();
  public final TextArea port = new TextArea ();

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