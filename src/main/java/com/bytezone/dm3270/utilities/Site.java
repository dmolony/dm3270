package com.bytezone.dm3270.utilities;

public class Site {

  public final String name;
  public final boolean extended;
  private final String url;
  private int port;

  public Site(String name, String url, int port, boolean extended) {
    this.name = name;
    this.url = url;
    this.port = port;
    this.extended = extended;
  }

  public String getName() {
    return name;
  }

  public String getURL() {
    return url;
  }

  public int getPort() {
    if (port <= 0) {
      System.out.println("Invalid port value: " + port);
      port = 23;
    }
    return port;
  }

  public boolean getExtended() {
    return extended;
  }

  @Override
  public String toString() {
    return String.format("Site [name=%s, url=%s, port=%d]", getName(), getURL(), getPort());
  }

}
