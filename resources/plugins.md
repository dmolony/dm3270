### Plugins (incomplete)
This facility allows users to write their own plugin java modules which are able to examine and process all 3270 input screens. After each screen is displayed all active plugins are called with a list of the fields on the screen. Modifiable fields may be altered, the cursor may be moved and any of the usual command or function keys may be pressed. Active plugins can also be triggered by a command key at any time.  
#### Creation
Implement the Plugin interface or extend DefaultPlugin.
```
package com.bytezone.dm3270.plugins;

public interface Plugin
{
  default void activate ()
  {
  }

  default void deactivate ()
  {
  }

  default boolean doesAuto ()
  {
    return false;
  }

  default boolean doesRequest ()
  {
    return false;
  }

  default void processAuto (PluginData screen)
  {
  }

  default void processRequest (PluginData screen)
  {
  }
}
```
#### Linking
Use the Plugins Manager to connect the class name to a command name. The command will appear as a menu item on the Plugins menu.
##### Plugins Manager
![Plugins](pluginmenu.png?raw=true "plugin menu")
##### Plugins Menu
![Plugins](plugins.png?raw=true "plugin list")
#### Activation
Select the plugin from the plugins menu.
