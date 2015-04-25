### Functions
Four functions are available in Debug Mode - Spy, Replay, Terminal and Test.  
![Debug Connection](connect2.png?raw=true "debug connection")
#### Spy Function
Waits for a terminal emulator to connect to the client, and then it completes the connection to the server. The mainframe session can be used as normal, however all communication buffers are collected and can be saved in a session file to replay later. For security purposes the file can be saved with all user input redacted, in order to prevent any passwords being saved. The session file should be named spy99.txt, where 99 is any two-digit number.
![Spy screen](spy.png?raw=true "spy screen")
#### Replay Function
Uses the dm3270 terminal to replay the commands that were previously recorded in a session file. Individual buffers can be selected and examined. The anticipated reply message (if any) is also displayed. Each command (both client and server) is sent to the terminal for processing.
![Replay screen](replay2.png?raw=true "replay screen")
#### Test Function
Waits for a terminal emulator to connect to the client port specifed. The program is now acting as a server, and it can be used to send the terminal various 3270 commands. Any replies from the terminal are displayed. The session can also be saved for later replaying.
![Mainframe screen](mainframe.png?raw=true "mainframe screen")
#### Terminal Function
Connects to the specified server. This is the only function available in Release Mode.
![Terminal screen](terminal.png?raw=true "dm3270")
