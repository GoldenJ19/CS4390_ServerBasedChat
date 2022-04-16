package org.teamnine.server.Authenticator;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.DatagramSocket;

public class Authenticator implements Runnable {
	private int[] randCookieList;
	public Authenticator(int port) {
		
	}
	// public Authenticator(int port, String dbaddr)
	
	public void run() {}
	

	public boolean validateRandCookie(int randCookie) { 
		for(int i : randCookieList) {
			if(randCookie == i)
			{
				return true;
			}	
		}
		return false;
		}
	
	public String hashMD5(String input) {
		try {  
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
	
	public boolean matching(String orig, String compare){
	String newComp = hashMD5(compare);
	if(orig.equals(newComp)) {return true;}
	else {return false;}
	}
}
