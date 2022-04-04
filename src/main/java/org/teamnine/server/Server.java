package org.teamnine.server;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
	private ServerSocket serverSocket;
	private ChatRoom chatRoom;
	private String[] subbedUsers = {"AHAD", "GRANT"};
	private List<Thread> threads;

	public Server(int port) {
		serverSocket = new ServerSocket(port);
		chatRoom = new ChatRoom();
		threads = new ArrayList<Thread>();
	}

	public void start() throws Exception {
		while (true) {
			Socket clientSocket = serverSocket.accept();
			ConnectionHandler ch = new ConnectionHandler(clientSocket, chatRoom);
			chatRoom.registerUser(ch);	
			threads.add(new Thread(ch));
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

		for (Thread thread : threads) 
			thread.join();
	}

	public static void main(String[] args) throws Exception {
		Server server = new Server(6666);
		try {
			server.start();
		} finally {
			server.stop();
		}
	}
}
//there should be a separate clas clienthandler. accepting incoming requests. once it receives requests then its going to connect