package com.bytezone.dm3270.display;

import java.util.List;

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

public class FontManager
{
  private static final String[] fontNames = { //
      "Andale Mono", "Anonymous Pro", "Consolas", "Courier New", "DejaVu Sans Mono",
          "Hermit", "IBM 3270", "IBM 3270 Narrow", "Inconsolata", "Input Mono",
          "Input Mono Narrow", "Luculent", "Menlo", "Monaco", "M+ 2m", "PT Mono",
          "Source Code Pro", "Monospaced" };
  private static final int[] fontSizes = { //
      12, 14, 15, 16, 17, 18, 20, 22 };

  private final ToggleGroup fontGroup = new ToggleGroup ();
  private final ToggleGroup sizeGroup = new ToggleGroup ();
  private Font defaultFont;
  private final Screen screen;
  private final RadioMenuItem[] fontSizeItems = new RadioMenuItem[fontSizes.length];

  public FontManager (Screen screen)
  {
    this.screen = screen;
  }

  public Menu getFontMenu ()
  {
    String fontSelected = screen.getFontName ();
    String sizeSelected = "" + screen.getFontSize ();

    Menu menuFont = new Menu ("Fonts");

    // add font names
    List<String> families = Font.getFamilies ();
    for (String fontName : fontNames)
    {
      boolean fontExists = families.contains (fontName);
      if (fontExists && fontSelected.isEmpty ())
        fontSelected = fontName;
      setMenuItem (fontName, fontGroup, menuFont, fontSelected, !fontExists);
    }

    if (!fontSelected.isEmpty ())
      defaultFont = Font.font (fontSelected, 14);

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
    menuFont.getItems ().addAll (smaller, bigger);

    // add font sizes
    menuFont.getItems ().add (new SeparatorMenuItem ());
    int count = 0;
    for (int fontSize : fontSizes)
      fontSizeItems[count++] =
          setMenuItem (fontSize + "", sizeGroup, menuFont, sizeSelected, false);

    return menuFont;
  }

  public Font getDefaultFont ()
  {
    return defaultFont == null ? Font.font ("Monospaced", 14) : defaultFont;
  }

  private RadioMenuItem setMenuItem (String itemName, ToggleGroup toggleGroup, Menu menu,
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

  private void smaller ()
  {
    int selectedSize = getSelectedSize ();
    for (int i = 0; i < fontSizes.length; i++)
    {
      if (fontSizes[i] == selectedSize)
      {
        if (i == 0)
          return;
        sizeGroup.selectToggle (fontSizeItems[i - 1]);
        fontSizeItems[i - 1].fire ();
        break;
      }
    }
  }

  private void bigger ()
  {
    int selectedSize = getSelectedSize ();
    for (int i = 0; i < fontSizes.length; i++)
    {
      if (fontSizes[i] == selectedSize)
      {
        if (i == fontSizes.length - 1)
          return;
        sizeGroup.selectToggle (fontSizeItems[i + 1]);
        fontSizeItems[i + 1].fire ();
        break;
      }
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

  private void selectFont ()
  {
    screen.adjustFont (getSelectedFont (), getSelectedSize ());
  }
}