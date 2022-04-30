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
	private Map<String, String> clientChallenges = new HashMap<>();
	private Map<String, String> clientPasswords = new HashMap<>();
	private Authenticator auth;
	private ParseBuilder parser;
	private Connection dbConn;

	//private Scanner in;
	public UDPHandler(Connection dbConn) {
		auth = new Authenticator();
		this.dbConn = dbConn;
		//in = new Scanner();
		//parser = new ParseBuilder();
	}

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
		ParseBuilder pb = new ParseBuilder(new Scanner(new ByteArrayInputStream(received)));
		String username = pb.pass("START").pass("MSGTYPE:").pass("HELLO").pass("USERNAME:").extract();
		pb.pass("END");
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
			ptrString[0] = username;
			return createChallenge(username);
	} else {
			return -1;
		}
	}

	public String getPasswordKeyOf(String clientID) { return clientPasswords.get(clientID); }
	public boolean processResponse(byte[] received) throws IOException {
		boolean correctKey = false;
		String username, res;
		ParseBuilder pb = new ParseBuilder(new Scanner(new ByteArrayInputStream(received)));
		try {
			username = pb.pass("START").pass("MSGTYPE:").pass("RESPONSE").pass("USERNAME:").extract();
			res = pb.pass("RES:").extract();
			pb.pass("END");
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

		String expected = clientChallenges.get(username);
		return expected != null && expected.equals(res);
	}

	public byte[] createChallengeMSG(int rand) {
		String challengeMSG = "START\n" +
				"MSGTYPE: CHALLENGE\n" +
				"RAND_COOKIE: " + rand + "\n" +
				"END\n";
		return challengeMSG.getBytes();

	}

	public byte[] createAuthMsg(boolean success, int randCookie, String ClientID, int portNum) throws Exception {
		String msg = null;
		if (!success) {
			msg = "START\n" +
					"MSGTYPE: AUTH_FAIL\n" +
					"END\n";
			return msg.getBytes();
		} else {
			msg = "START\n" +
					"MSGTYPE: AUTH_SUCCESS\n" +
					"RAND_COOKIE: "+randCookie+"\n"+
					"PORT_NUMBER: "+portNum+"\n"+
					"END\n";
			Cipher cipher = Authenticator.getCipher(Cipher.ENCRYPT_MODE, randCookie, clientPasswords.get(ClientID));
			return cipher.doFinal(msg.getBytes(StandardCharsets.UTF_8));
		}

	}

	//Given a clientID, fetches their secret key from the database.
	//Encrypts the key using MD5 and a rand_cookie, then adds the
	//client id and XRES into client challenges. Returns the rand cookie
	public int createChallenge(final String Client_ID) throws Exception {
		int rand = (int) Math.floor(Math.random() * (9999 - 1000 + 1) + 1000);
		String testKey = null;
		String sql = "SELECT skey FROM USERS WHERE name = ?;";
		try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
			stmt.setString(1, Client_ID);
			ResultSet rs = stmt.executeQuery();
			testKey = rs.getString(1);
		}


		String XRES = Authenticator.A3(rand, testKey);
		clientChallenges.put(Client_ID, XRES);
		clientPasswords.put(Client_ID, testKey);
		return rand;
	}

	//Given a client ID and a string response, returns true if the
	//response matches the XRES string created by createChallenge
	public boolean verifyResponse(String Client_ID, String response) {
		if (clientChallenges.get(Client_ID).equals(response)) {
			return true;
		}
		return false;
	}
}
