package org.teamnine.server;

import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import org.teamnine.common.ParseBuilder;
import org.teamnine.common.ParseException;

public class ConnectionHandler implements Runnable {
	private ChatRoom chatRoom;
	private Socket clientSocket;
	private ParseBuilder pb;	
	private PrintWriter out;
	private String username;
	private int randCookie;
	
	public ConnectionHandler(ChatRoom chatRoom, int portNum, int randCookie) throws IOException {
		this.chatRoom = chatRoom;
		this.clientSocket = clientSocket;
		this.randCookie = randCookie;
		this.out = new PrintWriter(clientSocket.getOutputStream(), true);

		Scanner in = new Scanner(clientSocket.getInputStream());
		this.pb = new ParseBuilder(in);
	}
	
	// initConnect parses connect call and returns the randCookie
	public int initConnect() throws ParseException, IOException {
		this.username = pb
			.pass("START")
			.pass("MSGTYPE:")
			.pass("CONNECT")
			.pass("USERNAME:")
			.extract();		

		String randCookieStr = pb.pass("RAND_COOKIE:").extract();
		return Integer.parseInt(randCookieStr);
	}
	
	public void connectedResponse() {
		out.print(
			"START\n" +
			"MSGTYPE: CONNECTED\n" +
			"END\n"
		);
		out.flush();
		System.out.println("sent response.");
	}
	// Changed to run - handle all exceptions, and send responses as necessary.
	public void run() {
		while (true) {
			try {
				String msgType = pb.pass("START").pass("MSGTYPE:").extract();
				System.out.println("Found msgType:"+msgType);
			} catch (ParseException e) {
				System.out.println("Recieved invalid request");
			} catch (Exception e) {
				System.out.println("Client "+username+" recieved unexpected error. Exiting...");
			} finally {
				chatRoom.unregisterUser(this);
				try {
					close();
				} catch (Exception e) {
					System.err.println("Couldn't close connection handler.");
					e.printStackTrace();
				}
			}
		}
	} 

	public void close() throws Exception {
		if (pb != null)
			pb.close();

		if (out != null)
			out.close();
		
		if (clientSocket != null)
			clientSocket.close();
		
		if (chatRoom != null)
			chatRoom.unregisterUser(this);
	}

	public String getUsername() { return username; }
}
