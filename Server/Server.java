/*
 *  File Name:      Server.java
 *  Author:         Justin Hardy
 *  Last Modified:  03/13/2022 Grant Jin
 */

import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader; 
import java.net.Socket;
import java.net.ServerSocket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Server {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private Scanner in;
    
    private String[]subbedUsers = {"AHAD", "GRANT"};

    public void start(int port) throws Exception {
        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new Scanner(clientSocket.getInputStream());
        
        String msgType = "";
        if(in.nextLine().equals("START")) {
        	if(in.next().equals("MSGTYPE:")) {
        		msgType = in.next();
        		
        		if(msgType.equals("CONNECT")) {
        			connectHandler();
        		}
        		
        		
        	}
        }
        
        else{
        	System.out.println("Did not begin with START message.");
        }
        /*String clientMsg = in.readLine();
		System.out.println("got client message: " + clientMsg);	
		out.printf("got this message from you '%s'\n", clientMsg);*/
    }

    public void stop() throws Exception {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
    
    public void connectHandler() throws Exception {
    	String username = in.next();
    	int rand_cookie = in.nextInt();
    	boolean isSubbed = false;
    	for(String user:subbedUsers) {
    		if(username.equals(user)) {
    			isSubbed = true;
    			break;
    		}
    	}
    	
    	if(isSubbed) {
    		System.out.println("User " + username + " is valid.");
    	}
    	else {
    		System.out.println("User " + username + " is NOT valid.");
    		stop();
    	}
    	
    }
    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.start(6666);
    }
    
    
}
