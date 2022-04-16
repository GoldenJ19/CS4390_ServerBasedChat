package org.teamnine.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import org.teamnine.common.*;

public class Server {
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private PrintWriter out;
	private Scanner in;
	private ParseBuilder pb;


	public Server(int udpPort, int tcpPort) throws IOException {
		serverSocket = new ServerSocket(udpPort, tcpPort);
		clientSocket = serverSocket.accept();
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new Scanner(clientSocket.getInputStream());
		pb = new ParseBuilder(in);
		// Start Authenticator
		// this.auth = new Authenticator(udpPort);
		// authThread = new Thread(new Authenticator(udpPort)
	}

	public void start(int port) throws Exception {
		serverSocket = new ServerSocket(port);
		while (true) {
			

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
	
	public void handleHello(String Client_ID) {
		//Search database for client ID
		//If clientID exists, give secret key K_A to auth
		int randCookie = (int) Math.floor(Math.random()*(9999-1000+1)+1000);
		String testKey = "test123";
		String XRES = Authenticator.A3(randCookie, testKey);
		clientChallenges.put(Client_ID, XRES);
		out.printf("START\nMSGTYPE: CHALLENGE\n" + XRES + "END\n");
		
	}

	public void handleResponse() {
		
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
	}

	private void connectedResponse() {
		out.printf("START\nMSGTYPE: CONNECTED\nEND\n");
	}

	public static void main(String[] args) throws Exception {
		Server server = new Server(3306, 6666);
		try {
			server.start(6666);
		} finally {
			server.stop();
		}
	}
	
	
}
