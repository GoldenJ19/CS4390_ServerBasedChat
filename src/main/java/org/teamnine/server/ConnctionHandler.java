package org.teamnine.server;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import org.teamnine.common.ParseBuilder;

public class ConnectionHandler implements Callable<Void> {
	private Server parent;
	private Socket clientSocket;
	private ParseBuilder pb;	
	private ConnectionHandler clientb;

	public ConnectionHandler(Server parent, Socket clientSocket) {
		this.parent = parent;
		this.clientSocket = clientSocket;
	}
	
	public Void call() {
		while (true) {
			// Read from socket
			// Validate calls and parse msgtype
			// Based on msgtype call respective method
		}
	} 

	private void onChatRequest() {}
	private void onEndSession() {}
}
