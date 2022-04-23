package org.teamnine.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;

public class Server {
	private ServerSocket serverSocket;
	private Connection dbConn;
	private ChatRoom chatRoom;
	private Thread chatRoomThread;

	public Server(int udpPort, int tcpPort) 
		throws IOException, SQLException, ClassNotFoundException {

		dbConn = DatabaseSetup.setupDatabase("server.db");	
		serverSocket = new ServerSocket(tcpPort);
		chatRoom = new ChatRoom();
	}

	public void start() {
		while (true) {
			Socket clientSocket;
			ConnectionHandler clientHandler;

			try {
				clientSocket = serverSocket.accept();
				//clientHandler = new ConnectionHandler(chatRoom, clientSocket);
			} catch (IOException e) {
				System.out.println("Unexpected IOException when accepting TCP connection");
				e.printStackTrace();
				return;
			}

			try {
				//int randCookie = clientHandler.initConnect();
				// TODO: Verify randcookie here
				//chatRoom.registerUser(clientHandler);
				System.out.println("Registered user, sending connected response");
				//clientHandler.connectedResponse();
				//new Thread(clientHandler);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Could not handle client connection.");
				return;
			}
		}
	}

	public void close() throws Exception {
		if (dbConn != null)
			dbConn.close();

		if (chatRoom != null)
			chatRoom.close();

		if (serverSocket != null)
			serverSocket.close();
	}


	public static void main(String[] args) throws Exception {
		Server server = null;
		try {
			server = new Server(1234, 5678);
			server.start();
		} finally {
			server.close();
		}
	}
}
