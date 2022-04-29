package org.teamnine.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Map;

import org.teamnine.common.*;

public class UDPHandler 
{
	private static String[] clientIDs = {"test", "test2"};
	private static Map <String, String> clientChallenges;
	private Authenticator auth;
	private ParseBuilder parser;
	//private Scanner in;
	public UDPHandler(){
		auth = new Authenticator();
		//in = new Scanner();
		//parser = new ParseBuilder();
	}
	
	public static String data(byte[] a)
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
        return ret.toString();
    }
	
	//Given a byte string, convert to a normal string and determine
	//given string (clientID) is in the list of valid users
	public static int securityTest(byte[] received) throws IOException {
		boolean validUser = false;
		String clientA = data(received);
		System.out.println(clientA);
		for(String client : clientIDs){
			if(client.equals(clientA))
			{
				validUser = true;
			}
		}
		
		//If user within validIDs, create challenge. Otherwise, return -1
		if(validUser){
			return createChallenge(clientA);
		}
		else {
			return -1;
		}
	}
	
	public static boolean processResponse(byte[] received) throws IOException {
		boolean correctKey = false;
		String loginAttempt = data(received);
		System.out.println(loginAttempt);
		for(String client : clientIDs){
			if(client.equals(loginAttempt))
			{
				correctKey = true;
			}
		}
		
		//If user within validIDs, create challenge. Otherwise, return -1
		if(correctKey){
			return true;
		}
		else {
			return false;
		}
	}
	
	public byte[] createChallengeMSG(int rand) {
		String challengeMSG = "START\n" +
				"MSGTYPE: CHALLENGE\n" +
				"RAND: " + rand + "\n" +
				"END\n";
		return challengeMSG.getBytes();
		
	}
	
	public byte[] createAuthMsg(boolean success, int randCookie) {
		String msg = null;
		if(!success) {
		msg = "START\n" +
				"MSGTYPE: AUTH_FAIL\n" +
				"RAND: " + randCookie + "\n" +
				"END\n";
		return msg.getBytes();
		}
		else
		{
			msg = "START\n" +
					"MSGTYPE: AUTH_SUCCESS\n" +
					"RAND: " + randCookie + "\n" +
					"END\n";
			return msg.getBytes();
			
		}
		
	}
	
	//Given a clientID, fetches their secret key from the database.
		//Encrypts the key using MD5 and a rand_cookie, then adds the
		//client id and XRES into client challenges. Returns the rand cookie
		public static int createChallenge(String Client_ID) {
			int rand = (int) Math.floor(Math.random()*(9999-1000+1)+1000);
			String testKey = "test123"; //Should be taken from the database instead
			String XRES = Authenticator.A3(rand, testKey);
			clientChallenges.put(Client_ID, XRES);
			return rand;
		}

		//Given a client ID and a string response, returns true if the 
		//response matches the XRES string created by createChallenge
		public boolean verifyResponse(String Client_ID, String response) {
			if(clientChallenges.get(Client_ID) == response) {
				return true;
			}
			return false;
		}
	
	
	/*public static void main(String[] args)
	{
		UDPHandler authHandler = new UDPHandler();
		
		
	}*/
}
