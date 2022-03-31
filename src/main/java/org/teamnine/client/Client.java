/*
 *  File Name:      Client.java
 *  Author:         Justin Hardy
 *  Last Modified:  03/15/2022 Justin Hardy
 */
package org.teamnine.client;

import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

import org.teamnine.common.ParseBuilder;

public class Client {
    private Socket clientSocket;
    private PrintWriter socket_out;
    private Scanner socket_in;
    private Random randomGen;
    private ParseBuilder pb;
    private static final int serverPort = 6666;
    private static final String serverIP = "127.0.0.1";

    public boolean startConnection(String ip, int port) throws Exception {
        // Create socket to server
        clientSocket = new Socket(ip, port);
        socket_out = new PrintWriter(clientSocket.getOutputStream(), true);
        socket_in = new Scanner(clientSocket.getInputStream());
        randomGen = new Random();
        pb = new ParseBuilder(socket_in);

        // Login
        Scanner console = new Scanner(System.in);
        System.out.print("Enter login username: ");
        String username = console.nextLine().trim();

        String[] response = null;
        int tries = 5;
        do {
            // Send connect request to server
            socket_out.println("START\n" +
                    "MSGTYPE: CONNECT\n" +
                    "USERNAME: " + username + "\n" +
                    "RAND_COOKIE: " + (randomGen.nextInt(1000) + 1) + "\n" +
                    "END");

            // Receive response
            response = receiveMessage();
        } while (--tries != 0 && (response == null || !Boolean.parseBoolean(response[0]))); // Check if response was valid

        if(tries == 0) {
            return false;
        }

        return true;
    }

    public String[] receiveMessage() throws Exception {
        // Create variable to hold the return type; what to include in the returned string array
        int returnType = -1;/** -1 = {"false"}, 0 = {"true"}, 1 = {seshID}, 2 = {clientB}, 3 = {seshID, message}, ... */
        // Create info variables
        int seshID = -1;
        String msgType = null, clientB = null, message = null;

        // Receive response from server
        if( socket_in.hasNextLine() ) {
            // Parse through message
            /// Extract message type
            msgType = pb.pass("START").pass("MSGTYPE:").extract();

            /// Switch case to determine next steps
            switch (msgType) {
                case "AUTH_FAIL":
                    returnType = -1;
                    break;
                case "CONNECTED":
                    returnType = 0;
                    break;
                case "CHAT_STARTED":
                    seshID = Integer.parseInt(pb.pass("SESSION_ID:").extract());
                    returnType = 1;
                    break;
                case "UNREACHABLE":
                    clientB = pb.pass("CLIENTB:").extract();
                    returnType = 2;
                    break;
                case "CHAT":
                    seshID = Integer.parseInt(pb.pass("SESSION_ID:").extract());
                    message = pb.pass("CHAT_MESSAGE:").extract();
                    returnType = 3;
                    break;
                default:
                    return null;
            }
            // Process "END" line.
            pb.pass("END");
        }

        switch(returnType) {
            case -1:
                return new String[]{"false"};
            case 0:
                return new String[]{"true"};
            case 1:
                return new String[]{seshID+""};
            case 2:
                return new String[]{clientB};
            case 3:
                return new String[]{seshID+"", message};
            case 4:
                // add more cases here
                return null;
            default:
                return null;
        }
    }

    public String sendMessage(String msg) throws Exception {
        System.out.println(msg);
        String resp = socket_in.nextLine();
        System.out.println("Received response from the server: \"" + resp + "\"");
        return resp;
    }

    public void stopConnection() throws Exception {
        socket_in.close();
        socket_out.close();
        clientSocket.close();
    }

    public static void main(String args[]) throws Exception {
        // Establish client object
        Client chatClient = new Client();

        // Try to connect
        boolean connected = false;
        do {
            try {
                // Attempt to connect to server
                connected = chatClient.startConnection(serverIP, serverPort);
            } catch (ConnectException e) {
                // Connection failed
                System.out.println("Could not connect to the server at IP address " + serverIP + " and port " + serverPort + ". Error returned:\n" + e.getMessage() + "\n");

                // Ask if they'd like to try to connect again
                System.out.print("Try to connect again? (Y/N):\n");
                Scanner in = new Scanner(System.in);
                String input = in.next();

                // Exit program if they don't want to connect again
                if(input.charAt(0) == 'N' || input.charAt(0) == 'n') {
                    System.exit(0);
                }
            }
        } while(!connected);

        // Connection succeeded
        System.out.println("Successfully established connection to the server at IP address " + serverIP + " and port " + serverPort + ".");


    }

}
