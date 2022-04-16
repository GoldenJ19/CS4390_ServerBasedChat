package org.teamnine.server.ChatRoom;

import java.util.concurrent.ConcurrentHashMap;
public class ChatRoom {

	private ConcurrentHashMap<String, ConnectionHandler> connectedUsers;
	private ConcurrentHashMap<String, ConnectionHandler> busyUsers;

	public ChatRoom() {
		connectedUsers = new ConcurrentHashMap<String, ConnectionHandler>();
		busyUsers = new ConcurrentHashMap<String, ConnectionHandler>();
	}

	public synchronized void registerUser(ConnectionHandler ch) {
		connectedUsers.put(ch.getUsername(), ch);	
		System.out.println("User "+ch.getUsername()+" is registered.");
	}

	public synchronized void unregisterUser(ConnectionHandler ch) {
		connectedUsers.remove(ch.getUsername());
		System.out.println("User "+ch.getUsername()+" is unregistered.");
	}

	public void closeRoom() {
		// TODO: Disconnect all users on chatroom close.
	}
	/*public synchronized void sendChat(String clientB, String msg) {
		ConnectionHandler otherUserHandler = connectedUsers.get(otherUser);
		if (otherUserHandler == null)
			if (busyUsers.containsKey(otherUser))
				throw new Exception("User '"+otherUser+"' is busy");
			else 
				throw new Exception("User '"+otherUser+"' is not connected");

		otherUserHandler.sendChat(msg);
	}*/
}
