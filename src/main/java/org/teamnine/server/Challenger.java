package org.teamnine.server;

import java.util.Map;

import org.teamnine.common.Authenticator;

public class Challenger 
{
	private Map <String, String> clientChallenges;
	
	public int createChallenege(String Client_ID) {
		int rand = (int) Math.floor(Math.random()*(9999-1000+1)+1000);
		String testKey = "test123";
		String XRES = Authenticator.A3(rand, testKey);
		clientChallenges.put(Client_ID, XRES);
		return rand;
	}

	public boolean verifyResponse(String Client_ID, String response) {
		if(true) {
			
			return true;
		}
		return false;
	}
	
	
}
