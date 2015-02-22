package com.bytezone.dm3270.application;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.orders.BufferAddress;
import com.bytezone.dm3270.orders.BufferAddressSource;

public class Cursor implements Iterable<Attribute>
{
  private int location;
  private boolean visible;

  private final ScreenPosition[] screenPositions;
  private final ScreenCanvas canvas;
  private final int size;
  private final int columns;
  private final List<Attribute> unappliedAttributes = new ArrayList<> ();
  private ConsoleStage consoleStage;
  private final ScreenHandler screenHandler;

  public Cursor (ScreenHandler screenHandler, ScreenPosition[] screenPositions,
      ScreenCanvas terminalScreen)
  {
    this.screenPositions = screenPositions;
    this.canvas = terminalScreen;
    this.screenHandler = screenHandler;

    size = screenHandler.getScreenSize ();
    columns = screenHandler.getColumns ();
  }

  // useful functions to add:
  //   ScreenField getCurrentField ();
  //   int getCurrentFieldPosition ();  -1 : not in a field, 0 : attribute byte, 1-len

  public void setConsoleStage (ConsoleStage consoleStage)
  {
    this.consoleStage = consoleStage;
  }

  public void setVisible (boolean visible)
  {
    this.visible = visible;
    canvas.draw (screenPositions[location]);
  }

  public boolean isVisible ()
  {
    return visible;
  }

  public void addAttribute (Attribute attribute)
  {
    unappliedAttributes.add (attribute);
  }

  public boolean hasUnappliedAttributes ()
  {
    return unappliedAttributes.size () > 0;
  }

  @Override
  public Iterator<Attribute> iterator ()
  {
    return unappliedAttributes.iterator ();
  }

  public void clearAttributes ()
  {
    unappliedAttributes.clear ();
  }

  public void setLocation (int location)
  {
    this.location = location;
    validate ();
  }

  public void setAddress (BufferAddressSource addressSource)
  {
    setLocation (addressSource.getBufferAddress ().getLocation ());
  }

  public void setAddress (BufferAddress address)
  {
    setLocation (address.getLocation ());
  }

  public int getLocation ()
  {
    return location;
  }

  public BufferAddress getAddress ()
  {
    return new BufferAddress (location);
  }

  public ScreenPosition getScreenPosition ()
  {
    return screenPositions[location];
  }

  private void validate ()
  {
    while (location < 0)
      location += size;
    while (location >= size)
      location -= size;

    if (consoleStage != null)
      consoleStage.setCursorLocation (location / columns, location % columns);
  }

  private void processUnappliedAttributes ()
  {
    for (Attribute attribute : unappliedAttributes)
      screenPositions[location].addAttribute (attribute);
    clearAttributes ();
  }

  public int moveLeft ()
  {
    location -= 1;
    validate ();
    return location;
  }

  public int moveRight ()
  {
    processUnappliedAttributes ();

    location += 1;
    validate ();
    return location;
  }

  public int moveUp ()
  {
    location -= columns;
    validate ();
    return location;
  }

  public int moveDown ()
  {
    location += columns;
    validate ();
    return location;
  }

  public int getLeft ()
  {
    int leftLocation = moveLeft ();
    moveRight ();
    return leftLocation;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();
    text.append ("Cursor: \n");
    text.append (String.format ("  location.... %d%n", location));
    text.append (String.format ("  visible..... %s%n", visible));
    return text.toString ();
  }
}