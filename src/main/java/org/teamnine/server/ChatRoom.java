package org.teamnine.server;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
public class ChatRoom implements Closeable {

	private final ConcurrentHashMap<String, ConnectionHandler> connectedUsers = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, ConnectionHandler> busyUsers = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
	private Connection dbConn;
	public ChatRoom(Connection dbConn) {
		this.dbConn = dbConn;
	}

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

		public synchronized void sendChat(ConnectionHandler from, String sessionID, String msg, Connection dbConn) {
			ConnectionHandler to;
			if (from == clienta) {
				clientb.sendChat(sessionID, msg);
				to = clientb;
			} else {
				clienta.sendChat(sessionID, msg);
				to = clienta;
			}

			// Insert chat into database
			String sql = "INSERT INTO chat_log VALUES(?, ?, ?, ?);";
			try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
				stmt.setString(1, from.getUsername());
				stmt.setString(2, to.getUsername());
				stmt.setString(3, sessionID);
				stmt.setString(4, msg);
				stmt.execute();
			} catch (SQLException e) {
				System.err.println("FATAL: sql error");
				throw new RuntimeException(e);
			}
		}

		public boolean isInSession( String username ) {
			return clienta.getUsername().equals(username) || clientb.getUsername().equals(username);
		}

		public String getClientA() {
			return clienta.getUsername();
		}

		public String getClientB() {
			return clientb.getUsername();
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
		for( Session session : sessions.values() ) {
			if( session.isInSession(ch.getUsername()) ) {
				try {
					endSession(session.getSessionID());
				}
				catch( ChatRoomException e ) {
					throw new RuntimeException("Something went \"seriously wrong\" when trying to end session.");
				}
				break;
			}
		}
	}

	// chatRequest signals clientb handler to send a chat started. Returns a sessionID.
	public synchronized String chatRequest(ConnectionHandler clientaHandler, String clientb) throws ChatRoomException {
		if (!connectedUsers.containsKey(clientb))
			throw new ChatRoomException("Client B " + clientb + "not connected.");
		if (busyUsers.containsKey(clientb))
			throw new ChatRoomException("Client B" + clientb + "is busy.");

		var clientbHandler = connectedUsers.get(clientb);

		var session = new Session(clientaHandler, clientbHandler);
		sessions.put(session.getSessionID(), session);

		clientbHandler.chatStarted(session.getSessionID(), clientaHandler.getUsername());

		busyUsers.put(clientaHandler.getUsername(), clientaHandler);
		busyUsers.put(clientbHandler.getUsername(), clientbHandler);

		return session.getSessionID();
	}

	// sendChat sends a chat 'from' a connectionHandler to the client b in the session specified in sessionID.
	public synchronized void sendChat(ConnectionHandler from, String sessionID, String msg) throws ChatRoomException {
		var session = sessions.get(sessionID);
		if (session == null)
			throw new ChatRoomException("Session "+sessionID+" doesn't exist.");

		session.sendChat(from, sessionID, msg, dbConn);
	}

	public synchronized void endSession(String sessionID) throws ChatRoomException {
		var session = sessions.get(sessionID);
		if (session == null)
			throw new ChatRoomException("Session "+sessionID+" doesn't exist.");
		busyUsers.remove(session.getClientA());
		busyUsers.remove(session.getClientB());
		session.end();
		sessions.remove(sessionID);
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