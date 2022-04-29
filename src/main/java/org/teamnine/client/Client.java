/*
 *  File Name:      Client.java
 *  Author:         Justin Hardy
 *  Last Modified:  03/15/2022 Justin Hardy
 */
package org.teamnine.client;

import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.text.NumberFormat;
import java.util.Random;
import java.util.Scanner;
import java.util.Formatter;
import java.util.ArrayList;

import org.teamnine.common.ParseBuilder;

public class Client implements ClientRunnable {
    private Socket clientSocket;
    private PrintWriter socket_out;
    private Scanner socket_in;
    private Random randomGen;
    private ParseBuilder pb;
    private int seshID = -1;
    private static final int serverPort_welcoming = 6666;
    private static final String serverIP = "127.0.0.1", messageFormat = "%16s:\t%s\n", command_connect = "CONNECT", command_exitapp = "EXIT", command_logoff = "LOG OFF",
    _separator = "----------------------------------------------";
    private int serverPort = -1;
    private int rand_cookie = -1;
    private String loggedInUsername = null, chattingWith = null;
    private ArrayList<String> messageHistory;
    private static Thread mainThread = null, inputThread = null;
    private static String receivedInput = null;

    public boolean contactServerWelcomingSocket(String ip, int port) throws Exception {
        // Login
        Scanner console = new Scanner(System.in);
        System.out.print("Enter login username\n>");
        String username = console.nextLine().trim();
        System.out.println();

        String[] response = null;
        int tries = 5;
        do {
            // Create socket to server
            clientSocket = new Socket(ip, port);
            socket_out = new PrintWriter(clientSocket.getOutputStream(), true);
            socket_in = new Scanner(clientSocket.getInputStream());
            randomGen = new Random();
            pb = new ParseBuilder(socket_in);

            // Send connect request to server
            socket_out.println("START\n" +
                    "MSGTYPE: HELLO\n" +
                    "USERNAME: " + username + "\n" +
                    "END");

            // Receive response
            response = receiveMessage();

            // Validate response... should be CHALLENGE, otherwise AUTH_FAIL
            if( response != null && response[0].equals("CHALLENGE") ) {
                /// Continue with validation process
                // Parse rand to an int
                int rand = Integer.parseInt(response[1]);

                // Use rand to generate RES
                String RES = org.teamnine.common.Authenticator.A3(rand, username); // use username as secret key just for now

                // Send response to server
                socket_out.println("START\n" +
                        "MSGTYPE: RESPONSE\n" +
                        "RES: " + RES + "\n" +
                        "END");

                // Receive response to response
                response = receiveMessage();

                // Validate response... should be AUTH_SUCCESS, otherwise AUTH_FAIL
                if( response != null && response[0].equals("AUTH_SUCCESS") ) {
                    // Parse rand_cookie and serverPort to an int
                    rand_cookie = Integer.parseInt(response[1]);
                    serverPort = Integer.parseInt(response[2]);
                    loggedInUsername = username;
                    return true;
                }
                else if ( response != null && response[0].equals("AUTH_FAIL") ) {
                    System.out.println("ERROR: Server returned AUTH_FAIL when attempting to authenticate.");
                }
                else {
                    System.out.println("ERROR: Server returned unexpected message when attempting to authenticate.");
                }
            }
            else if ( response != null && response[0].equals("AUTH_FAIL") ) {
                System.out.println("ERROR: Server returned AUTH_FAIL when attempting to authenticate.");
            }
            else {
                System.out.println("ERROR: Server returned unexpected message when attempting to authenticate.");
            }
        } while (tries-- != 0); // Check if response was valid

        return false;
    }

    public boolean startConnection(String ip, int port) throws Exception {
        // Send connect request to server
        String[] response = null;
        int tries = 5;
        do {
            // Create socket to server
            clientSocket = new Socket(ip, port);
            socket_out.close();
            socket_out = new PrintWriter(clientSocket.getOutputStream(), true);
            socket_in.close();
            socket_in = new Scanner(clientSocket.getInputStream());
            pb = new ParseBuilder(socket_in);

            // Send connect request to server
            socket_out.println("START\n" +
                    "MSGTYPE: CONNECT\n" +
                    "USERNAME: " + loggedInUsername + "\n" +
                    "RAND_COOKIE: " + rand_cookie + "\n" +
                    "END");

            // Receive response
            response = receiveMessage();

            // Validate response... should be CONNECTED, otherwise AUTH_FAIL
            if( response != null && response[0].equals("CONNECTED") ) {
                return true;
            }
            else if ( response != null && response[0].equals("AUTH_FAIL") ) {
                System.out.println("ERROR: Server returned AUTH_FAIL when attempting connect.");
            }
            else {
                System.out.println("ERROR: Server returned unexpected message when attempting to connect.");
            }
        } while(tries-- != 0);

        return false;
    }

    public boolean startChat( String clientB ) throws Exception {
        if( clientSocket.isConnected() ) {
            // Send chat request to server
            //String[] response = null;

            // Send connect request to server
            socket_out.println("START\n" +
                    "MSGTYPE: CHAT_REQUEST\n" +
                    "CLIENTB: " + clientB + "\n" +
                    "END");

            /* This shouldn't be needed anymore since it is handled by another thread.
            // Receive response
            response = receiveMessage();

            // Validate response
            if( response != null && response[0].equals("CHAT_STARTED") ) {
                seshID = Integer.parseInt(response[1]);
                return true;
            }
            else if ( response != null && response[0].equals("AUTH_FAIL") ) {
                System.out.println("ERROR: Server returned AUTH_FAIL when attempting to start chat with user " + clientB  + ".");
            }
            else {
                System.out.println("ERROR: Server returned unexpected message when attempting to start chat with user " + clientB + ".");
            }
            */
        }

        return false;
    }

    public boolean sendChat( String message ) throws Exception {
        if( clientSocket.isConnected() ) {
            // Send chat request to server
            //String[] response = null;

            // Send connect request to server
            socket_out.println("START\n" +
                    "MSGTYPE: CHAT\n" +
                    "SESSION_ID: " + seshID + "\n" +
                    "CHAT_MESSAGE: " + message + "\n" +
                    "END");

            // Add to message history
            String _message = String.format(messageFormat, loggedInUsername, message);
            messageHistory.add(_message);

            /* This shouldn't be needed anymore since another thread handles this
            // Receive response
            response = receiveMessage();

            // make sure response is not received
            if( response != null && response[0].equals("UNREACHABLE") ) {
                // Client B unreachable
                String clientB = response[1];
                System.out.println("ERROR: Server returned UNREACHABLE when attempting to chat with user " + clientB  + ".");
                seshID = -1;
            }
            else if (response == null) {
                // Message was sent successfully
                return true;
            }
            else {
                System.out.println("ERROR: Server returned unexpected message when send message.");
            }*/

            return true;
        }

        return false;
    }

    public boolean endChat() throws Exception {
        // Check if we're actually connected to something
        if( clientSocket.isConnected() ) {
            // Send chat request to server
            //String[] response = null;

            // Send connect request to server
            socket_out.println("START\n" +
                    "MSGTYPE: END_REQUEST\n" +
                    "SESSION_ID: " + seshID + "\n" +
                    "END");

            /* This shouldn't be needed anymore since another thread handles this.
            // Receive response
            response = receiveMessage();


            // Validate response
            if( response != null && response[0].equals("END_NOTIF") ) {
                seshID = -1;
                return true;
            }
            else {
                System.out.println("ERROR. Unexpected Session terminated.");
                return false;
            }*/
            return true;
        }
        else {
            // Disconnect from server entirely
            stopConnection();
            return true;
        }
    }

    public boolean getMessageHistory( String clientB ) throws Exception {
        if( clientSocket.isConnected() ) {
            // Send chat request to server
            //String[] response = null;

            // Send connect request to server
            socket_out.println("START\n" +
                    "MSGTYPE: HISTORY_REQ\n" +
                    "CLIENTB: " + clientB + "\n" +
                    "END");

            /* This shouldn't be needed anymore since another thread handles this
            // Receive response
            response = receiveMessage();

            // make sure response is not received
            if( response != null && response[0].equals("UNREACHABLE") ) {
                // Client B unreachable
                String clientB = response[1];
                System.out.println("ERROR: Server returned UNREACHABLE when attempting to chat with user " + clientB  + ".");
                seshID = -1;
            }
            else if (response == null) {
                // Message was sent successfully
                return true;
            }
            else {
                System.out.println("ERROR: Server returned unexpected message when send message.");
            }*/

            return true;
        }

        return false;
    }

    public void stopConnection() throws Exception {
        socket_in.close();
        socket_out.close();
        clientSocket.close();
        seshID = -1;
    }

    public String[] receiveMessage() throws Exception {
        // Create info variables
        int seshID = -1, rand = -1, rand_cookie = -1, port_number = -1;
        String msgType = null, clientB = null, message = null;

        // Receive response from server
        if( socket_in.hasNext() ) {
            // Parse through message
            /// Extract message type
            msgType = pb.pass("START").pass("MSGTYPE:").extract();

            /// Switch case to determine next steps
            switch (msgType) {
                // Return nothing extra
                case "AUTH_FAIL":
                case "CONNECTED":
                    pb.pass("END");
                    return new String[]{msgType};

                // Return seshID
                case "END_NOTIF":
                case "CHAT_STARTED":
                    seshID = Integer.parseInt(pb.pass("SESSION_ID:").extract());
                    pb.pass("END");
                    return new String[]{msgType, seshID+""};

                // Return clientB name
                case "UNREACHABLE":
                    clientB = pb.pass("CLIENTB:").extract();
                    pb.pass("END");
                    return new String[]{msgType, clientB};

                // Return seshID and message
                case "CHAT":
                    seshID = Integer.parseInt(pb.pass("SESSION_ID:").extract());
                    message = pb.pass("CHAT_MESSAGE:").extractLine();
                    pb.pass("END");
                    return new String[]{msgType, seshID+"", message};

                // Return rand
                case "CHALLENGE":
                    rand = Integer.parseInt(pb.pass("RAND:").extract());
                    pb.pass("END");
                    return new String[]{msgType, rand+""};

                // Return rand_cookie and port_number
                case "AUTH_SUCCESS":
                    rand_cookie = Integer.parseInt(pb.pass("RAND_COOKIE:").extract());
                    port_number = Integer.parseInt(pb.pass("PORT_NUMBER:").extract());
                    pb.pass("END");
                    return new String[]{msgType, rand_cookie+"", port_number+""};

                default:
                    pb.pass("END");
            }
        }
        return null;
    }

    public synchronized String loggedInPrompt( Scanner in ) throws InterruptedException {
        // Create input thread with correct specifications, and start it
        runInputThread(in, false);

        // Wait for input to be received
        synchronized (mainThread) {
            mainThread.wait();
        }
        System.out.println();

        // Read in input
        String toReturn = receivedInput;
        receivedInput = null;

        // kill thread if it is still alive
        if ( inputThread.isAlive() )
            inputThread.interrupt();
        inputThread = null;

        return toReturn;
    }

    public synchronized boolean chattingPrompt( Scanner in ) throws InterruptedException {
        // Create input thread with correct specifications, and start it
        runInputThread(in, true);

        // Wait for input to be received
        synchronized (mainThread) {
            mainThread.wait();
        }
        System.out.println();

        // Reset input
        receivedInput = null;

        // kill thread if it is still alive
        if ( inputThread.isAlive() )
            inputThread.interrupt();
        inputThread = null;

        return false;
    }

    public void runInputThread( Scanner in, boolean chatting ) {
        inputThread = new Thread(
            new ClientRunnable() {
                @Override
                public void runThrowable() {
                    boolean inputGiven = false;
                    while (!inputGiven) {
                        if (!chatting) {
                            /** User Selection Loop */
                            System.out.printf("Enter username of person you'd like to chat with (or enter %s):\n>", command_logoff);
                            receivedInput = in.nextLine().trim();
                            System.out.println();

                            // Check if user entered "LOG OFF"
                            if (receivedInput.equalsIgnoreCase(command_logoff)) {
                                System.out.println("\nLogging Off...");
                                System.exit(0);
                            } else {
                                System.out.printf("Attempt to chat with user %s? (Y/N)\n>", receivedInput);
                                String response = in.nextLine();
                                if (response.equalsIgnoreCase("Y")) {
                                    inputGiven = true;
                                }
                            }
                        } else {
                            /** Chat loop */
                            while( !inputGiven ) {
                                // Prompt user
                                System.out.printf("CHATTING WITH: %s\n%s:\n%s\n%s\n| %-60s |\n| %-60s |\n>", chattingWith, _separator, getMessageHistoryString(), _separator, "Enter 1 to send a chat, 2 to refresh your message history", "3 to view full message history, or 4 to disband chat session");
                                receivedInput = in.nextLine().trim();
                                System.out.println();

                                // Determine what was entered
                                int selection = -1;
                                try {
                                    selection = Integer.parseInt(receivedInput);
                                } catch( NumberFormatException e ) {
                                    // Invalid input
                                    ClearConsole();
                                    System.out.println("\nInvalid Input. Please try again\n");
                                    continue;
                                }

                                // Valid input
                                switch(selection) {
                                    case 1:
                                        // Prompt user to enter the message they'd like to send:
                                        System.out.printf("Enter the message you'd like to send:\n>");
                                        receivedInput = in.nextLine().trim();
                                        System.out.println("\n");

                                        // Confirm message they entered is what they want to send.
                                        System.out.printf("Please confirm you'd like to send the following message (Y/N):\n\"%s\"\n>", receivedInput);
                                        String response = in.nextLine().trim();
                                        System.out.println();

                                        // Parse their input
                                        if(response.equalsIgnoreCase("Y")) {
                                            try {
                                                // Send message
                                                if(!sendChat(receivedInput))
                                                    throw new Exception("sendChat() function returned false");
                                                ClearConsole();
                                                System.out.println("Chat message successfully sent.\n");
                                            } catch( Exception e ) {
                                                // In case it doesn't succeed, print this message and loop back.
                                                ClearConsole();
                                                System.out.println("Failed to send chat. Please try again.\n");
                                            }
                                        }
                                        else {
                                            // Abort message sending
                                            ClearConsole();
                                            System.out.println("Message send protocol aborted.\n");
                                        }
                                        break;

                                    case 2:
                                        ClearConsole();
                                        continue;

                                    case 3:
                                        try {
                                            ClearConsole();
                                            getMessageHistory(chattingWith);
                                        } catch( Exception e ) {
                                            // In case it doesn't succeed, print this message and loop back.
                                            ClearConsole();
                                            System.out.println("Failed to get message history. Please try again.\n");
                                        }
                                        break;

                                    case 4:
                                        try {
                                            if (!endChat())
                                                throw new Exception("endChat() function returned false");
                                            //inputGiven = true;
                                            Thread.sleep(60_000);
                                        } catch( Exception e ) {
                                            // In case it doesn't succeed, print this message and loop back.
                                            ClearConsole();
                                            System.out.println("Failed to end chat. Please try again.\n");
                                        }
                                        break;

                                    default:
                                        // Invalid input
                                        ClearConsole();
                                        System.out.println("Invalid Input. Please try again\n");
                                        break;
                                }
                            }
                        }
                    }

                    // Notify main thread
                    synchronized( mainThread ) {
                        mainThread.notify();
                    }
                }
            }, "Input Thread");
        inputThread.start();
    }

    private String getMessageHistoryString() {
        // Put message history into one big string, separated by newlines.
        String toReturn = "";
        for( String message : messageHistory ) {
            toReturn += message;
        }

        // Return message history without the final newline character, or a string stating that there are no messages in the history.
        return toReturn.isEmpty() ? "<No Messages in History>" : toReturn.substring(0, toReturn.lastIndexOf("\n"));
    }

    /**
     * Main Function, runs the Client program.
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        // To check input of terminal
        Scanner in = new Scanner(System.in);
        System.out.printf("Welcome to the Server-Based Chat Application\nPlease enter the command %s or %s\n>", command_connect, command_exitapp);
        String userIn = in.nextLine();
        System.out.println();
        if (userIn.equalsIgnoreCase(command_exitapp)) {
            System.exit(0);
        } else if (userIn.equalsIgnoreCase(command_connect)) {
            // Establish client object
            Client chatClient = new Client();

            // Try to connect to welcoming socket
            boolean connected = false;
            do {
                try {
                    // Attempt to connect to server
                    connected = chatClient.contactServerWelcomingSocket(serverIP, serverPort_welcoming);
                } catch (ConnectException e) {
                    // Connection failed
                    System.out.println("Could not connect to the server at IP address " + serverIP + " and welcoming port " + serverPort_welcoming + ". Error returned:\n" + e.getMessage() + "\n");

                    // Ask if they'd like to try to connect again
                    System.out.print("Try to connect again? (Y/N):\n");
                    String input = in.next();

                    // Exit program if they don't want to connect again
                    if (input.charAt(0) == 'N' || input.charAt(0) == 'n') {
                        System.exit(0);
                    }
                }
            } while (!connected);

            // Notify user that connection was successful
            System.out.println("Connected to " + serverIP + ":" + serverPort_welcoming + "\n");

            // Connect to socket made by server for this client
            int tries = 5;
            connected = false;
            chatClient.clientSocket.close();
            do {
                try {
                    connected = chatClient.startConnection(serverIP, chatClient.serverPort);
                } catch (ConnectException e) {
                    // Connection failed
                    System.out.println("Could not connect to the server at IP address " + serverIP + " and port " + chatClient.serverPort + ". Error returned:\n" + e.getMessage() + "\n");

                    // Ask if they'd like to try to connect again
                    System.out.print("Try to connect again? (Y/N):\n");
                    String input = in.next();

                    // Exit program if they don't want to connect again
                    if (input.charAt(0) == 'N' || input.charAt(0) == 'n') {
                        System.exit(0);
                    }
                }
            } while (!connected && tries-- != 0);

            // Connection succeeded
            ClearConsole();
            System.out.println("Connected to " + serverIP + ":" + chatClient.serverPort + ".\n");

            // Create a new thread & add reference to main threads
            Thread listener = new Thread(chatClient, "Listener");
            mainThread = Thread.currentThread();
            listener.start();

            while(true) {
                // Allow the user to put in name of person they want to chat with
                boolean clientFound = false;

                /** Repeat until user enters a client we can chat with. */
                while (!clientFound) {
                    String usernameDest = null;
                    try {
                        usernameDest = chatClient.loggedInPrompt(in);
                    } catch (InterruptedException e) {
                        // Kill input thread
                        inputThread.stop();
                        inputThread = null;

                        // Join chat session we've been invited to
                        System.out.print("\nATTENTION! Another user has started a chat with you. Entering chat room.");
                        clientFound = true;
                        Thread.sleep(500);
                        System.out.print(".");
                        Thread.sleep(500);
                        System.out.print(".");
                        Thread.sleep(500);
                        ClearConsole();
                        continue;
                    }

                    // Send chat request message to server
                    chatClient.startChat(usernameDest);

                    // Notify user that we are attempting to start a chat
                    System.out.printf("Starting chat session with %s...\n", usernameDest);

                    try {
                        // Wait 30 seconds for response (reasonable amount of time)
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        // Join chat session we've been invited to
                        System.out.print("\nATTENTION! Chat session initiated. Entering chat room.");
                        chatClient.chattingWith = usernameDest;
                        clientFound = true;
                        Thread.sleep(500);
                        System.out.print(".");
                        Thread.sleep(500);
                        System.out.print(".");
                        Thread.sleep(500);
                        ClearConsole();
                        continue;
                    }

                    System.out.println("ERROR. Chat request timed out. Please try again.\n");
                }

                /** Start a loop to handle the chat session */
                boolean chatting = true;
                do {
                    try {
                        chatting = chatClient.chattingPrompt(in);
                    } catch (InterruptedException e) {
                        // Kill input thread
                        inputThread.stop();
                        inputThread = null;
                        chatting = false;

                        // Leave chat session
                        Thread.sleep(3000);
                        ClearConsole();
                        continue;
                    }
                } while (chatting);
            }
        }
    }

    /**
     * Function for listener thread
     * @throws Exception
     */
    public void runThrowable() throws Exception {
        // Listen while this thread is active
        while(true) {
            // Listen for server response
            String[] response = receiveMessage();

            // Check session id, if -1 we know we're not in a chat right now.
            if(response != null) {
                if (seshID == -1 && response[0].equals("CHAT_STARTED")) {
                    /// We're not in a chat session
                    // Initialize chat session
                    seshID = Integer.parseInt(response[1]);
                    messageHistory = new ArrayList<String>();

                    // Need to stop the user from entering a person to chat with if someone starts a chat
                    // session with us.
                    interruptThread(mainThread);
                } else if(seshID != -1) {
                    /// We are in a chat session
                    int _seshID = -1;
                    String clientB = null;
                    switch( response[0] ) {
                        case "CHAT":
                            // Whenever we get a chat, first add the message to the message history
                            // and then, check and make message history does not exceed a specific length (10)
                            // we should remove the first index in message history (the last thing we received)
                            _seshID = Integer.parseInt(response[1]);
                            if( seshID == _seshID ) {
                                // Read message from Client B
                                String message = response[2];
                                String _message = String.format(messageFormat, chattingWith, message);
                                messageHistory.add(_message);

                                // Remove least recently received message when we have 11 messages (to keep it at max of 10).
                                if(messageHistory.size() > 10) {
                                    messageHistory.remove(0);
                                }
                            }
                            else {
                                // Message received from client we're not chatting with...somehow
                                System.out.println("ERROR: Received message from unexpected client.");
                            }
                            break;

                        case "UNREACHABLE":
                            // Whenever we get this message, we want to end the chat session, but print a special
                            // print statement that lets us know we lost connection with client b (whatever its name is)
                            clientB = response[1];
                            seshID = -1;
                            System.out.println("\nCONNECTION TERMINATED: " + clientB + " has lost connection to the chat session.");
                            interruptThread(mainThread);
                            break;

                        case "END_NOTIF":
                            // Whenever we get this message, we want to end the chat session, but print a special
                            // print statement letting us know we (or the other client) decided to end the chat session
                            _seshID = Integer.parseInt(response[1]);
                            if( seshID == _seshID ) {
                                seshID = -1;
                            }
                            else {
                                System.out.println("ERROR: Received chat termination message from unexpected client.");
                            }
                            System.out.println("\nCONNECTION TERMINATED: Chat session has been ended by either you or the other user.");
                            interruptThread(mainThread);
                            break;

                        case "HISTORY_RESP":
                            // Print entire history to console
                            clientB = response[1];
                            String _message = response[2];
                            System.out.printf(messageFormat, clientB, _message);
                            break;

                        default:
                            System.out.println("ERROR! Unexpected message received from server!");
                    }
                }
                else {
                    System.out.println("ERROR! Unexpected message received from server!");
                }
            }
        }
    }

    private static void interruptThread( Thread thread ) {
        try {
            thread.interrupt();
        } catch( Exception e ) {
            System.out.println("ERROR: Failed to interrupt thread.");
        }
    }

    // Does not work on IntelliJ Run console, must use an OS-based terminal.
    // You can use IntelliJ's terminal instead, by changing directories to \target\classes, and then running the "java org.teamnine.client.Client" command.
    private static void ClearConsole(){
        try{
            String operatingSystem = System.getProperty("os.name");

            if(operatingSystem.contains("Windows")){
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "cls");
                Process startProcess = pb.inheritIO().start();
                startProcess.waitFor();
            } else {
                ProcessBuilder pb = new ProcessBuilder("clear");
                Process startProcess = pb.inheritIO().start();

                startProcess.waitFor();
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }
}
