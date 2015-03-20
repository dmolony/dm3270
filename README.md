# dm3270
Yet another TN3270 Terminal Emulator. This one is written in Java 8, and uses JavaFX.
#### Warning
This is beta software. Not everything has been implemented yet, however most of the functions are working.
#### Defaults
The program defaults to the Release setting, which means it runs only in Terminal mode (see below). To enable the other modes you will need to change the Debug menu option and restart. This is the dialog box you will see when running as Release.
![Default Connection](Resources/connect.png?raw=true "default connection")
#### Modes
There are four ways in which this software can currently be used - Spy, Replay, Mainframe and Terminal. These are described below.                
![Initial screen](Resources/main1.png?raw=true "initial screen")
#### Spy Mode
Waits for a terminal emulator to connect to the client port specified, and then it completes the connection to the actual mainframe at the server and port specified. The mainframe session can then be used as normal, however all communication buffers are recorded and can be saved in a session file to replay later.
![Spy screen](Resources/spy.png?raw=true "spy screen")
#### Replay Mode
Uses the dm3270 terminal to replay the commands that were previously saved in a session file. Individual buffers can be selected and examined. The anticipated reply message (if any) can also displayed. Each command (both client and server) is sent to the terminal for processing.
![Replay screen](Resources/replay.png?raw=true "replay screen")
#### Mainframe Mode
Waits for a terminal emulator to connect to the client port specifed. The program is now serving as a mainframe, and the mainframe window can be used to send the terminal various 3270 commands. Any replies from the terminal are displayed. The session can also be saved for later replaying.
![Mainframe screen](Resources/mainframe.png?raw=true "mainframe screen")
#### Terminal Mode
Connects the dm3270 terminal to a real mainframe. This mode has not been thoroughly tested yet.
![Terminal screen](Resources/console.png?raw=true "dm3270")
