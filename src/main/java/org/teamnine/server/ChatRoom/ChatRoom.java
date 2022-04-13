package org.teamnine.server;

import java.util.Map;
import java.util.HashMap;

public class ChatRoom {

	private Map<String, ConnectionHandler> connectedUsers;
	private Map<String, ConnectionHandler> busyUsers;

	public ChatRoom() {
		connectedUsers = new HashMap<String, ConnectionHandler>();
		busyUsers = new HashMap<String, ConnectionHandler>();
	}

	/*public synchronized void registerUser(ConnectionHandler ch) {
		connectedUsers.add(ch.getUsername(), ch);	
	}

	public synchronized void sendChat(String otherUser, String msg) {
		ConnectionHandler otherUserHandler = connectedUsers.get(otherUser);
		if (otherUserHandler == null)
			if (busyUsers.containsKey(otherUser))
				throw new Exception("User '"+otherUser+"' is busy");
			else 
				throw new Exception("User '"+otherUser+"' is not connected");

		otherUserHandler.sendChat(msg);
	}*/
}
