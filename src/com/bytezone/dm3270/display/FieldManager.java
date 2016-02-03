package com.bytezone.dm3270.display;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import com.bytezone.dm3270.plugins.PluginData;
import com.bytezone.dm3270.plugins.PluginField;
import com.bytezone.dm3270.plugins.ScreenLocation;

public class FieldManager
{
  private final Screen screen;
  private ScreenWatcher screenWatcher;
  private final ContextManager contextManager;
  private ScreenDimensions screenDimensions;

  private final List<Field> fields = new ArrayList<> ();
  private final List<Field> unprotectedFields = new ArrayList<> ();
  private final List<Field> emptyFields = new ArrayList<> ();

  private int dataPositions;
  private int inputPositions;
  private int hiddenProtectedFields;
  private int hiddenUnprotectedFields;

  FieldManager (Screen screen, ContextManager contextManager,
      ScreenDimensions screenDimensions)
  {
    this.screen = screen;
    this.contextManager = contextManager;
    this.screenDimensions = screenDimensions;
    screenWatcher = new ScreenWatcher (this, screenDimensions);
  }

  // ScreenWatcher is never deleted, but most (not all) of its fields are refreshed
  public ScreenWatcher getScreenWatcher ()
  {
    return screenWatcher;
  }

  void setScreenDimensions (ScreenDimensions screenDimensions)
  {
    this.screenDimensions = screen.getScreenDimensions ();
    screenWatcher = new ScreenWatcher (this, screenDimensions);
  }

  // called by Screen.clearScreen()
  void reset ()
  {
    fields.clear ();
    unprotectedFields.clear ();
    emptyFields.clear ();
  }

  // this is called after the pen and screen positions have been modified
  void buildFields (ScreenPosition[] screenPositions)
  {
    reset ();

    dataPositions = 0;
    inputPositions = 0;
    hiddenProtectedFields = 0;
    hiddenUnprotectedFields = 0;

    for (List<ScreenPosition> protoField : divide (screenPositions))
    {
      addField (new Field (screen, protoField));
      setContexts (protoField);
    }

    //    assert dataPositions + fields.size () == 1920 || fields.size () == 0;

    // link uprotected fields
    Field previousUnprotectedField = null;

    for (Field field : fields)
      if (field.isUnprotected ())
      {
        unprotectedFields.add (field);
        if (previousUnprotectedField != null)
          previousUnprotectedField.linkToNext (field);
        previousUnprotectedField = field;
      }

    if (unprotectedFields.size () > 0)
    {
      // link first unprotected field to the last one
      Field firstField = unprotectedFields.get (0);
      Field lastField = unprotectedFields.get (unprotectedFields.size () - 1);
      lastField.linkToNext (firstField);

      // link protected fields to unprotected fields
      Field prev = lastField;
      Field next = firstField;

      for (Field field : fields)
        if (field.isProtected ())
        {
          field.setNext (next);
          field.setPrevious (prev);
        }
        else
        {
          next = field.getNextUnprotectedField ();
          prev = field;
        }
    }

    screenWatcher.check ();
    fireScreenChanged (screenWatcher);
  }

  private void addField (Field field)
  {
    fields.add (field);

    dataPositions += field.getDisplayLength ();

    if (field.getDisplayLength () == 0)
      emptyFields.add (field);

    if (field.isUnprotected ())
      inputPositions += field.getDisplayLength ();

    if (field.isHidden ())
      if (field.isProtected ())
        ++hiddenProtectedFields;
      else
        ++hiddenUnprotectedFields;
  }

  public Optional<Field> getFieldAt (int position)        // should this be indexed?
  {
    return fields.parallelStream ().filter (f -> f.contains (position)).findAny ();
  }

  public List<Field> getUnprotectedFields ()
  {
    return unprotectedFields;
  }

  public List<Field> getFields ()
  {
    return fields;
  }

  public int size ()
  {
    return fields.size ();
  }

  // called from Screen.eraseAllUnprotected()
  Optional<Field> eraseAllUnprotected ()
  {
    unprotectedFields.parallelStream ().forEach (f -> f.clearData (true));

    return unprotectedFields.stream ().findFirst ();
  }

  // ---------------------------------------------------------------------------------//
  // Field utilities
  // ---------------------------------------------------------------------------------//

  List<Field> getRowFields (int requestedRow)
  {
    int firstLocation = requestedRow * screenDimensions.columns;
    int lastLocation = firstLocation + screenDimensions.columns - 1;
    return getFieldsInRange (firstLocation, lastLocation);
  }

  List<Field> getRowFields (int requestedRowFrom, int rows)
  {
    int firstLocation = requestedRowFrom * screenDimensions.columns;
    int lastLocation = (requestedRowFrom + rows) * screenDimensions.columns - 1;
    return getFieldsInRange (firstLocation, lastLocation);
  }

  private List<Field> getFieldsInRange (int firstLocation, int lastLocation)
  {
    List<Field> rowFields = new ArrayList<> ();
    for (Field field : fields)
    {
      int location = field.getFirstLocation ();
      if (location < firstLocation)
        continue;
      if (location > lastLocation)
        break;
      if (field.getDisplayLength () > 0)
        rowFields.add (field);
    }
    return rowFields;
  }

  boolean textMatches (Field field, String text)
  {
    return text.equals (field.getText ());
  }

  boolean textMatches (int fieldNo, String text)
  {
    return text.equals (fields.get (fieldNo).getText ());
  }

  boolean textMatches (Field field, String text, int location)
  {
    return field.getFirstLocation () == location && text.equals (field.getText ());
  }

  boolean textMatches (int fieldNo, String text, int location)
  {
    Field field = fields.get (fieldNo);
    return field.getFirstLocation () == location && text.equals (field.getText ());
  }

  boolean textMatchesTrim (Field field, String text)
  {
    return text.equals (field.getText ().trim ());
  }

  boolean textMatchesTrim (Field field, String text, int location)
  {
    return field.getFirstLocation () == location
        && text.equals (field.getText ().trim ());
  }

  List<String> getMenus ()
  {
    List<String> menus = new ArrayList<> ();

    for (Field field : fields)
    {
      if (field.getFirstLocation () >= screenDimensions.columns)
        break;

      if (field.isProtected () && field.isVisible () && field.getDisplayLength () > 1)
      {
        String text = field.getText ().trim ();
        if (!text.isEmpty ())
          menus.add (text);
      }
    }

    return menus;
  }

  // ---------------------------------------------------------------------------------//
  // Convert internal Fields to ScreenFields for use by plugins
  // ---------------------------------------------------------------------------------//

  public PluginData getPluginScreen (int sequence, ScreenLocation screenLocation)
  {
    List<PluginField> pluginFields = new ArrayList<> ();
    int count = 0;

    for (Field field : fields)
      pluginFields.add (field.getPluginField (sequence, count++));

    return new PluginData (sequence, screenLocation, pluginFields);
  }

  // ---------------------------------------------------------------------------------//
  // ScreenChangeListeners
  // ---------------------------------------------------------------------------------//

  private final Set<ScreenChangeListener> screenChangeListeners = new HashSet<> ();

  private void fireScreenChanged (ScreenWatcher screenWatcher)
  {
    screenChangeListeners.forEach (listener -> listener.screenChanged (screenWatcher));
  }

  public void addScreenChangeListener (ScreenChangeListener listener)
  {
    if (!screenChangeListeners.contains (listener))
      screenChangeListeners.add (listener);
  }

  public void removeScreenChangeListener (ScreenChangeListener listener)
  {
    if (screenChangeListeners.contains (listener))
      screenChangeListeners.remove (listener);
  }

  // ---------------------------------------------------------------------------------//
  // Divide the ScreenPositions into fields
  // ---------------------------------------------------------------------------------//

  static List<List<ScreenPosition>> divide (ScreenPosition[] screenPositions)
  {
    List<List<ScreenPosition>> components = new ArrayList<> ();
    List<ScreenPosition> positions = new ArrayList<ScreenPosition> ();

    int start = -1;
    int first = -1;
    int ptr = 0;

    while (ptr != first)                    // not wrapped around to the first field yet
    {
      ScreenPosition screenPosition = screenPositions[ptr];

      if (screenPosition.isStartField ())   // check for the start of a new field
      {
        if (start >= 0)                     // if there is a field to add
        {
          components.add (new ArrayList<> (positions));
          positions.clear ();
        }
        else
          first = ptr;                      // this is the first field on the screen

        start = ptr;                        // beginning of the current field
      }

      if (start >= 0)                       // if we are in a field...
        positions.add (screenPosition);     // collect next field's positions

      // increment ptr and wrap around
      if (++ptr == screenPositions.length)  // faster than validate()
      {
        ptr = 0;
        if (first == -1)
          break;                            // wrapped around and still no fields
      }
    }

    if (start >= 0 && positions.size () > 0)
      components.add (new ArrayList<> (positions));

    return components;
  }

  // ---------------------------------------------------------------------------------//
  // Process a field's ScreenPositions
  // ---------------------------------------------------------------------------------//

  void setContexts (List<ScreenPosition> positions)
  {
    StartFieldAttribute startFieldAttribute = positions.get (0).getStartFieldAttribute ();
    ScreenContext defaultContext =
        startFieldAttribute.process (contextManager, null, null);

    if (startFieldAttribute.isExtended ())
      setExtendedContext (defaultContext, positions);
    else
      positions.forEach (sp -> sp.setScreenContext (defaultContext));

    if (startFieldAttribute.isHidden ())
      positions.forEach (sp -> sp.setVisible (false));
  }

  private void setExtendedContext (ScreenContext defaultContext,
      List<ScreenPosition> positions)
  {
    boolean first = true;
    ScreenContext currentContext = defaultContext;

    for (ScreenPosition screenPosition : positions)
    {
      for (Attribute attribute : screenPosition.getAttributes ())
        currentContext =
            attribute.process (contextManager, defaultContext, currentContext);

      if (first)
      {
        first = false;
        defaultContext = currentContext;
      }
      screenPosition.setScreenContext (currentContext);
    }
  }

  // ---------------------------------------------------------------------------------//
  // Debugging
  // ---------------------------------------------------------------------------------//

  public String getTotalsText ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Start fields     : %4d%n", fields.size ()));
    text.append (String.format ("  Zero length    : %4d%n", emptyFields.size ()));
    text.append (String.format ("  Unprotected    : %4d   (%d hidden)%n",
                                unprotectedFields.size (), hiddenUnprotectedFields));
    text.append (String.format ("  Protected      : %4d   (%d hidden)%n%n",
                                fields.size () - unprotectedFields.size (),
                                hiddenProtectedFields));

    text.append (String.format ("Screen positions : %4d%n",
                                dataPositions + fields.size ()));
    text.append (String.format ("  Attributes     : %4d%n", fields.size ()));
    text.append (String.format ("  Output         : %4d%n",
                                dataPositions - inputPositions));
    text.append (String.format ("  Input          : %4d", inputPositions));

    return text.toString ();
  }

  public String getFieldsText ()
  {
    StringBuilder text = new StringBuilder ();

    int count = 0;
    for (Field field : fields)
      text.append (String.format ("%4d %s%n", count++, field));

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}