CS 4390
Team 9: JARG
Project: Server-Based Chat

Contributors:
Ahad Shabbir (Team Leader)
Jusitn Hardy
Rumsha Hyder-Chowdhury
Grant Jin

General Use:
Begin the server by running Server.java. The server will wait on client messages to be handled, and is otherwise dormant.

To connect a client, run Client.java. Mulitple instances of Client can be run and handled by the server. The server database
currently contains only 4 valid client IDs and secret keys, which are as follows (case-sensitive):

CientID      Secret Key
ahad         testpass1
grant        testpass2
justin       testpass3
rumsha       testpass4

File Descriptions:
Client Package:
  Client: The client class, representing the client.
Common Package:
  Authenticator: Class containing security functions for the program (MD5, SHA256, AES encryption/decryption)
  ParseBuilder: Class containing the parsehandling functions for client/server messages.
  ParseException: Exception unique to ParseBuilder, called when there is an error in parsing.
Server Package:
  ChatRoom: Controls chat session functions between two clients.
  ChatRoomException: Exception unique to ChatRoom.
  ConnectionHandler: Controls the TCP message handling on server side.
  DatabseSetup: Sets up the database of client IDs/secret keys. Creates server.db if it doesn't already exist.
  Server: The server class, representing the server.
  UDPHandler: Controls the UDP message handling on the server side. 


Notes:
Must be running Java 17