# dm3270
Yet another TN3270 Terminal Emulator. This one is written in Java 8, and uses JavaFX. Please ensure that you are using the latest java version as JavaFX ~~has fewer bugs~~ improves with every release. At present Java 8 update 40 is the minimum version requred.
#### Defaults
The program defaults to Release Mode, which means that only the Terminal function is available (see below). To enable the other [functions](resources/functions.md) you will need to change to [Debug Mode](resources/menus.md) and restart.
This is the dialog box you will see when running in Release Mode.  
![Default Connection](resources/connect1.png?raw=true "default connection")
#### Site List
To edit the list of sites that you wish to connect to, click the edit button on the connection screen.  
![Sites](resources/sitelist.png?raw=true "site list")
#### Terminal Function
Connects to the specified server. This is the only function available in Release Mode.
![Terminal screen](resources/terminal.png?raw=true "dm3270")


#### Other features
* [Key commands](resources/commands.md)
* [Menus](resources/menus.md)
* [Debug functions](resources/functions.md)
* [Screen history](resources/history.md)
* Scripting
