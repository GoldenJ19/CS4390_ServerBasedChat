package org.teamnine.server;

import java.io.Closeable;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
public class ChatRoom implements Closeable {

	private final ConcurrentHashMap<String, ConnectionHandler> connectedUsers = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, ConnectionHandler> busyUsers = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

	private static class Session {
		private final ConnectionHandler clienta;
		private final ConnectionHandler clientb;
		private final String sessionID;

		public Session(ConnectionHandler clienta, ConnectionHandler clientb) {
			this.clienta = clienta;
			this.clientb = clientb;

			byte[] sessionIDBytes = new byte[8];
			new Random().nextBytes(sessionIDBytes);
			this.sessionID = Base64.getEncoder().encodeToString(sessionIDBytes);
		}

		public synchronized void sendChat(ConnectionHandler from, String sessionID, String msg) {
			if (from == clienta) {
				clientb.sendChat(sessionID, msg);
			} else {
				clienta.sendChat(sessionID, msg);
			}
		}

		public synchronized void end() {
			clienta.endNotif(sessionID);
			clientb.endNotif(sessionID);
		}

		public String getSessionID() { return sessionID; }
	}

	public synchronized void registerUser(ConnectionHandler ch) {
		connectedUsers.put(ch.getUsername(), ch);
		System.out.println("User " + ch.getUsername() + " is registered.");
	}

	public synchronized void unregisterUser(ConnectionHandler ch) {
		connectedUsers.remove(ch.getUsername());
		System.out.println("User " + ch.getUsername() + " is unregistered.");
	}

	// chatRequest signals clientb handler to send a chat started. Returns a sessionID.
	public synchronized String chatRequest(ConnectionHandler clientaHandler, String clientb) throws ChatRoomException {
		if (!connectedUsers.containsKey(clientb))
			throw new ChatRoomException("Client B " + clientb + "not connected.");
		if (busyUsers.containsKey("Client B" + clientb + "is busy"))
			throw new ChatRoomException("Client B" + clientb + "is busy.");

		var clientbHandler = connectedUsers.get(clientb);

		var session = new Session(clientaHandler, clientbHandler);
		sessions.put(session.getSessionID(), new Session(clientaHandler, clientbHandler));

		clientbHandler.chatStarted(session.getSessionID(), clientb);

		busyUsers.put(clientaHandler.getUsername(), clientaHandler);
		busyUsers.put(clientbHandler.getUsername(), clientbHandler);

		return session.getSessionID();
	}

	// sendChat sends a chat 'from' a connectionHandler to the client b in the session specified in sessionID.
	public synchronized void sendChat(ConnectionHandler from, String sessionID, String msg) throws ChatRoomException {
		var session = sessions.get(sessionID);
		if (session == null)
			throw new ChatRoomException("Session "+sessionID+" doesn't exist.");

		session.sendChat(from, sessionID, msg);
	}

	public void close() {
		// Close all chat sessions
		for (var session : sessions.values())
			session.end();

		// Interrupt all connect handlers
		for (var user : connectedUsers.values())
			user.interrupt();
	}
}