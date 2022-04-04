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

		for (Thread thread : threads)
			thread.interrupt();

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
