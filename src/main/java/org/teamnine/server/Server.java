package org.teamnine.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.teamnine.common.ParseException;

public class Server {
	private ServerSocket serverSocket;
	private ChatRoom chatRoom;
	private String[] subbedUsers = {"AHAD", "GRANT"};
	private List<Thread> threads;

	public Server(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		chatRoom = new ChatRoom();
		threads = new ArrayList<Thread>();
	}

	public void start() throws Exception {
		while (true) {
			//Wait on next client socket
			Socket clientSocket = serverSocket.accept();
			
			//Create new Connection Handler for new client
			ConnectionHandler ch = new ConnectionHandler(chatRoom, clientSocket);
			
			//Attempt connection
			boolean connected = false;
			try {
				connected = ch.initConnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Write CONNECTED to client if connection is successful, AUTH_FAIL otherwise		
			if(connected){
				ch.connectedResponse();
				chatRoom.registerUser(ch);	
				threads.add(new Thread(ch));
			}
			
			else{
				ch.authFailResponse();		
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