package com.bytezone.dm3270.display;

import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Font;

public class FontManager
{
  private static String[] preferredFontNames = { //
      "Andale Mono", "Anonymous Pro", "Consolas", "Courier New", "DejaVu Sans Mono",
          "Hermit", "IBM 3270", "IBM 3270 Narrow", "Inconsolata", "Input Mono",
          "Input Mono Narrow", "Luculent", "Menlo", "Monaco", "M+ 2m", "PT Mono",
          "Source Code Pro", "Monospaced" };

  private final ToggleGroup fontGroup = new ToggleGroup ();
  private final ToggleGroup sizeGroup = new ToggleGroup ();
  private Font defaultFont;
  private final Screen screen;

  public FontManager (Screen screen)
  {
    this.screen = screen;
  }

  public Menu getFontMenu ()
  {
    String fontSelected = screen.getFontName ();
    String sizeSelected = "" + screen.getFontSize ();

    Menu menuFont = new Menu ("Fonts");

    List<String> families = Font.getFamilies ();
    for (String fontName : preferredFontNames)
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

    menuFont.getItems ().add (new SeparatorMenuItem ());
    String[] menuSizes = { "12", "14", "15", "16", "17", "18", "20", "22" };
    for (String menuSize : menuSizes)
      setMenuItem (menuSize, sizeGroup, menuFont, sizeSelected, false);

    return menuFont;
  }

  public Font getDefaultFont ()
  {
    return defaultFont == null ? Font.font ("Monospaced", 14) : defaultFont;
  }

  private void setMenuItem (String itemName, ToggleGroup toggleGroup, Menu menu,
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
  }

  private void selectFont ()
  {
    String fontName = (String) fontGroup.getSelectedToggle ().getUserData ();
    int fontSize =
        Integer.parseInt ((String) sizeGroup.getSelectedToggle ().getUserData ());
    screen.adjustFont (fontName, fontSize);
  }
}