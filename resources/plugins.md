### Plugins
This facility allows users to write their own plugin java modules which are able to examine and process 3270 input screens.  Modifiable fields may be altered, the cursor may be moved and any of the usual command or function keys may be pressed. A plugin can be executed automatically after each screen is displayed, or on request by the user.
#### Creation
Implement the Plugin interface or extend DefaultPlugin.
```java
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
  default void processAuto (PluginData data)
  {
  }
  default void processRequest (PluginData data)
  {
  }
}
```
#### Linking
Use the Plugin Manager to connect the class name to a command name. The command will appear as a menu item on the Plugins menu.
##### Plugin Manager
![Plugins](plugins.png?raw=true "plugin list")
##### Plugins Menu
![Plugins](pluginmenu.png?raw=true "plugins menu")
#### Activation
Select the plugin from the plugins menu. Plugins that are defined as Active are automatically activated.
