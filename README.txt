CS 4390
Team 9: JARG
Project: Server-Based Chat

Contributors:
Ahad Shabbir (Team Leader)
Jusitn Hardy
Rumsha Hyder-Chowdhury
Grant Jin

Project Description:
A server/client system that allows clients to enter chat sesssions with other clients. Clients are also able to see their chat history with other clients.
Client login is handled using UDP transport, while other connections are over TCP. All TCP messages are encrypted using AES ciphering. 
The project is organized using Maven. The client code is in src/main/java/org/teamnine/client, and the server code is in src/main/java/org/teamnine/server.

General Use:
To compile this project, run 'mvn compile' from the command line. It will put all of the bytecode for the classes in target/classes/org/teamnine/. To run the server. You will need maven and java 13+ to run the code. 

To run the client type:
mvn exec:java -Dexec.mainClass="org.teamnine.client.Client"

To run the server type:
mvn exec:java -Dexec.mainClass="org.teamnine.server.Server"

You can also use an IDE capable of handling maven (i.e. IntellJ or eclipse) to run the relevant files.

Begin the server by running Server.java. The server will wait on client messages to be handled, and is otherwise dormant.

To connect a client, run Client.java. Mulitple instances of Client can be run and handled by the server. The server database currently contains only 4 
valid client IDs and secret keys, which are as follows (case-sensitive):

CientID      Secret Key
ahad         testpass1
grant        testpass2
justin       testpass3
rumsha       testpass4

File Descriptions:
Client Package:
  Client: The client class, representing the client.
  ClientRunnable: The client thread class.
Common Package:
  Authenticator: Class containing security functions for the program (MD5, SHA256, AES encryption/decryption)
  FixedCipherOutputStream: Class that works around bugs in the CipherOutputStream.
  ParseBuilder: Class containing the parsehandling functions for client/server messages.
  ParseException: Exception unique to ParseBuilder, called when there is an error in parsing.
Server Package:
  ChatRoom: Controls chat session functions between multiple clients.
  ChatRoomException: Exception unique to ChatRoom.
  ConnectionHandler: Controls the TCP message handling on server side.
    NOTE: Will display error if not running at least Java 13, due to case statements using '->' instead of ':'.
  DatabseSetup: Sets up the database of client IDs/secret keys. Creates server.db if it doesn't already exist.
    NOTE: Will display error due to databse CREATE TABLE query using triple quotes. This is intentional, and
          should not cause compliation errors.
  Server: The server class, representing the server.
  UDPHandler: Controls the UDP message handling on the server side. 
