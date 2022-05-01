package org.teamnine.server;

import org.teamnine.common.Authenticator;
import org.teamnine.common.ParseBuilder;
import org.teamnine.common.ParseException;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class UDPHandler {
	
	//Hashmaps containing passwords + challenges(XRES) for clientIDs
	private Map<String, String> clientChallenges = new HashMap<>();
	private Map<String, String> clientPasswords = new HashMap<>();
	//Database connection
	private Connection dbConn;

	//Constructor for UDPHandler, initializing dbConn
	public UDPHandler(Connection dbConn) {
		this.dbConn = dbConn;
	}

	//Given a byte array, converts to a string using String builder
	public static String data(byte[] a) {
		if (a == null)
			return null;
		StringBuilder ret = new StringBuilder();
		int i = 0;
		while (i < a.length && a[i] != 0) {
			ret.append((char) a[i]);
			i++;
		}
		return ret.toString();
	}

	//Given a byte string, convert to a normal string and determine
	//given string (clientID) is in the list of valid users
	public int handleHello(byte[] received, String[] ptrString) throws Exception {
		boolean validUser = false;
		System.out.println(data(received));
		//Create parsebuilder using the received data
		ParseBuilder pb = new ParseBuilder(new Scanner(new ByteArrayInputStream(received)));
		
		//Pass other parts of the message, extract username
		String username = pb.pass("START").pass("MSGTYPE:").pass("HELLO").pass("USERNAME:").extract();
		pb.pass("END");
		
		//SQL query to get count of clientIDs = username
		String sql = "SELECT COUNT(name) FROM USERS WHERE name = ?;";
		try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();
			validUser = rs.getInt(1) > 0;
		} catch (SQLException e) {
			System.err.println("FATAL: sql error");
			e.printStackTrace();
		}
		
		//If user within validIDs, create challenge. Otherwise, return -1
		if (validUser) {
			//returns the clientID to the server, and call createChallenge
			ptrString[0] = username;
			return createChallenge(username);
	} else {
			return -1;
		}
	}

	//Retrieves the password of a client given clientID.
	//Takes from clientPasswords HashMap
	public String getPasswordKeyOf(String clientID) { return clientPasswords.get(clientID); }
	
	//Processes RESPONSE message from the client
	public boolean processResponse(byte[] received) throws IOException {
		String username, res;
		//Create a parsebuilder using the received data
		ParseBuilder pb = new ParseBuilder(new Scanner(new ByteArrayInputStream(received)));
		try {
			//Extract the response message
			username = pb.pass("START").pass("MSGTYPE:").pass("RESPONSE").pass("USERNAME:").extract();
			res = pb.pass("RES:").extract();
			pb.pass("END");
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

		//check if the response matches the stored XRES
		String expected = clientChallenges.get(username);
		return expected != null && expected.equals(res);
	}

	//creates a CHALLENEGE MESSAGE given the rand_cookie
	public byte[] createChallengeMSG(int rand) {
		String challengeMSG = "START\n" +
				"MSGTYPE: CHALLENGE\n" +
				"RAND_COOKIE: " + rand + "\n" +
				"END\n";
		return challengeMSG.getBytes();

	}

	//creates an AUTH message (either SUCCESS or FAIL)
	public byte[] createAuthMsg(boolean success, int randCookie, String ClientID, int portNum) throws Exception {
		String msg = null;
		//If !success, create AUTH_FAIL
		if (!success) {
			msg = "START\n" +
					"MSGTYPE: AUTH_FAIL\n" +
					"END\n";
			return msg.getBytes();
		} 
		
		//Create AUTH_SUCCESS using given randCookie and port number if successful
		else {
			msg = "START\n" +
					"MSGTYPE: AUTH_SUCCESS\n" +
					"RAND_COOKIE: "+randCookie+"\n"+
					"PORT_NUMBER: "+portNum+"\n"+
					"END\n";
			//Encrypts the message using AES before returning the message
			Cipher cipher = Authenticator.getCipher(Cipher.ENCRYPT_MODE, randCookie, clientPasswords.get(ClientID));
			return cipher.doFinal(msg.getBytes(StandardCharsets.UTF_8));
		}

	}

	//Given a clientID, fetches their secret key from the database.
	//Encrypts the key using MD5 and a rand_cookie, then adds the
	//client id and XRES into client challenges. Returns the rand cookie
	public int createChallenge(final String Client_ID) throws Exception {
		//Create rand_Cookie (random int from 1000-9999
		int rand = (int) Math.floor(Math.random() * (9999 - 1000 + 1) + 1000);
		
		//Query database for user's secret key
		String testKey = null;
		String sql = "SELECT skey FROM USERS WHERE name = ?;";
		try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
			stmt.setString(1, Client_ID);
			ResultSet rs = stmt.executeQuery();
			testKey = rs.getString(1);
		}

		//Create the XRES that is stored in client challenges. Will be tested against
		//client RESPONSE messages to see if client's hashed response = XRES
		String XRES = Authenticator.A3(rand, testKey);
		clientChallenges.put(Client_ID, XRES);
		
		//Put client's password into the clientPasswords map
		clientPasswords.put(Client_ID, testKey);
		return rand;
	}
}
