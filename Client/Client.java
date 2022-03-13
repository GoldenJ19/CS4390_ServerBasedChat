/*
 *  File Name:      Client.java
 *  Author:         Justin Hardy
 *  Last Modified:  03/13/2022 Justin Hardy
 */

import static java.lang.System.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.ConnectException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private Scanner in;
    private static final int serverPort = 6666;
    private static final String serverIP = "127.0.0.1";

    public void startConnection(String ip, int port) throws Exception {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new Scanner(clientSocket.getInputStream());
    }

    public String sendMessage(String msg) throws Exception {
        System.out.println(msg);
        String resp = in.nextLine();
        System.out.println("Received response from the server: \"" + resp + "\"");
        return resp;
    }

    public void stopConnection() throws Exception {
        in.close();
        out.close();
        clientSocket.close();
    }

    public static void main(String args[]) throws Exception {
        // Establish client object
        Client client = new Client();

        // Try to connect
        boolean connected = false;
        do {
            try {
                // Attempt to connect to server
                client.startConnection(serverIP, serverPort);

                // Connection succeeded
                connected = true;
                System.out.println("Successfully established connection to the server at IP address " + serverIP + " and port " + serverPort + ".");
            } catch (ConnectException e) {
                // Connection failed
                System.out.println("Could not connect to the server at IP address " + serverIP + " and port " + serverPort + ". Error returned:\n" + e.getMessage() + "\n");

                // Ask if they'd like to try to connect again
                System.out.print("Try to connect again? (Y/N):\n");
                Scanner in = new Scanner(System.in);
                String input = in.next();

                // Exit program if they don't want to connect again
                if(input.charAt(0) == 'N' || input.charAt(0) == 'n') {
                    exit(0);
                }
            }
        } while(!connected);

        Scanner in = new Scanner(System.in);
        System.out.print("Enter message to send to server: ");
        String resp = client.sendMessage(in.nextLine());
        System.out.printf("got response from server '%s'\n", resp);
    }

}