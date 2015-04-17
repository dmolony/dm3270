# dm3270
Yet another TN3270 Terminal Emulator. This one is written in Java 8, and uses JavaFX. Please ensure that you are using the latest java version as JavaFX ~~has fewer bugs~~ improves with every release. At present Java 8 update 40 is the minimum version requred.
#### Defaults
The program defaults to Release Mode, which means that only the Terminal function is available (see below). To enable the other functions you will need to change the Debug menu option and restart. This is the dialog box you will see when running in Release Mode.  
![Default Connection](resources/connect1.png?raw=true "default connection")
#### Site List
To edit the list of sites that you wish to connect to, click the edit button on the connection screen.  
![Sites](resources/sitelist.png?raw=true "site list")
#### Terminal Function
Connects to the specified server. This is the only function available in Release Mode.
![Terminal screen](resources/terminal.png?raw=true "dm3270")
#### Menus
There are two menus which are only available from the first screen. There you may choose the font and font size for the terminal screen, and whether to run in Debug or Release mode. The fonts listed are the only ones that dm3270 will attempt to load, and then only if you have them installed on your machine. Most of the fonts are freely available on the web.  
![Fonts](resources/fonts.png?raw=true "fonts")
![Debug](resources/debug.png?raw=true "debug")
#### Keys
Not all keys are available on all platforms, and some keys aren't available at all. To help with this, the following shortcuts are available. These will probably change after some more testing on Windows.
* Home - ctrl-h
* Erase EOL - ctrl-del
* Return - crtl-enter
* Insert - ctrl-i
* PA1/2/3 - ctrl-F1/F2/F3
