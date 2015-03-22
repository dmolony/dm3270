# dm3270
Yet another TN3270 Terminal Emulator. This one is written in Java 8, and uses JavaFX. Please ensure you are using the latest version as JavaFX improves with every reales. At present Java 8 update 40 is the minimum version requred.
#### Warning
This is beta software. You know what that means.
#### Defaults
The program defaults to the Release setting, which means it runs only in Terminal mode (see below). To enable the other modes you will need to change the Debug menu option and restart. This is the dialog box you will see when running as Release.  
![Default Connection](Resources/connect.png?raw=true "default connection")
#### Modes
The four modes in which this software can currently be used are Spy, Replay, Mainframe and Terminal. These are described below. Only terminal mode is available when in the Release setting, but all four are enabled if you switch to the Debug setting. This is the dialog box you will see when running as Debug.  
![Initial screen](Resources/main1.png?raw=true "initial screen")
#### Spy Mode
Waits for any terminal emulator to connect to the client port specified, and then it completes the connection to the actual mainframe at the server and port specified. The mainframe session can be used as normal, however all communication buffers are recorded and can be saved in a session file to replay later.
![Spy screen](Resources/spy.png?raw=true "spy screen")
#### Replay Mode
Uses the dm3270 terminal to replay the commands that were previously saved in a session file. Individual buffers can be selected and examined. The anticipated reply message (if any) can also displayed. Each command (both client and server) is sent to the terminal for processing.
![Replay screen](Resources/replay.png?raw=true "replay screen")
#### Mainframe Mode
Waits for any terminal emulator to connect to the client port specifed. The program is now serving as a dummy mainframe, and the mainframe window can be used to send the terminal various 3270 commands. Any replies from the terminal are displayed. The session can also be saved for later replaying.
![Mainframe screen](Resources/mainframe.png?raw=true "mainframe screen")
#### Terminal Mode
Connects the dm3270 terminal to the specified mainframe. This is the only mode available at the Release setting.
![Terminal screen](Resources/console.png?raw=true "dm3270")
#### Menus
There are two menus which are only available from the first screen. There you may choose the font and font size for the terminal screen, and whether to run in Debug or Release mode. The fonts listed are the only ones that dm3270 will attempt to load, and then only if you have them installed on your machine. Most of the fonts are freely available on the web.
![Fonts](resources/fonts.png?raw=true "fonts")
![Debug](resources/debug.png?raw=true "debug")
