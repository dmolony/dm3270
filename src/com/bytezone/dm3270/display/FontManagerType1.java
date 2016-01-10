package com.bytezone.dm3270.display;

import java.util.List;
import java.util.prefs.Preferences;

import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Font;

class FontManagerType1 implements FontManager
{
  private static final String[] fontNames =
      { "Andale Mono", "Anonymous Pro", "Consolas", "Courier New", "DejaVu Sans Mono",
        "Hack", "Hermit", "IBM 3270", "IBM 3270 Narrow", "Inconsolata", "Input Mono",
        "Input Mono Narrow", "Lucida Sans Typewriter", "Luculent", "Menlo", "Monaco",
        "M+ 1m", "Panic Sans", "PT Mono", "Source Code Pro", "Ubuntu Mono",
        "Monospaced" };
  private static final int[] fontSizes = { 9, 10, 12, 14, 15, 16, 17, 18, 20, 22 };

  private final ToggleGroup fontGroup = new ToggleGroup ();
  private final ToggleGroup sizeGroup = new ToggleGroup ();
  private final RadioMenuItem[] fontSizeItems = new RadioMenuItem[fontSizes.length];
  private final Menu menuFont;

  private final Screen screen;
  private FontDetails fontDetails;
  private Font statusBarFont;

  FontManagerType1 (Screen screen, Preferences prefs)
  {
    this.screen = screen;

    String nameSelected = prefs.get ("FontName", "Monospaced");
    String sizeSelected = prefs.get ("FontSize", "16");
    setFont (nameSelected, Integer.parseInt (sizeSelected));

    menuFont = getMenu ();
  }

  // called from ConsolePane.setHistoryBar()
  // called from ConsolePane.getStatusBar()
  @Override
  public Font getStatusBarFont ()
  {
    if (statusBarFont == null)
      statusBarFont = Font.font ("Monospaced", 14);
    return statusBarFont;
  }

  @Override
  public FontDetails getFontDetails ()
  {
    return fontDetails;
  }

  @Override
  public String getFontName ()
  {
    return fontDetails.name;
  }

  @Override
  public int getFontSize ()
  {
    return fontDetails.size;
  }

  @Override
  public Menu getFontMenu ()
  {
    return menuFont;
  }

  private Menu getMenu ()
  {
    String fontSelected = getFontName ();
    String sizeSelected = "" + getFontSize ();

    Menu menuFont = new Menu ("Fonts");

    // add font names
    List<String> families = Font.getFamilies ();
    for (String fontName : fontNames)
    {
      boolean fontExists = families.contains (fontName);
      if (fontExists && fontSelected.isEmpty ())
        fontSelected = fontName;
      setMenuItem (menuFont, fontGroup, fontName, fontSelected, !fontExists);
    }

    // select Monospaced if there is still no font selected
    if (fontGroup.getSelectedToggle () == null)
    {
      ObservableList<Toggle> toggles = fontGroup.getToggles ();
      fontGroup.selectToggle (toggles.get (toggles.size () - 1));
    }

    // add increase/decrease size commands
    menuFont.getItems ().add (new SeparatorMenuItem ());

    MenuItem smaller = new MenuItem ("Smaller font");
    smaller.setAccelerator (new KeyCodeCombination (KeyCode.MINUS,
        KeyCombination.SHORTCUT_DOWN));
    smaller.setOnAction (e -> smaller ());

    MenuItem bigger = new MenuItem ("Larger font");
    bigger.setAccelerator (new KeyCodeCombination (KeyCode.PLUS,
        KeyCombination.SHORTCUT_DOWN));
    bigger.setOnAction (e -> bigger ());

    menuFont.getItems ().addAll (smaller, bigger, new SeparatorMenuItem ());

    // add font sizes
    int count = 0;
    for (int fontSize : fontSizes)
      fontSizeItems[count++] =
          setMenuItem (menuFont, sizeGroup, fontSize + "", sizeSelected, false);

    return menuFont;
  }

  private RadioMenuItem setMenuItem (Menu menu, ToggleGroup toggleGroup, String itemName,
      String selectedItemName, boolean disable)
  {
    RadioMenuItem item = new RadioMenuItem (itemName);
    item.setToggleGroup (toggleGroup);
    menu.getItems ().add (item);
    if (itemName.equals (selectedItemName))
      item.setSelected (true);
    item.setDisable (disable);
    item.setUserData (itemName);
    item.setOnAction (e -> selectFont ());
    return item;
  }

  @Override
  public void smaller ()
  {
    int selectedSize = getSelectedSize ();
    if (selectedSize == fontSizes[0])
      return;

    for (int i = 1; i < fontSizes.length; i++)
      if (fontSizes[i] == selectedSize)
      {
        sizeGroup.selectToggle (fontSizeItems[i - 1]);
        fontSizeItems[i - 1].fire ();
        break;
      }
  }

  @Override
  public void bigger ()
  {
    int selectedSize = getSelectedSize ();
    int lengthMinusOne = fontSizes.length - 1;
    if (selectedSize == fontSizes[lengthMinusOne])
      return;

    for (int i = 0; i < lengthMinusOne; i++)
      if (fontSizes[i] == selectedSize)
      {
        sizeGroup.selectToggle (fontSizeItems[i + 1]);
        fontSizeItems[i + 1].fire ();
        break;
      }
  }

  private int getSelectedSize ()
  {
    return Integer.parseInt ((String) sizeGroup.getSelectedToggle ().getUserData ());
  }

  private String getSelectedFont ()
  {
    return (String) fontGroup.getSelectedToggle ().getUserData ();
  }

  // called when the user selects a font or size from the menu
  private void selectFont ()
  {
    String name = getSelectedFont ();
    int size = getSelectedSize ();
    if (name.equals (fontDetails.name) && size == fontDetails.size)
      return;

    setFont (name, size);
  }

  private void setFont (String name, int size)
  {
    fontDetails = new FontDetails (name, size, Font.font (name, size));
    statusBarFont = Font.font (name, size - 2);
    screen.fontChanged (fontDetails);
  }
}