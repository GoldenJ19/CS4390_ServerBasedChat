/*
 *  File Name:      Client.java
 *  Author:         Justin Hardy
 *  Last Modified:  03/13/2022 Grant Jin
 */

import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader; 
import java.util.Scanner;

public class Client {
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
        String resp = in.readLine();
        return resp;
    }

    public void stopConnection() throws Exception {
        in.close();
        out.close();
        clientSocket.close();
    }

	public static void main(String args[]) throws Exception {
		chatClient client = new chatClient();
		client.startConnection("127.0.0.1", 6666);
		//out.printf("START\nMSGTYPE: CONNECT\n GRANT 1 END");
		//Scanner in = new Scanner(System.in);
		//System.out.print("Enter message to send to server: ");
		String resp = client.sendMessage("START\nMSGTYPE: CONNECT\n JUSTIN 1 END");
		//System.out.printf("got response from server '%s'\n", resp);
	}

}
