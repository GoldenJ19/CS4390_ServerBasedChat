package org.teamnine.Server;
import Main.server;
import java.concurrent.Thread;
import java.concurrent.InterruptedException;

public class ServerThread implements Runnable {
	private int udpPort;
	private int tcpPort;

	public Server(int udpPort, int tcpPort) {
		this.udpPort = udpPort;
		this.tcpPort = tcpPort;
	}

	public void run() {
		Server server;
		try {
			server = new Server(udpPort, tcpPort);
		} catch (Exception e) {
			System.err.println("Unexpected error when starting server");
			e.printStackTrace();
			return;
		}	
		
		try {
			if (server != null)
				server.start();
		} catch (InterruptedException e) {
			System.err.println("Caught interrupt, gracefully exiting...");
			server.close();
			return;
		}
	}
}
