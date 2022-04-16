package org.teamnine.server.Authenticator;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Authenticator implements Runnable {
	private int[] K_Alist;
	private int[] randCookieList;
	private DatagramSocket clientSocket;
	private int rand;
	
	public Authenticator(int port) throws SocketException {
		clientSocket = new DatagramSocket(port);
		rand = (int) Math.floor(Math.random()*(9999-1000+1)+1000);
	}
	// public Authenticator(int port, String dbaddr)
	
	public void run() {
		//Connect database to K_Alist
		
	}
	
	//Returns T/F depending on if a randCookie is in the list of known
	//randCookies
	public boolean validateRandCookie(int randCookie) { 
		for(int i : randCookieList) {
			if(randCookie == i)
			{
				return true;
			}	
		}
		return false;
		}
	
	//Returns a MD5 hashed string, where the input string is 
	//a randCookie and client K_A concatenated
	public String A3(int rand, int K_A) {
		try {  
			String input = String.valueOf(rand) + String.valueOf(K_A);
			
            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
  
            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());
  
            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);
  
            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } 
	
        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }	
	}
	
	
	//Returns a SHA1 hashed string, where the input string is 
	//a randCookie and client K_A concatenated
	public String A8(int rand, int K_A) {
		try {
			String input = String.valueOf(rand) + String.valueOf(K_A);
			
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
	        digest.reset();
	        digest.update(input.getBytes());
	        String hashtext = String.format("%040x", new BigInteger(1, digest.digest()));
	        
	        return hashtext;
		} 
		
		catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }	
		
	}
	
	
	//Boolean function that compares a MD5 hash to some randCookie and K_A
	public boolean A3match(String orig, int rand, int K_A){
	String newComp = A3(rand, K_A);
	if(orig.equals(newComp)) {return true;}
	else {return false;}
	}
	
	//Boolean function that compares a SHA1 hash to some randCookie and K_A
	public boolean A8match(String orig, int rand, int K_A){
		String newComp = A8(rand, K_A);
		if(orig.equals(newComp)) {return true;}
		else {return false;}
		}
	
	public int getRandCookie() {
		return rand;
	}
}
