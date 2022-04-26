package org.teamnine.server;

import org.teamnine.common.ParseBuilder;
import org.teamnine.common.ParseException;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.*;
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
	
	public ConnectionHandler(ChatRoom chatRoom, int portNum, int randCookie) throws IOException {
		this.chatRoom = chatRoom;
		this.randCookie = randCookie;

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
			this.out = new PrintWriter(connectSocket.getOutputStream());
			Scanner in = new Scanner(connectSocket.getInputStream());
			this.pb = new ParseBuilder(in);

			// Handle connect
			try {
				int clientRandCookie = initConnect();
				if (clientRandCookie == randCookie) {
					chatRoom.registerUser(this);
					connectedResponse();
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
				try {
					String msgType = pb.pass("START").pass("MSGTYPE:").extract();
					String sessionID;
					switch (msgType) {
						case "CHAT_REQUEST": {
							String clientb = pb.pass("CLIENTB:").extract();
							pb.pass("END");
							sessionID = chatRoom.chatRequest(this, clientb);
							out.print(
								"START\n" +
								"MSGTYPE: CHAT_STARTED\n" +
								"SESSION_ID: " + sessionID + "\n" +
								"CLIENTB:" + clientb + "\n" +
								"END\n"
							);
						}
						case "CHAT": {
							sessionID = pb.pass("SESSION_ID:").extract();
							String msg = pb.pass("MESSAGE:").extract();
							pb.pass("END");
							chatRoom.sendChat(this, sessionID, msg);
						}
						default: System.err.println("Invalid msgType '" + msgType + "'\n");
					}
				} catch (ParseException e) {
					System.err.println("Bad message from client.");
					System.err.println(e.getMessage());
					clientErrorResponse(e.getMessage());
				} catch (ChatRoomException e) {
					System.err.println("Couldn't communicate with other client");
					clientErrorResponse(e.getMessage());
				}
			}
		} catch (SocketException e) {
			System.err.println("Connection error, exiting...");
		} catch(InterruptedIOException | NoSuchElementException e) {
			System.err.println("Interrupted, exiting...");
		} catch (IOException e) {
			// Usually happens on connection closed, so just exit.
			System.err.println("FATAL: Encountered unexpected IOException, exiting");
		} finally {
			try {
				close();
			} catch (Exception e) {
				System.err.println("Couldn't close resources.");
			}
		}
	}

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
			"END\n"
		);
		out.flush();
	}

	private void connectedResponse() {
		out.print(
			"START\n" +
			"MSGTYPE: CONNECTED\n" +
			"END\n"
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