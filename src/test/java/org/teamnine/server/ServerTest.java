package org.teamnine.server; 
import org.junit.Test;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import static org.junit.Assert.assertEquals;

public class ServerTest {
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	
	// Starts the server on a new thread and returns the thread reference.
	private Thread startServer(int port) throws Exception {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Server server = new Server();
				try {
					server.start(port); 
				} catch (Exception e) {
					try {
						e.printStackTrace();
						server.stop();
					} catch (Exception e2) {
						e2.printStackTrace();
						throw new RuntimeException();
					}

					throw new RuntimeException();
				}
			}
		});

		return thread;
	}

	// startConnection connects to the server and connects relevant pipes.
    private void startConnection(String ip, int port) throws Exception {
		clientSocket = new Socket(ip, port);
		out = new PrintWriter(clientSocket.getOutputStream(), true); 
		in = new BufferedReader(new 
				InputStreamReader(clientSocket.getInputStream()));
    }

	// sendMessage sends a 'msg' to the socket and records the response.
    private String sendMessage(String msg) throws Exception {
		out.println(msg);

		String resp = "";
		for (int i = 0; i < 3; i++) {
			resp += in.readLine() + "\n";
		}
		return resp;
    }

	// stopConnection closes the pipes and sockets.
	private void stopConnection() throws Exception {
		if (in != null)
			in.close();

		if (out != null)
			out.close();
		
		if (clientSocket != null)
			clientSocket.close();
    }

	@Test
    public void testServer() throws Exception {
		final int port = 3636;
		final String ip = "127.0.0.1";

		Thread srvThread = null;
		try {
			// Run the server on a separate thread.
			srvThread = startServer(port);
			srvThread.start();
			
			// Sleep some time to wait for the server to listen
			Thread.sleep(3000);
			startConnection(ip, port);

			// Send CONNECT request to server with valid username.
			String resp = sendMessage(
				"START\n" +
				"MSGTYPE: CONNECT\n" + 
				"USERNAME: AHAD\n" +
				"RAND_COOKIE: 1\n" +
				"END\n"
			);

			// Test and see if we got a connected response.
			assertEquals(resp, 
				"START\n" +
				"MSGTYPE: CONNECTED\n" +
				"END\n"
			);
				
		} finally {
			stopConnection();
			if (srvThread != null)
				srvThread.join();
		}
    }
}
