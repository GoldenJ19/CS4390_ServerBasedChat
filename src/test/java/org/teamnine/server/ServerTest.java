package org.teamnine.server; 
import org.junit.Test;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import org.teamnine.server.Main.Server;
import static org.junit.Assert.assertEquals;

public class ServerTest {
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;

	@Test
    public void testServer() throws Exception {
		final int udpPort = 3636;
		final int tcpPort = 3637;
		final String ip = "127.0.0.1";

		try {
			ServerThread server = new ServerThread(udpPort, tcpPort);		
			serverThread = new Thread(server);

			// Sleep some time to wait for the server to listen
			Thread.sleep(3000);

			// Send CONNECT request to server with valid username.
			String resp = out.println(
				"START\n" +
				"MSGTYPE: CONNECT\n" + 
				"USERNAME: AHAD\n" +
				"RAND_COOKIE: 1\n" +
				"END\n"
			);

			System.out.println(resp);
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
