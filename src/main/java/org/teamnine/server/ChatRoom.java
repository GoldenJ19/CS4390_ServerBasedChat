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

	public synchronized void registerUser(ConnectionHandler ch) {
		connectedUsers.put(ch.getUsername(), ch);	
	}
	
	public synchronized void startChat(String A, String B) {
		busyUsers.put(A, connectedUsers.get(A));
		busyUsers.put(B, connectedUsers.get(B));
	}
	public boolean isReachable(String clientB) {
		return (connectedUsers.containsKey(clientB) && !busyUsers.containsKey(clientB));
	}

	/*public synchronized void sendChat(String otherUser, String msg) {
		ConnectionHandler otherUserHandler = connectedUsers.get(otherUser);
		if (otherUserHandler == null)
			if (busyUsers.containsKey(otherUser))
				throw new Exception("User '"+otherUser+"' is busy");
			else 
				throw new Exception("User '"+otherUser+"' is not connected");

		otherUserHandler.sendChat(msg);
	}*/
}
