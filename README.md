# dm3270
Yet another TN3270 Terminal Emulator. This one is written in Java 8, and uses JavaFX. Please ensure that you are using the latest java version as JavaFX ~~has fewer bugs~~ improves with every release. At present Java 8 update 40 is the minimum version requred.
#### Defaults
The program defaults to Release Mode, which means that only the Terminal function is available (see below). To enable the other functions you will need to change the Debug menu option and restart. This is the dialog box you will see when running in Release Mode.  
![Default Connection](resources/connect1.png?raw=true "default connection")
#### Site List
To add to or edit the list of sites that you wish to connect to, click the edit button on the connection screen.  
![Sites](resources/sitelist.png?raw=true "site list")
#### Functions
The four functions available in Debug Mode are Spy, Replay, Terminal and Test. These are described below. Only the terminal function is available when in Release Mode, but all four are enabled if you switch to Debug Mode. This is the dialog box you will see when running in Debug Mode.  
![Debug Connection](resources/connect2.png?raw=true "debug connection")
#### Spy Function
Waits for a terminal emulator to connect to the client, and then it completes the connection to the server. The mainframe session can be used as normal, however all communication buffers are collected and can be saved in a session file to replay later. The session file should be named spy99.txt, where 99 is any two-digit number.
![Spy screen](resources/spy2.png?raw=true "spy screen")
#### Replay Function
Uses the dm3270 terminal to replay the commands that were previously recorded in a session file. Individual buffers can be selected and examined. The anticipated reply message (if any) is also displayed. Each command (both client and server) is sent to the terminal for processing.
![Replay screen](resources/replay2.png?raw=true "replay screen")
#### Test Function
Waits for a terminal emulator to connect to the client port specifed. The program is now acting as a server, and it can be used to send the terminal various 3270 commands. Any replies from the terminal are displayed. The session can also be saved for later replaying.
![Mainframe screen](resources/server.png?raw=true "mainframe screen")
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
