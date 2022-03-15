/*
 *  File Name:      ChatServerTest.java
 *  Author:         Ahad Shabbir 
 *  Last Modified:  03/13/2022 Ahad Shabbir 
 *  Description: Basic client app to help test the server.
 */

import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader; 
import java.util.Scanner;

public class ChatServerTest {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) throws Exception {
	clientSocket = new Socket(ip, port);
	out = new PrintWriter(clientSocket.getOutputStream(), true);
	in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public String sendMessage(String msg) throws Exception {
	out.println(msg);

	String resp = "";
	for (int i = 0; i < 3; i++) {
	    resp += in.readLine() + "\n";
	}
	return resp;
    }

    public void stopConnection() throws Exception {
	in.close();
	out.close();
	clientSocket.close();
    }

    public static void main(String args[]) throws Exception {
	ChatServerTest client = new ChatServerTest();
	client.startConnection("127.0.0.1", 6666);
	String resp = client.sendMessage("START\nMSGTYPE: CONNECT\n USERNAME: AHAD\nRAND_COOKIE: 1\nEND\n");
	System.out.println(resp);
    }

}
