package org.teamnine.server;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import org.teamnine.common.ParseBuilder;

public class Server {
	private ServerSocket serverSocket;
	private Authenticator auth;
	private Thread authThread;

	public Server(int udpPort, int tcpPort) {
		serverSocket = new ServerSocket(tcpPort);
		// Start Authenticator
		// this.auth = new Authenticator(udpPort);
		// authThread = new Thread(new Authenticator(udpPort)
	}

	public void start(int port) throws Exception {
		serverSocket = new ServerSocket(port);
		while (true) {
			clientSocket = serverSocket.accept();
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new Scanner(clientSocket.getInputStream());
			pb = new ParseBuilder(in);

			String msgType;
			msgType = pb.pass("START").pass("MSGTYPE:").extract();
			switch(msgType) {
				case "CONNECT":
					connectHandler();
					break;
				default:
					throw new Exception("invalid msgType");
			}
		}
	}

	public void stop() throws Exception {
		if (serverSocket != null)
			serverSocket.close();
	}
	
	private void connectHandler() throws Exception {
		ParseBuilder pb = new ParseBuilder(in);
		String username = pb.pass("USERNAME:").extract();
		String randCookieStr = pb.pass("RAND_COOKIE:").extract();
		int rand_cookie = Integer.parseInt(randCookieStr);
		System.out.println("rand_cookie = " + rand_cookie);

		boolean isSubbed = false;
		for (String user : subbedUsers) {
			if (username.equals(user)) {
				isSubbed = true;
				break;
			}
		}
		
		if (isSubbed) {
			System.out.println("User " + username + " is valid.");
			// Create ConnectionHandler for thread
			connectedResponse();
			// Add ConnectionHandler user key pait to hashmap
		} else {
			System.out.println("User " + username + " is NOT valid.");
			stop();
		}
	}

	private void connectedResponse() {
		out.printf("START\nMSGTYPE: CONNECTED\nEND\n");
	}

	public static void main(String[] args) throws Exception {
		Server server = new Server();
		try {
			server.start(6666);
		} finally {
			server.stop();
		}
	}
	
	
}
//there should be a separate clas clienthandler. accepting incoming requests. once it receives requests then its going to connect