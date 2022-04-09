package org.teamnine.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import org.teamnine.common.ParseBuilder;
import org.teamnine.common.ParseException;

public class ConnectionHandler implements Runnable {
	private ChatRoom chatRoom;
	private Socket clientSocket;
	private ParseBuilder pb;	
	private ConnectionHandler clientb;
	private String username;
	private String randCookie;
	private Scanner in;
	private PrintWriter out;

	public ConnectionHandler(ChatRoom chatRoom, Socket clientSocket) throws Exception {
		this.chatRoom = chatRoom;
		this.clientSocket = clientSocket;
		this.in = new Scanner(clientSocket.getInputStream());
		this.out = new PrintWriter(clientSocket.getOutputStream(), true);
		this.pb = new ParseBuilder(in);
		
	}
	
	// Changed to run - handle all exceptions, and send responses as necessary.
	public void run() {
		
		// Read client socket for connect request - put user in username
		
				
		while (true) {
			// Read from socket
			// Validate calls and parse msgtype
			// Based on msgtype call respective method
		}
	} 
	
	boolean initConnect() throws IOException, ParseException{
		String msgType;
		msgType = pb.pass("START").pass("MSGTYPE:").extract();
		if(msgType.equals("CONNECT")){
			username = pb.pass("USERNAME:").extract();
			randCookie = pb.pass("RAND_COOKIE:").extract();
			return true;
		}
		else
		{
			return false;
			
		}
	}
	
	public void connectedResponse(){
		out.write("START\nMSGTYPE: CONNECTED\nEND");
	}
	
	public void authFailResponse(){
		out.write("START\nMSGTYPE: AUTH_FAIL\nEND");
	}
	
	public String getUsername() { return username; }
	
	private void onChatRequest(String clientB) {
		if(chatRoom.isReachable(clientB)) {
			chatRoom.startChat(this.getUsername(), clientB);
			//out.write("START\nMSGTYPE: CHAT_STARTED\nSESSION_ID: " + sessionID + "\nEND");
		}
		else {
			out.write("START\nMSGTYPE: UNREACHABLE\nCLIENTB: " + clientB + "\nEND");
		}
		
	}
	
	
	private void onEndSession() {}

}
