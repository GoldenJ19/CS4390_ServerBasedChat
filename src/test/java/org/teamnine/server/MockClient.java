package org.teamnine.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class MockClient implements Closeable {
	private final PrintStream out;
	private final Scanner in;
	private final String username;
	private final int randCookie;
	private final Socket clientSocket;

	public MockClient(String hostname, int port, String username, int randCookie) throws IOException {
		this.username = username;
		this.randCookie = randCookie;

		this.clientSocket = new Socket(hostname, port);
		this.out = new PrintStream(clientSocket.getOutputStream(), false);
		this.in = new Scanner(clientSocket.getInputStream());
	}
	public void connect() {
		out.print(
			"START\n" +
			"MSGTYPE: CONNECT\n" +
			"USERNAME: "+username+"\n"+
			"RAND_COOKIE: "+randCookie+"\n"+
			"END\n"
		);
		out.flush();
	}

	public void chatRequest(String clientb) {
		out.print(
			"START\n"+
			"MSGTYPE: CHAT_REQUEST\n"+
			"CLIENTB: "+clientb+"\n"+
			"END\n"
		);
		out.flush();
	}

	public String recordResponse(int n) {
		StringBuilder resp = new StringBuilder();
		for (int i = 0; i < n; i++)
			resp.append(in.nextLine()).append("\n");

		return resp.toString();
	}

	public void sendChat(String sessionID, String msg) {
		out.print(
			"START\n"+
			"MSGTYPE: CHAT\n" +
			"SESSION_ID: "+sessionID+"\n"+
			"MESSAGE: "+msg+"\n"+
			"END\n"
		);
		out.flush();
	}

	public void close() throws IOException {
		clientSocket.close();
		out.close();
		in.close();
	}

	public void endRequest(String session_id) {
		out.print(
			"START\n"+
			"MSGTYPE: END_REQUEST\n" +
			"SESSION_ID: "+session_id+"\n"+
			"END\n"
		);
		out.flush();
	}

	public void historyRequest(String clientb) {
		out.print(
			"START\n"+
			"MSGTYPE: HISTORY_REQ\n"+
			"CLIENTB: "+clientb+"\n"+
			"END\n"
		);
		out.flush();
	}
}