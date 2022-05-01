package org.teamnine.server;

import org.teamnine.common.ParseBuilder;
import org.teamnine.common.ParseException;
import org.teamnine.common.Authenticator;
import org.teamnine.common.CipherOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ConnectionHandler implements Runnable {
	private final ChatRoom chatRoom;
	private final ServerSocket serverSocket;
	private Socket connectSocket;
	private ParseBuilder pb;
	private PrintWriter out;
	private String username;
	private final int randCookie;
	private boolean running = true;
	private Connection dbConn;
	private String secretKey;

	public ConnectionHandler(ChatRoom chatRoom, int portNum, int randCookie, Connection dbConn, String secretKey) throws IOException {
		this.chatRoom = chatRoom;
		this.randCookie = randCookie;
		this.dbConn = dbConn;
		this.secretKey = secretKey;
		// Bind and connect client socket to port number:
		serverSocket = new ServerSocket();
		SocketAddress address = new InetSocketAddress("localhost", portNum);
		serverSocket.bind(address);

	}
	
	// initConnect parses connect call and returns the randCookie
	private int initConnect() throws ParseException, IOException {
		this.username = pb
			.pass("START")
			.pass("MSGTYPE:")
			.pass("CONNECT")
			.pass("USERNAME:")
			.extract();

		String randCookieStr = pb.pass("RAND_COOKIE:").extract();
		pb.pass("END");
		return Integer.parseInt(randCookieStr);
	}
	
	public void run() {
		// accept connection and setup writers and readers.
		try {
			connectSocket = serverSocket.accept();
			CipherInputStream cipherInput = new CipherInputStream(
					connectSocket.getInputStream(),
					Authenticator.getCipher(Cipher.DECRYPT_MODE, randCookie, secretKey)
			);
			CipherOutputStream cipherOutput = new CipherOutputStream(
					connectSocket.getOutputStream(),
					Authenticator.getCipher(Cipher.ENCRYPT_MODE, randCookie, secretKey)
			);
			this.out = new PrintWriter(cipherOutput, true);
			Scanner in = new Scanner(cipherInput);
			this.pb = new ParseBuilder(in);

			// Handle connect - verify the rand cookie matches.
			try {
				int clientRandCookie = initConnect();
				if (clientRandCookie == randCookie) {
					chatRoom.registerUser(this);
					connectedResponse();
					cipherOutput.flush();
				} else {
					authFailResponse("incorrect rand cookie");
				}
			} catch (ParseException e) {
				System.err.println("Bad message from client - expected connect.");
				System.err.println(e.getMessage());
				clientErrorResponse(e.getMessage());
			}

			// Handle other methods
			while (running) {
				String clientb = null;
				try {
					String msgType = pb.pass("START").pass("MSGTYPE:").extract();
					String sessionID = null;
					switch (msgType) {
						case "CHAT_REQUEST" -> {
							clientb = pb.pass("CLIENTB:").extract();
							pb.pass("END");
							sessionID = chatRoom.chatRequest(this, clientb);
							out.print(
									"START\n" +
									"MSGTYPE: CHAT_STARTED\n" +
									"SESSION_ID: " + sessionID + "\n" +
									"CLIENTB: " + clientb + "\n" +
									"END\n"
							);
							out.flush();
						}
						case "CHAT" -> {
							sessionID = pb.pass("SESSION_ID:").extract();
							String msg = pb.pass("MESSAGE:").extractLine();
							pb.pass("END");
							chatRoom.sendChat(this, sessionID, msg);
						}
						case "END_REQUEST" -> {
							sessionID = pb.pass("SESSION_ID:").extract();
							pb.pass("END");
							chatRoom.endSession(sessionID);
						}
						case "HISTORY_REQ" -> {
							clientb = pb.pass("CLIENTB:").extract();
							pb.pass("END");
							historyResponse(clientb);
						}
						default -> System.err.println("Invalid msgType '" + msgType + "'\n");
					}
				} catch (ParseException e) {
					System.err.println("Bad message from client.");
					System.err.println(e.getMessage());
					clientErrorResponse(e.getMessage());
				} catch (ChatRoomException e) {
					unreachable(clientb);
				}
			}
		} catch (SocketException e) {
			System.err.println("Connection error, exiting...");
		} catch(InterruptedIOException | NoSuchElementException e) {
			System.out.println("Interrupted, exiting...");
		} catch (Exception e) {
			System.err.println("FATAL: Encountered unexpected Exception, exiting");
		} finally {
			try {
				close();
			} catch (Exception e) {
				System.err.println("Couldn't close resources.");
			}
		}
	}

	private void historyResponse(String clientb) {
		String sql = "SELECT clientFrom, message FROM chat_log WHERE (clientFrom = ? AND clientTo = ?) OR (clientFrom = ? and clientTo = ?);";
		try(PreparedStatement stmt = dbConn.prepareStatement(sql)){
			stmt.setString(1, username);
			stmt.setString(2, clientb);
			stmt.setString(3, clientb);
			stmt.setString(4, username);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				out.print(
					"START\n"+
					"MSGTYPE: HISTORY_RESP\n"+
					"SENDER: "+rs.getString(1)+"\n"+
					"MESSAGE: "+rs.getString(2)+"\n"+
					"END\n"
				);
				out.flush();
			}
		} catch (SQLException e) {
			System.err.println("Fatal sql exception");
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	// Interrupt shutdown the pipes in the socket and stops the handler.
	public void interrupt() {
		try {
			if (connectSocket != null && !connectSocket.isClosed()) {
				connectSocket.shutdownInput();
				connectSocket.shutdownOutput();
			}
		} catch (IOException e) {
			System.err.println("Error when shutting down connectSocket streams.");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		running = false;
	}

	public void unreachable(String clientb) {
		out.println(
			"START\n" +
			"MSGTYPE: UNREACHABLE\n" +
			"CLIENTB: "+clientb+"\n" +
			"END"
		);
		out.flush();
	}

	public void sendChat(String sessionID, String msg) {
		out.print(
			"START\n" +
			"MSGTYPE: CHAT\n" +
			"SESSION_ID: "+sessionID+"\n"+
			"MESSAGE: "+msg+"\n" +
			"END\n"
		);
		out.flush();
	}

	public void chatStarted(String sessionID, String clientb) {
		out.print(
			"START\n"+
			"MSGTYPE: CHAT_STARTED\n"+
			"SESSION_ID: "+sessionID+"\n"+
			"CLIENTB: "+clientb+"\n"+
			"END\n"
		);
		out.flush();
	}

	public void endNotif(String sessionID) {
		out.print(
			"START\n"+
			"MSGTYPE: END_NOTIF\n"+
			"SESSION_ID: "+sessionID+"\n"+
			"END\n"
		);
		out.flush();
	}

	private void connectedResponse() {
		out.println(
			"START\n" +
			"MSGTYPE: CONNECTED\n" +
			"END"
		);
		out.flush();
	}

	private void authFailResponse(String desc) {
		out.print(
			"START\n" +
			"MSGTYPE: AUTH_FAIL\n" +
			"DESC: " +
			desc +
			"\n" +
			"END\n"
		);
		out.flush();
	}

	private void clientErrorResponse(String desc) {
		out.print(
			"START\n" +
			"MSGTYPE: CLIENT_ERROR\n" +
			"DESC: " +
			desc +
			"\n" +
			"END\n"
		);
		out.flush();
	}
	private void close() throws Exception {
		if (pb != null)
			pb.close();

		if (out != null)
			out.close();
		
		if (serverSocket != null)
			serverSocket.close();

		if (connectSocket != null)
			connectSocket.close();

		if (chatRoom != null)
			chatRoom.unregisterUser(this);
	}
	public String getUsername() { return username; }
}