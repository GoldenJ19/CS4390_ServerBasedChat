package org.teamnine.server;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import org.teamnine.common.ParseBuilder;

public class ConnectionHandler implements Runnable {
	private ChatRoom chatRoom;
	private Socket clientSocket;
	private ParseBuilder pb;	
	private ConnectionHandler clientb;
	private String username;

	public ConnectionHandler(ChatRoom chatRoom, Socket clientSocket) {
		this.chatRoom = chatRoom;
		this.clientSocket = clientSocket;
		// Read client socket for connect request - put user in username
	}
	
	// Changed to run - handle all exceptions, and send responses as necessary.
	public void run() {
		while (true) {
			// Read from socket
			// Validate calls and parse msgtype
			// Based on msgtype call respective method
		}
	} 

	public String getUsername() { return username; }
	private void onChatRequest() {}
	private void onEndSession() {}

}
