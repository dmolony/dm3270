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
import javafx.stage.Stage;

public class FontManager
{
  private static final String[] fontNames = { //
      "Andale Mono", "Anonymous Pro", "Consolas", "Courier New", "DejaVu Sans Mono",
          "Hermit", "IBM 3270", "IBM 3270 Narrow", "Inconsolata", "Input Mono",
          "Input Mono Narrow", "Luculent", "Menlo", "Monaco", "M+ 1m", "Panic Sans",
          "PT Mono", "Source Code Pro", "Ubuntu Mono", "Monospaced" };
  private static final int[] fontSizes = { 10, 12, 14, 15, 16, 17, 18, 20, 22 };

  private final ToggleGroup fontGroup = new ToggleGroup ();
  private final ToggleGroup sizeGroup = new ToggleGroup ();
  private Font defaultFont;
  private final Screen screen;
  private final RadioMenuItem[] fontSizeItems = new RadioMenuItem[fontSizes.length];
  private final CharacterSize characterSize;

  public FontManager (Screen screen, Preferences prefs)
  {
    this.screen = screen;
    characterSize = new CharacterSize ();

    String fontSelected = prefs.get ("FontName", "Monospaced");
    String sizeSelected = prefs.get ("FontSize", "16");
    setFont (fontSelected, Integer.parseInt (sizeSelected));
  }

  public CharacterSize getCharacterSize ()
  {
    return characterSize;
  }

  public Menu getFontMenu ()
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

    menuFont.getItems ().addAll (smaller, bigger, new SeparatorMenuItem ());

    // add font sizes
    int count = 0;
    for (int fontSize : fontSizes)
      fontSizeItems[count++] =
          setMenuItem (menuFont, sizeGroup, fontSize + "", sizeSelected, false);

    return menuFont;
  }

  public Font getDefaultFont ()
  {
    return defaultFont == null ? Font.font ("Monospaced", 14) : defaultFont;
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

  public void smaller ()
  {
    int selectedSize = getSelectedSize ();
    for (int i = 1; i < fontSizes.length; i++)
      if (fontSizes[i] == selectedSize)
      {
        sizeGroup.selectToggle (fontSizeItems[i - 1]);
        fontSizeItems[i - 1].fire ();
        break;
      }
  }

  private void bigger ()
  {
    int selectedSize = getSelectedSize ();
    for (int i = 0; i < fontSizes.length - 1; i++)
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

  private void selectFont ()
  {
    String name = getSelectedFont ();
    int size = getSelectedSize ();
    if (name.equals (characterSize.getName ()) && size == characterSize.getSize ())
      return;

    setFont (name, size);
    ((Stage) screen.getScene ().getWindow ()).sizeToScene ();

    screen.eraseScreen ();
    screen.drawScreen ();
  }

  private void setFont (String name, int size)
  {
    characterSize.changeFont (name, size);
    screen.characterSizeChanged (characterSize);
  }

  public String getFontName ()
  {
    return characterSize.getName ();
  }

  public int getFontSize ()
  {
    return characterSize.getSize ();
  }
}