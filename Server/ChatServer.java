import java.net.Socket;
import java.net.ServerSocket;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.Scanner;

public class ChatServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private Scanner in;
    
    private String[] subbedUsers = {"AHAD", "GRANT"};

    public void start(int port) throws Exception {
        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new Scanner(clientSocket.getInputStream());

       	parseMsgType(); 

        /*String clientMsg = in.readLine();
		System.out.println("got client message: " + clientMsg);	
		out.printf("got this message from you '%s'\n", clientMsg);*/
    }

	private void parseMsgType() throws Exception {
        String msgType = "";
        if (in.nextLine().equals("START")) {
        	if (in.next().equals("MSGTYPE:")) {
        		msgType = in.next();
				switch (msgType) {
					case "CONNECT":
						connectHandler();
						break;
					default:
						throw new Exception("couldn't find an implemented MSGTYPE in message body");
				}
        	} else {
				throw new Exception("couldn't find MSGTYPE after START line");
			}
        } else {
			throw new Exception("message didn't start with START");
		}
	}

    public void stop() throws Exception {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
    
    private void connectHandler() throws Exception {
    	String username = in.next();
    	int rand_cookie = in.nextInt();
    	boolean isSubbed = false;
    	for (String user : subbedUsers) {
    		if (username.equals(user)) {
    			isSubbed = true;
    			break;
    		}
    	}
    	
    	if (isSubbed) {
    		System.out.println("User " + username + " is valid.");
			connectedResponse();
    	} else {
    		System.out.println("User " + username + " is NOT valid.");
    		stop();
    	}
    	
    }

	private void connectedResponse() {
		out.printf("START\nMSGTYPE: CONNECTED\nEND\n");
	}

    public static void main(String[] args) throws Exception {
        ChatServer server = new ChatServer();
        server.start(6666);
    }
    
    
}
