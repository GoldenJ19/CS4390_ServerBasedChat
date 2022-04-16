package org.teamnine.client;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.ArrayList;

import org.teamnine.common.Authenticator;
import org.teamnine.common.ParseBuilder;

public class MockServer implements ClientRunnable {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private Scanner in;
    private ParseBuilder pb;
    private String expectedA3 = null;
    private static ArrayList<Integer> portsUsed = new ArrayList<Integer>();
    private ArrayList<MockServer> subServers;
    private MockServer parentServer;
    private ArrayList<Thread> threads;

    private String[] subbedUsers = {"Ahad", "Grant"};

    public void start(int port) throws Exception {
        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new Scanner(clientSocket.getInputStream());
        pb = new ParseBuilder(in);

        String msgType;
        while(true) {
            if (in.hasNext()) {
                msgType = pb.pass("START").pass("MSGTYPE:").extract();
                switch (msgType) {
                    case "HELLO":
                        authenticateHandler1();
                        break;

                    case "RESPONSE":
                        authenticateHandler2();
                        break;

                    case "CONNECT":
                        connectHandler();
                        break;

                    case "CHAT_REQUEST":
                        emulateChat();
                        break;

                    default:
                        throw new Exception("invalid msgType");
                }
            }
            else {
                Thread.sleep(200);
            }
        }
    }

	/*private String parseMsgType() throws Exception {
		String msgType = "";

		if (in.nextLine().equals("START")) {
			if (in.next().equals("MSGTYPE:")) {
				msgType = in.next();
				return msgType;
			} else {
				throw new Exception("couldn't find MSGTYPE after START line");
			}
		} else {
			throw new Exception("message didn't start with START");
		}
	}*/

    private void emulateChat() throws Exception {
        java.util.Random random = new java.util.Random();

        // Read the rest of the input
        String clientB = pb.pass("CLIENTB:").extractLine();
        pb.pass("END");

        // Create chat with fake client
        int seshID = Math.abs(random.nextInt());
        chatStartedResponse(seshID);

        // Some random messages to reply with
        String[] randomMessages = new String[]{"Cool!", "Whatever.", "Okay.", "Gotcha.", "What?", "Could you explain?", "Fine.", "Great!", "Excuse me?", "wtf"};

        // Listener loop
        while(true) {
            if (in.hasNext()) {
                String msgType = pb.pass("START").pass("MSGTYPE:").extract();
                switch (msgType) {
                    case "CHAT":
                        // Read message
                        pb.pass("SESSION_ID:").extract();
                        pb.pass("CHAT_MESSAGE:").extractLine();
                        pb.pass("END");

                        // Reply with random message
                        chatResponse(seshID, randomMessages[random.nextInt(randomMessages.length)]);
                        break;
                    case "END_REQUEST":
                        // Read message
                        pb.pass("SESSION_ID:").extract();
                        pb.pass("END");
                        // Stop chat
                        endNotifResponse(seshID);
                        return;
                    case "HISTORY_RESP":
                        // Read message
                        pb.pass("CLIENTB:").extract();
                        pb.pass("END");

                        // Will do nothing for now
                        break;
                }
            }
        }
    }

    public void stop() throws Exception {
        if (in != null)
            in.close();

        if (out != null)
            out.close();

        if (clientSocket != null)
            clientSocket.close();

        if (serverSocket != null)
            serverSocket.close();
    }

    private void authenticateHandler1() throws Exception {
        ParseBuilder pb = new ParseBuilder(in);

        String username = pb.pass("USERNAME:").extract();
        System.out.println("username = " + username);

        pb.pass("END");

        int random = Math.abs(new java.util.Random().nextInt());
        authChallengeResponse(random);
        expectedA3 = Authenticator.A3(random, username);
    }

    private void authenticateHandler2() throws Exception {
        ParseBuilder pb = new ParseBuilder(in);

        String a3Result = pb.pass("RES:").extract();

        pb.pass("END");

        if( a3Result.equals(expectedA3) ) {
            // Print message
            System.out.println("Authentication succeeded.");

            // Generate port number
            int port = portsUsed.size() != 1 ? portsUsed.get(portsUsed.size()-1)+1 : 1;
            portsUsed.add(port);

            System.out.println("Ports Used:");
            for( int p : portsUsed ) {
                System.out.println("• " + p);
            }

            // Create new server and link this and the new one together
            Thread t = new Thread(this, "Port " + port);
            t.start();
            System.out.println("Thread created & started.");

            // Add thread to thread list
            threads.add(t);

            // Send response
            authSuccessResponse(port);
        }
        else {
            authFailureResponse();
        }

        this.stop();
        this.start(6666);
    }

    private void connectHandler() throws Exception {
        ParseBuilder pb = new ParseBuilder(in);
        String username = pb.pass("USERNAME:").extract();
        String randCookieStr = pb.pass("RAND_COOKIE:").extract();
        int rand_cookie = Integer.parseInt(randCookieStr);
        System.out.println("rand_cookie = " + rand_cookie);
        pb.pass("END");

        boolean isSubbed = false;
        for (String user : subbedUsers) {
            if (username.equalsIgnoreCase(user)) {
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

    private void chatStartedResponse( int seshID ) {
        out.printf("START\nMSGTYPE: CHAT_STARTED\nSESSION_ID: " + seshID + "\nEND\n");
    }

    private void chatResponse( int seshID, String message ) {
        out.printf("START\nMSGTYPE: CHAT\nSESSION_ID: " + seshID + "\nCHAT_MESSAGE: " + message + "\nEND\n");
    }

    private void endNotifResponse( int seshID ) {
        out.printf("START\nMSGTYPE: END_NOTIF\nSESSION_ID: " + seshID + "\nEND\n");
    }

    private void authChallengeResponse( int random ) {
        out.printf("START\nMSGTYPE: CHALLENGE\nRAND: " + random + "\nEND\n");
    }

    private void authSuccessResponse( int port ) throws Exception {
        out.printf("START\nMSGTYPE: AUTH_SUCCESS\nRAND_COOKIE: " + new java.util.Random().nextInt() + "\nPORT_NUMBER: " + port + "\nEND\n");
        System.out.println("AUTH_SUCCESS message sent to client.");
    }

    private void authFailureResponse() {
        out.printf("START\nMSGTYPE: AUTH_FAIL\nEND\n");
    }

    private void connectedResponse() {
        out.printf("START\nMSGTYPE: CONNECTED\nEND\n");
    }

    public static void main(String[] args) throws Exception {
        MockServer server = new MockServer();
        try {
            portsUsed.add(6666);
            server.subServers = new ArrayList<MockServer>();
            server.parentServer = null;
            server.threads = new ArrayList<Thread>();
            server.start(6666);
        } finally {
            server.stop();
        }
    }

    public void runThrowable() throws Exception {
        // Get new port number
        int port = portsUsed.get(portsUsed.size()-1);

        // Create new server
        MockServer subServer = new MockServer();
        subServer.parentServer = this;
        subServers.add(subServer);
        subServer.start(port);
    }

}
//there should be a separate clas clienthandler. accepting incoming requests. once it receives requests then its going to connect