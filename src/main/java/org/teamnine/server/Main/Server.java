package org.teamnine.server;

import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.SQLException;
import org.teamnine.common.ParseBuilder;

public class Server {
	private ServerSocket serverSocket;
	private Connection dbConn;
	//private Authenticator auth;
	//private Thread authThread;

	public Server(int udpPort, int tcpPort) 
		throws IOException, SQLException, ClassNotFoundException {

		dbConn = DatabaseSetup.setupDatabase("server.db");	
		serverSocket = new ServerSocket(tcpPort);
	}

	public void close() 
		throws IOException, SQLException, ClassNotFoundException {

		if (dbConn != null)
			dbConn.close();

		if (serverSocket != null)
			serverSocket.close();
	}

	/*public void start(int port) throws Exception {
		serverSocket = new ServerSocket(port);
		while (true) {
			clientSocket = serverSocket.accept();
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
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
		
		// auth.close();
		// auth.join();
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
	}*/

	public static void main(String[] args) throws Exception {
		Server server = new Server(1234, 5678);
		server.close();
	}
}
