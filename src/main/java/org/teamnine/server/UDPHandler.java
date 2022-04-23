package org.teamnine.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPHandler 
{
	
	private DatagramSocket UDPSocket;
	private byte[] receive;
	
	public UDPHandler(int udpPort) throws SocketException{
		UDPSocket = new DatagramSocket(udpPort);
		receive = new byte[10000];
	}
	
	public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }
	
	public void securityTest() {
		DatagramPacket UDPclientmsg = new DatagramPacket(receive, receive.length);
		
		try {
			UDPSocket.receive(UDPclientmsg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringBuilder helloRes = data(receive);
	}
}
