package org.teamnine.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Server {
	private DatagramSocket UDPsocket;
	private DatagramPacket UDPmsg;
	private byte[] received;
	private Connection dbConn;
	private ChatRoom chatRoom;
	private Map<ConnectionHandler, Thread> connections = new HashMap<>();
	private int tcpPort;
	public Server(int udpPort, int tcpPort) 
		throws IOException, SQLException, ClassNotFoundException {

		dbConn = DatabaseSetup.setupDatabase("server.db");
		UDPsocket = new DatagramSocket(udpPort);
		chatRoom = new ChatRoom(dbConn);
		received = new byte[]{};
		this.tcpPort = tcpPort;
	}

	public void start() throws Exception {
		while (true) {
			UDPHandler authHandler = new UDPHandler(dbConn);
			received = new byte[10000];
			UDPmsg = new DatagramPacket(received, received.length);

			try {
				UDPsocket.receive(UDPmsg);
				received = UDPmsg.getData();
				String[] ptrString = new String[1];
				int randCookie = authHandler.handleHello(UDPmsg.getData(), ptrString);
				String username = ptrString[0];

				//if user is subbed, send CHALLENGE with randcookie
				if (randCookie > -1) {
					received = new byte[10000];
					received = authHandler.createChallengeMSG(randCookie);
					UDPmsg = new DatagramPacket(received, received.length, UDPmsg.getAddress(), UDPmsg.getPort());
					UDPsocket.send(UDPmsg);

					received = new byte[10000];
					UDPmsg = new DatagramPacket(received, received.length);
					UDPsocket.receive(UDPmsg);
					boolean successfulLogin = authHandler.processResponse(received);

					// Bound check tcp port
					while (!successfulLogin) {
						received = new byte[10000];
						received = authHandler.createAuthMsg(false, randCookie, username, 0);
						UDPmsg = new DatagramPacket(received, received.length, UDPmsg.getAddress(), UDPmsg.getPort());
						UDPsocket.send(UDPmsg);
						successfulLogin = authHandler.processResponse(received);
					}
					// connection handler
					int chTCPPort = tcpPort++;
					ConnectionHandler ch = new ConnectionHandler(chatRoom, chTCPPort, randCookie, dbConn,
							authHandler.getPasswordKeyOf(username));
					Thread thread = new Thread(ch);
					thread.start();
					connections.put(ch, thread);
					received = new byte[10000];
					received = authHandler.createAuthMsg(true, randCookie, username, chTCPPort);
					UDPmsg = new DatagramPacket(received, received.length, UDPmsg.getAddress(), UDPmsg.getPort());
					UDPsocket.send(UDPmsg);
				}
			} catch (IOException badmsg) {
				// TODO Auto-generated catch block
				badmsg.printStackTrace();
			}
		}
	}
	public void close() throws Exception {
		if (dbConn != null)
			dbConn.close();

		if (chatRoom != null)
			chatRoom.close();

		if (UDPsocket != null)
			UDPsocket.close();

		for (Map.Entry<ConnectionHandler, Thread> e : connections.entrySet()) {
			e.getKey().interrupt();
			e.getValue().join();
		}
	}


	public static void main(String[] args) throws Exception {
		Server server = null;
		try {
			server = new Server(1234, 5678);
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			server.close();
		}
	}

}
