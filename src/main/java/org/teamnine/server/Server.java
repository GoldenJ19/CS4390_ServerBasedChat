package org.teamnine.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;

public class Server {
	private DatagramSocket UDPsocket;
	private DatagramPacket UDPmsg;
	private byte[] received;
	private ServerSocket serverSocket;
	private Connection dbConn;
	private ChatRoom chatRoom;
	private Thread chatRoomThread;

	public Server(int udpPort, int tcpPort) 
		throws IOException, SQLException, ClassNotFoundException {

		dbConn = DatabaseSetup.setupDatabase("server.db");
		UDPsocket = new DatagramSocket(udpPort);
		serverSocket = new ServerSocket(tcpPort);
		chatRoom = new ChatRoom(dbConn);
	}

	public void start() {
		while (true) {
			UDPHandler authHandler = new UDPHandler();
			UDPmsg = new DatagramPacket(received, received.length);
			
			try {
				UDPsocket.receive(UDPmsg);
				int randCookie = authHandler.securityTest(received);
				
				//if user is subbed, send CHALLENGE with randcookie
				if(randCookie > -1) {
					received = new byte[10000];
					received = authHandler.createChallengeMSG(randCookie);
					UDPmsg = new DatagramPacket(received, received.length);
					UDPsocket.send(UDPmsg);
					
					received = new byte[10000];
					UDPmsg = new DatagramPacket(received, received.length);
					UDPsocket.receive(UDPmsg);
					boolean successfulLogin = UDPHandler.processResponse(received);
					while(!successfulLogin) {
						received = new byte[10000];
						received = authHandler.createAuthMsg(successfulLogin, randCookie);
						UDPmsg = new DatagramPacket(received, received.length);
						UDPsocket.send(UDPmsg);	
					}
					received = new byte[10000];
					//Note: Should pass TCP port as well
					received = authHandler.createAuthMsg(successfulLogin, randCookie);
					UDPmsg = new DatagramPacket(received, received.length);
					UDPsocket.send(UDPmsg);
				}
			} catch (IOException badmsg) {
				// TODO Auto-generated catch block
				badmsg.printStackTrace();
			}
			
			
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
		
		if (UDPsocket != null)
			UDPsocket.close();
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
