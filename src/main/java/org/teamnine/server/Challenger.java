package org.teamnine.server;

import java.util.Map;

import org.teamnine.common.Authenticator;

public class Challenger 
{
	private Map <String, String> clientChallenges;
	
	public int createChallenege(String Client_ID) {
		int randCookie = (int) Math.floor(Math.random()*(9999-1000+1)+1000);
		String testKey = "test123";
		String XRES = Authenticator.A3(randCookie, testKey);
		clientChallenges.put(Client_ID, XRES);
		return randCookie;
	}

	public void verifyResponse(String Client_ID, String response) {
		
	}
}
