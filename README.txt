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

Client will prompt the user to type CONNECT or EXIT. If CONNECT is chosen, the user is prompted for a clientID and password.
The client sends a HELLO message to the server, containing the clientID. The server will check if the clientID is containted
within the database. If the given clientID matches one in the database, the server returns a CHALLENGE message containing a 
random integer (rand_cookie). Additionally, the server retrieves the client's secrety key, and hashes it with the rand_cookie
using MD5. If the client recieves the CHALLENGE message, the password inputed by the user is hashed using the rand_cookie given
by the server. The client sends a RESPONSE message to the server, containing the hashed rand_cookie/password. If the hashed string
matches the hash of the registered secret key and rand_cookie, then the client has sent the correct password and the server sends
an AUTH_SUCCESS message. This message contains a new rand_cookie, and port number. This message is encrypted using AES.

Notes:
Must be running Java 17
